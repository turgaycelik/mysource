package com.atlassian.jira.upgrade;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;

import com.atlassian.core.util.DateUtils;
import com.atlassian.fugue.Option;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.util.IndexingConfiguration;
import com.atlassian.jira.issue.attachment.AttachmentStore;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.JiraListener;
import com.atlassian.jira.event.ListenerManager;
import com.atlassian.jira.event.listeners.search.IssueIndexListener;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.startup.JiraStartupLogger;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.CompositeShutdown;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.LuceneDirectoryUtils;
import com.atlassian.jira.util.Shutdown;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.util.system.check.PluginVersionCheck;
import com.atlassian.jira.util.system.check.SystemEnvironmentChecklist;
import com.atlassian.jira.web.ContextKeys;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import com.atlassian.plugin.PluginAccessor;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.search.BooleanQuery;
import org.ofbiz.core.entity.GenericValue;

/**
 * The consistency checker runs every time the web app is reloaded, and checks JIRA's consistency (duh ;))
 * <p/>
 * At the moment it just looks to check that certain Listeners are loaded, but in the future it can check the
 * consistency of data etc.
 */
public class ConsistencyCheckImpl implements ConsistencyChecker
{
    private static final Logger log = Logger.getLogger(ConsistencyCheckImpl.class);
    private final JiraStartupLogger startupLog = new JiraStartupLogger();

    static final String INIT_KEY = ConsistencyCheckImpl.class.getName() + ":initialized";

    public void destroy(final ServletContext context)
    {
        final Boolean initialized = (Boolean) context.getAttribute(INIT_KEY);
        if (initialized == null || !initialized)
        {
            return;
        }

        // make sure all services clean up after themselves
        final Collection<JiraServiceContainer> services = getServices();

        for (final Object element : services)
        {
            final JiraServiceContainer service = (JiraServiceContainer) element;
            try
            {
                service.destroy();
            }
            catch (final RuntimeException e)
            {
                log.error(
                    "Failed to destroy service '" + ((service != null) && (service.getName() != null) ? service.getName() : "Unknown") + "' " + e.getMessage(),
                    e);
            }
        }
        try
        {
            getShutdown().shutdown();
        }
        catch (final RuntimeException e)
        {
            log.error("Failed to run shutdown hooks.", e);
        }
    }

    private Shutdown getShutdown()
    {
        return new CompositeShutdown(getIndexManagerShutdown(), new Shutdown()
                {
                    public void shutdown()
                    {
                        getTaskManager().shutdownAndWait(0);
                    }
                });
    }

    /*
     * overridden in tests
     */
    Shutdown getComponentManager()
    {
        return ComponentManager.getInstance();
    }

    /*
     * overridden in tests
     */
    IndexLifecycleManager getIndexManager()
    {
        return ComponentAccessor.getComponent(IndexLifecycleManager.class);
    }

    LuceneDirectoryUtils getLuceneDirectoryUtils()
    {
        return ComponentAccessor.getComponent(LuceneDirectoryUtils.class);
    }

    /**
     * The shutdown instance used to shutdown the index manager. Needed to handle very rare exceptions.
     *
     * @return a shutdownable
     */
    private Shutdown getIndexManagerShutdown()
    {
        try
        {
            return getIndexManager();
        }
        catch (final RuntimeException e)
        {
            log.error("Failed to get IndexManager, cannot shut it down cleanly...", e);
            return new Shutdown()
            {
                public void shutdown()
                {}

                @Override
                public String toString()
                {
                    return "NullShutdownForIndexManager";
                }
            };
        }
    }

    /*
     * overridden in tests
     */
    TaskManager getTaskManager()
    {
        return ComponentAccessor.getComponent(TaskManager.class);
    }

    /**
     * Gets all the currently registered services with JIRA.
     *
     * @return Unmodifiable collection of {@link com.atlassian.jira.service.JiraServiceContainer}
     * @see com.atlassian.jira.service.ServiceManager#getServices()
     */
    protected Collection<JiraServiceContainer> getServices()
    {
        return ComponentAccessor.getComponent(ServiceManager.class).getServices();
    }

    public void initialise(final ServletContext servletContext)
    {
        setStartupTime(servletContext);

        checkConsistency(servletContext);
        checkAndInitLucene(servletContext);
        new PluginVersionCheck(ComponentAccessor.getComponentOfType(PluginAccessor.class), ComponentAccessor.getComponentOfType(BuildUtilsInfo.class)).check(servletContext);

        printJIRAStartupMessage(servletContext);

        ServletContextProvider.getServletContext().setAttribute(INIT_KEY, Boolean.TRUE);
    }

    private void printJIRAStartupMessage(final ServletContext context)
    {
        // Print successful start up message if there are no events in the context.
        final JohnsonEventContainer eventCont = JohnsonEventContainer.get(context);
        if ((eventCont == null) || !eventCont.hasEvents())
        {
            // Finished loading JIRA
            startupLog.printStartedMessage();
        }

        // We do this after JIRA has started up so that these warnings come last (and are thus still visible to the
        // admin. If we printed it before printStartedMessage() then they would get scrolled way off the screen
        // making it unlikely most people would ever see them.
        checkSystemEnvironment();
    }

    public void checkConsistency(final ServletContext context)
    {
        // check consistency
        try
        {
            log.info("Checking JIRA consistency");

            final boolean connection = checkConnection(context);

            if (connection)
            {
                checkDataConsistency(context);
            }
        }
        catch (Exception dataConsistencyCheckException)
        {
            log.error("An error occurred during the consistency check", dataConsistencyCheckException);
        }
    }

    public void checkDataConsistency(final ServletContext context) throws Exception
    {
        checkMailListenerAndService();
        checkIssueAssignHistoryListener();
        checkAttachmentStorage();
        checkIndexingSetup(context);
        checkLanguageExists();
        checkAndInitSID();
    }

    private void checkSystemEnvironment()
    {
        final List<String> messages = SystemEnvironmentChecklist.getEnglishWarningMessages();

        for (String message : messages)
        {
            startupLog.printMessage(message, Level.WARN);
        }
    }

    /**
     * Looks for files that could be Lucene locks left after an unclean shutdown. Registers a Johnson Event in this
     * scenario.
     */
    private void checkAndInitLucene(final ServletContext context)
    {
        final ApplicationProperties ap = ComponentAccessor.getApplicationProperties();

        // Get a path for each index directory
        final IndexLifecycleManager indexManager = getIndexManager();

        // A collection to which we will add all found lock files (if any)
        final Collection<String> existingLockFilepaths = getLuceneDirectoryUtils().getStaleLockPaths(indexManager.getAllIndexPaths());

        // If there were any lock files found, then place an event into the context. Otherwise we are OK and
        // can proceed.
        if ((existingLockFilepaths != null) && !existingLockFilepaths.isEmpty())
        {
            final StringBuilder sb = new StringBuilder();
            for (final String filePath : existingLockFilepaths)
            {
                if (filePath != null)
                {
                    sb.append(filePath).append(' ');
                }
            }

            if (sb.length() > 1)
            {
                // Delete last " "
                sb.deleteCharAt(sb.length() - 1);
            }

            // Log error message
            final Collection<String> messages = CollectionBuilder.newBuilder(
                    "Index lock file(s) found. This occurs either because JIRA was not cleanly shutdown",
                    "or because there is another instance of this JIRA installation currently running.",
                    "Please ensure that no other instance of this JIRA installation is running",
                    "and then remove the following lock file(s) and restart JIRA:", "", sb.toString(), "",
                    "Once restarted you will need to reindex your data to ensure that indexes are up to date.", "",
                    "Do NOT delete the lock file(s) if there is another JIRA running with the same index directory",
                    "instead cleanly shutdown the other instance.").asList();
            startupLog.printMessage(messages, Level.ERROR);

            final Event event = new Event(EventType.get("index-lock-already-exists"), "An existing index lock was found.",
                    EventLevel.get(EventLevel.ERROR));
            event.addAttribute("lockfiles", sb.toString());
            final JohnsonEventContainer eventCont = JohnsonEventContainer.get(context);
            if (eventCont != null)
            {
                eventCont.addEvent(event);
            }
        }

        // Set max clauses even if indexing is disabled so that it takes affect when indexing is enabled again.
        // As max clauses is a static variable in Lucene, this will work.
        int maxClausesCount = 65000;

        try
        {
            maxClausesCount = Integer.parseInt(ap.getDefaultBackedString(APKeys.JIRA_SEARCH_MAXCLAUSES));
        }
        catch (final NumberFormatException e)
        {
            log.warn("Could not read the property '" + APKeys.JIRA_SEARCH_MAXCLAUSES + "' for the number of maximum search clauses. Using default " + maxClausesCount);
        }

        BooleanQuery.setMaxClauseCount(maxClausesCount); // Fixes JRA-3127 (JT)
    }

    /**
     * Rather than create an upgrade task, just ensure that the language has been set, or else set it to English.
     */
    private void checkLanguageExists()
    {
        final ApplicationProperties ap = ComponentAccessor.getApplicationProperties();

        if (ap.getString(APKeys.JIRA_I18N_LANGUAGE_INPUT) == null)
        {
            log.info("Input Language has not been set.  Setting to 'English - Moderate Stemming'");
            ap.setString(APKeys.JIRA_I18N_LANGUAGE_INPUT, APKeys.Languages.ENGLISH_MODERATE_STEMMING);
        }
    }

    private boolean checkConnection(final ServletContext context)
    {
        boolean worked = true;

        try
        {
            final OfBizDelegator ofBizDelegator = ComponentAccessor.getOfBizDelegator();

            if (ofBizDelegator == null)
            {
                log.error("Could not get OfBizDelegator");
                worked = false;
            }
            else
            {
                try
                {
                    ofBizDelegator.findAll("Project");
                }
                catch (final RuntimeException e)
                {
                    log.error("Could not connect to database. Check your entityengine.xml settings: " + e, e);
                    worked = false;

                    //Add an error that you could not connect to the database
                    final Event event = new Event(EventType.get("database"), "Could not connect to database", e.getMessage(),
                            EventLevel.get(EventLevel.ERROR));
                    final JohnsonEventContainer eventCont = JohnsonEventContainer.get(context);
                    if (eventCont != null)
                    {
                        eventCont.addEvent(event);
                    }
                }
            }
        }
        catch (final Exception databaseConnectionTestException)
        {
            log.error("Could not check database connection. Check your entityengine.xml settings.", databaseConnectionTestException);
            worked = false;

            //Add an error that you could not connect to the database
            final Event event = new Event(EventType.get("database"), "Could not connect to database.", databaseConnectionTestException.getMessage(),
                    EventLevel.get(EventLevel.ERROR));
            final JohnsonEventContainer eventCont = JohnsonEventContainer.get(context);
            if (eventCont != null)
            {
                eventCont.addEvent(event);
            }
        }

        return worked;
    }

    private void checkMailListenerAndService() throws Exception
    {
        ensureSingleListener(com.atlassian.jira.event.listeners.mail.MailListener.class, "Mail Listener");
        ensureSingleService("com.atlassian.jira.service.services.mail.MailQueueService", "Mail Queue Service");
    }

    private void checkIssueAssignHistoryListener() throws Exception
    {
        ensureSingleListener(com.atlassian.jira.event.listeners.history.IssueAssignHistoryListener.class, "Issue Assignment Listener");
    }

    private void checkAttachmentStorage() throws Exception
    {
        final ApplicationProperties ap = ComponentAccessor.getApplicationProperties();

        if (ap.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS))
        {
            final Option<ErrorCollection> result = ComponentAccessor.getComponent(AttachmentStore.class).errors();

            if (result.isDefined())
            {
                log.error("Attachments are turned on, " + result.get().toString() + " - disabling attachments");
                ap.setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, false);
            }
        }
    }

    /**
     * Checks that the index path exists, and the IndexingListener exists
     */
    private void checkIndexingSetup(final ServletContext context) throws Exception
    {
        final ApplicationProperties ap = ComponentAccessor.getApplicationProperties();

        final String indexPath = ComponentAccessor.getIndexPathManager().getIndexRootPath();

        if (!directoryExists(indexPath))
        {
            log.error("Indexing is turned on, but index path [" + indexPath + "] invalid - disabling indexing");
            removeListeners(IssueIndexListener.class);

            // Mark the index as disabled
            ComponentAccessor.getComponent(IndexingConfiguration.class).disableIndex();

            if (JiraUtils.isSetup())
            {
                final Event event = new Event(EventType.get("reindex"), "The JIRA search index is missing.", EventLevel.get(EventLevel.ERROR));
                final JohnsonEventContainer eventCont = JohnsonEventContainer.get(context);
                if (eventCont != null)
                {
                    eventCont.addEvent(event);
                }
            }
            return;
        }
        // ensure the indexing listener is there
        ensureSingleListener(IssueIndexListener.class, "Issue Index Listener");
    }

    /**
     * Returns true if a given path exists and is a directory
     */
    private boolean directoryExists(final String path)
    {
        if (path != null)
        {
            final File dir = new File(path);
            if (dir.exists() && dir.isDirectory())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Looks through all the listeners, and ensures that there is only one of the given listener.
     * <p/>
     * If there is more than one, the extras are deleted. If there aren't any, one is added with the given class and
     * name
     */
    private void ensureSingleListener(final Class<? extends JiraListener> clazz, final String name) throws Exception
    {
        final String classname = clazz.getName();
        // check that there is one and only one cache listener
        @SuppressWarnings ("unchecked")
        final Collection<GenericValue> listenerConfigs = ComponentAccessor.getOfBizDelegator().findAll("ListenerConfig");

        boolean foundOne = false;

        final List<GenericValue> toRemove = new ArrayList<GenericValue>();

        for (final Object element : listenerConfigs)
        {
            final GenericValue listenerConfig = (GenericValue) element;

            if (listenerConfig.getString("clazz").equals(classname))
            {
                if (foundOne)
                {
                    // we already found one - delete this listener
                    toRemove.add(listenerConfig);
                }
                else
                {
                    foundOne = true;
                }
            }
        }

        if (!foundOne)
        {
            //if application setup - then send an error, else just @ info level
            if ("true".equals((ComponentAccessor.getApplicationProperties()).getString(APKeys.JIRA_SETUP)))
            {
                log.error("Could not find " + name + ", adding.");
            }
            else
            {
                log.info("Could not find " + name + ", adding.");
            }

            ListenerManager listenerManager = ComponentAccessor.getComponent(ListenerManager.class);

            try
            {
                listenerManager.createListener(name, clazz);
            }
            catch (final Exception e)
            {
                log.error("Error adding listener: " + e, e);
            }
        }
        else if (toRemove.size() > 0)
        {
            log.debug("Removing " + toRemove.size() + " extra listeners with class " + classname);
            ComponentAccessor.getOfBizDelegator().removeAll(toRemove);
            ComponentAccessor.getListenerManager().refresh();
        }
    }

    /**
     * Looks through all the services, and ensures that there is only one of the given service.
     * <p/>
     * If there is more than one, the extras are deleted. If there aren't any, one is added with the given class and
     * name
     */
    private void ensureSingleService(final String clazz, final String name) throws Exception
    {
        final Collection<JiraServiceContainer> serviceConfigs = getServices();

        boolean foundOne = false;

        final List<JiraServiceContainer> toRemove = new ArrayList<JiraServiceContainer>();

        for (final JiraServiceContainer service : serviceConfigs)
        {
            if (service.getServiceClass().equals(clazz))
            {
                if (foundOne)
                {
                    // we already found one - delete this service
                    toRemove.add(service);
                }
                else
                {
                    foundOne = true;
                }
            }
        }

        if (!foundOne)
        {
            //if application setup - then send an error, else just @ info level
            if ("true".equals((ComponentAccessor.getApplicationProperties()).getString(APKeys.JIRA_SETUP)))
            {
                log.error("Could not find " + name + ", adding.");
            }
            else
            {
                log.info("Could not find " + name + ", adding.");
            }

            // add new service
            try
            {
                ComponentAccessor.getServiceManager().addService(name, clazz, DateUtils.MINUTE_MILLIS);
            }
            catch (final Exception e)
            {
                log.error("Error adding service: " + e, e);
            }

        }
        else if (!toRemove.isEmpty())
        {
            log.debug("Removing " + toRemove.size() + " extra services with class " + clazz);
            for (final Object element : toRemove)
            {
                final JiraServiceContainer serviceContainer = (JiraServiceContainer) element;
                ComponentAccessor.getServiceManager().removeService(serviceContainer.getId());
            }
        }
    }

    /**
     * Stores the system startup time in the context so that system uptime can be computed and displayed on system info
     * page
     */
    private void setStartupTime(final ServletContext context)
    {
        if (context != null)
        {
            context.setAttribute(ContextKeys.STARTUP_TIME, System.currentTimeMillis());
        }
    }

    /**
     * Remove all the listeners of this class.
     *
     * @param clazz The class of the listener to be removed
     */
    private void removeListeners(final Class<? extends JiraListener> clazz)
    {
        //remove the listener
        ComponentAccessor.getComponent(ListenerManager.class).deleteListener(clazz);
    }

    /**
     * This methods just make sure to get the server ID which will generate one if necessary at JIRA's startup.
     */
    private void checkAndInitSID()
    {
        final String serverId = ComponentAccessor.getComponentOfType(JiraLicenseService.class).getServerId();
        if (log.isInfoEnabled())
        {
            log.info("The Server ID for this JIRA instance is: [" + serverId + "]");
        }
    }
}
