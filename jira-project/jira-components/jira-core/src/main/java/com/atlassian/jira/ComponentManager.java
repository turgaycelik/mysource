package com.atlassian.jira;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;

import com.atlassian.annotations.Internal;
import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.filter.SearchRequestAdminService;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.bulkedit.BulkOperationManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.diagnostic.PluginDiagnostics;
import com.atlassian.jira.event.ComponentManagerShutdownEvent;
import com.atlassian.jira.event.ComponentManagerStartedEvent;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.extension.ContainerProvider;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequestFactory;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.mail.MailingListCompiler;
import com.atlassian.jira.mail.SubscriptionMailQueueItemFactory;
import com.atlassian.jira.movesubtask.MoveSubTaskOperationManager;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.JiraOsgiContainerManager;
import com.atlassian.jira.plugin.assignee.AssigneeResolver;
import com.atlassian.jira.plugin.component.ComponentModuleDescriptor;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.startup.JiraStartupChecklist;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.jira.upgrade.UpgradeManager;
import com.atlassian.jira.upgrade.UpgradeManagerImpl;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.Shutdown;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.web.action.issue.IssueCreationHelperBean;
import com.atlassian.jira.web.util.FileIconBean;
import com.atlassian.jira.web.util.JiraLocaleUtils;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginSystemLifecycle;
import com.atlassian.plugin.event.NotificationException;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.osgi.container.OsgiContainerManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.atlassian.velocity.VelocityManager;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.type.TypeBindings;
import org.codehaus.jackson.map.type.TypeFactory;
import org.joda.time.DateTime;
import org.osgi.util.tracker.ServiceTracker;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

import static java.lang.String.format;

/**
 * This component manager uses PicoContainer to resolve all the dependencies between components.
 * <p/>
 * <p/> It is responsible for initialising a large number of components in JIRA. Any components defined here may be
 * injected via a constructor.
 * <p/>
 * <p/> The ComponentManager also has various static accessor methods for non-Pico-managed objects, eg.
 * <code>ComponentManager.getInstance().getProjectManager()</code>. Plugins developers should no longer use these -
 * please use {@link com.atlassian.jira.component.ComponentAccessor} instead.
 * <p/>
 * <p/> More information can be found at the <a href="http://www.picocontainer.org">picocontainer website</a>.
 */
@Internal
public class ComponentManager implements Shutdown
{

    public static final String EXTENSION_PROVIDER_PROPERTY = "jira.extension.container.provider";

    private static final Logger log = Logger.getLogger(ComponentManager.class);

    private static final ComponentManager COMPONENT_MANAGER = new ComponentManager();
    private static final String BOOTSTRAP_CONTAINER = "BootstrapContainer";
    private static final String JIRA_CONTAINER = "JIRAContainer";

    //
    // instance fields
    //

    private volatile WrappedComponentContainer container;

    private final PluginSystem pluginSystem = new PluginSystem();

    private volatile ComponentManagerStateImpl state = ComponentManagerStateImpl.NOT_STARTED;

    /**
     * Constructor made private, singleton.
     */
    private ComponentManager()
    {
    }

    /**
     * Initialization registers components for the bootstrap loading of JIRA.  This gets enough of PICO registered to
     * allow JIRA to bootstrap without a database.
     */
    public void bootstrapInitialise()
    {
        initComponentContainer(true, BOOTSTRAP_CONTAINER);
        new BootstrapContainerRegistrar().registerComponents(container.getComponentContainer());
        changeState(ComponentManagerStateImpl.CONTAINER_INITIALISED);
    }


    /**
     * If JIRA needs to be setup, then add the extra components needed to the container.
     */
    public void setupInitialise()
    {
         String error = validateBootstrapContainer();
         if (error != null)
         {
             throw new IllegalStateException(error);
         }
         new SetupContainerRegistrar().registerComponents(container.getComponentContainer());
    }


    private String validateBootstrapContainer()
    {
        String errorMessage = null;
        if (container == null)
        {
            errorMessage = "The bootstrap container has not been initialised, you cannot initialise a Setupcontainer";
        }
        else if (container.getPicoContainer().getName().startsWith(JIRA_CONTAINER))
        {
            errorMessage = "The main JIRA container cannot be used to initialise a SetupContainer";
        }
        else if (!state.isContainerInitialised())
        {
            errorMessage = String.format("The ComponentManager is %s so you cannot initialise a SetupContainer", state.name());
        }
        return errorMessage;
    }

    @VisibleForTesting
    void initialise(final boolean useEagerInitialization)
    {
        registerComponents(useEagerInitialization);
        registerExtensions();
        runInitializingComponents();
        changeState(ComponentManagerStateImpl.CONTAINER_INITIALISED);
    }

    /**
     * Initialization registers components and then registers extensions.
     */
    public void initialise()
    {
        initialise(true);
    }

    /**
     * Adds license configuration in license manager.
     */
    public synchronized void start()
    {
        quickStart();
        // Need to ensure that the components are eagerly instantiated after the "component" plugins had a chance to register.
        // As otherwise the default components are used to instantiate other default components. See JRA-4950
        eagerlyInstantiate();
    }

    /**
     * This is here (outside of the initialise method) as the getComponentInstancesOfType method starts instantiating
     * components and calls on the LicenseComponentAdpater which tries to get reference to this object using the {@link
     * ComponentManager#getInstance()} method. That method returns null as the reference to this object does not exist
     * until the initialise method completes. So this method should be invoked after the initialise method completes
     * execution.
     */
    private void quickStart()
    {
        // The Jackson TypeFactory singleton caches classes related to the first HashMap and
        // ArrayList that it sees. If this comes from a plugin, it can end up caching classes from
        // OSGi class loaders, which can hang on to memory for far longer than is desirable.  This
        // call forces population of the HashMap cache before the plugin system comes up, and hence
        // before any OSGi class loaders are on the scene. My attempts to safely populate the
        // ArrayList cache have failed - i have never seen it populated in the wild, and the obvious
        // analogue call does not seem to populate the cache, and digging down to call
        // findTypeParameters directly caused crashes in code using jackson.
        TypeFactory.instance.constructType(HashMap.class, (TypeBindings) null);

        getComponent(PluginDiagnostics.class); // eagerly load to catch events on plugin system startup
        pluginSystem.start();

        changeState(ComponentManagerStateImpl.PLUGINSYSTEM_STARTED);
        // now register component plugins before starting anything
        final PluginAccessor pluginAccessor = getPluginAccessor();
        final List<ComponentModuleDescriptor> funNewComponents = pluginAccessor.getEnabledModuleDescriptorsByClass(ComponentModuleDescriptor.class);

        if (!funNewComponents.isEmpty())
        {
            for (final ComponentModuleDescriptor componentModuleDescriptor : funNewComponents)
            {
                componentModuleDescriptor.registerComponents(container.getPicoContainer());
            }
        }
        container.getPicoContainer().addComponent(pluginAccessor.getClassLoader());

        changeState(ComponentManagerStateImpl.COMPONENTS_REGISTERED);

        // 1. Call start() if they are startable.
        runStartable();
        // 2. Register components with the EventPublisher if they annotate with @EventComponent.
        registerEventComponents();

        changeState(ComponentManagerStateImpl.STARTED);
        getComponent(EventPublisher.class).publish(ComponentManagerStartedEvent.INSTANCE);
    }


    private void initComponentContainer(boolean useEagerInitialization, String name)
    {
        if(container != null){
            throw new IllegalStateException("Component container is already initialized");
        }

        container = new WrappedComponentContainer(new ComponentContainer(useEagerInitialization));
        container.getPicoContainer().setName(name+"_"+DateTime.now().toString());
    }

    private void registerEventComponents()
    {
        EventPublisher eventPublisher = getComponent(EventPublisher.class);
        Set<Object> registeredListeners = Sets.newIdentityHashSet();
        Collection<ComponentAdapter<?>> componentAdapters = getContainer().getComponentAdapters();
        for (ComponentAdapter<?> componentAdapter : componentAdapters)
        {
            Class<?> componentKey = componentAdapter.getComponentImplementation();
            if (componentKey.getAnnotation(EventComponent.class) != null)
            {
                Object instance = componentAdapter.getComponentInstance(container.getPicoContainer(), ComponentAdapter.NOTHING.class);
                if (registeredListeners.add(instance))
                {
                    eventPublisher.register(instance);
                }
            }
        }
    }

    private void runInitializingComponents()
    {
        List<InitializingComponent> components = getContainer().getComponents(InitializingComponent.class);
        for (InitializingComponent component : components)
        {
            try
            {
                component.afterInstantiation();
            }
            catch (final Exception e)
            {
                log.error("Error occurred while initializing component '" + component.getClass().getName() + "'.", e);
                throw new InfrastructureException("Error occurred while initializing component '" + component.getClass().getName() + "'.", e);
            }
        }
    }

    private void runStartable()
    {
        List<Startable> startables = getContainer().getComponents(Startable.class);
        for (Startable startable : startables)
        {
            try
            {
                if (!(startable instanceof PluginSystemLifecycle))// don't start the plugin manager twice!
                {
                    startable.start();
                }
            }
            catch (final Exception e)
            {
                log.error("Error occurred while starting component '" + startable.getClass().getName() + "'.", e);
                throw new InfrastructureException("Error occurred while starting component '" + startable.getClass().getName() + "'.", e);
            }            
        }

    }

    public synchronized void stop()
    {
        getComponent(EventPublisher.class).publish(ComponentManagerShutdownEvent.INSTANCE);
        pluginSystem.shutdown();
    }

    public void dispose()
    {
        //JRADEV-21332:: Ensure the cache descriptors are cleared to release any ClassLoaders they hold
        PropertyUtils.clearDescriptors();
        changeState(ComponentManagerStateImpl.NOT_STARTED);
        //JRADEV-23443 - lets try to free up the permgen before adding the new plugin system
        if (container != null)
        {
            container.dispose();
            container = null;
        }
        gc();
    }

    public void shutdown()
    {
        stop();
        dispose();
    }


    private static void gc()
    {
        int count = 0;
        Object obj = new Object();
        WeakReference<Object> ref = new java.lang.ref.WeakReference<Object>(obj);

        //noinspection UnusedAssignment
        obj = null;

        // break after 100 attempts
        while (count < 10 && ref.get() != null)
        {
            count++;
            log.debug("Attempting to do a garbage collection:" + count);
            System.gc();
        }
    }

    /**
     * What {@link State} is the {@link ComponentManager} in.
     *
     * @return the current state.
     */
    public State getState()
    {
        return state;
    }

    /**
     * Eagerly instantiates the container by making a call to {@link org.picocontainer.PicoContainer#getComponents()} ()} method on
     * the container that is returned by {@link ComponentManager#getContainer()} method.
     */
    @GuardedBy ("this")
    private void eagerlyInstantiate()
    {
        // this is to work around synchronisation problems with Pico (PICO-199)
        // http://jira.codehaus.org/browse/PICO-199
        // Pico has problems if it is instantiating A+B from different threads, and both depend on C
        // and they are using synchronised component adapters. You then get a deadlock.
        // This only happens when C is not registered, or C is not registered by its interface,
        // in which case PICO does a full tree walk (from within a synchronised method!).
        // This really needs to get fixed, but one work around is to full instantiate the tree first,
        // in which case, the need for a full tree walk is decreased.
        container.getComponentContainer().initializeEagerComponents();
    }

    private void registerExtensions()
    {

        final ApplicationProperties applicationProperties = container.getComponentContainer().getComponentInstance(ApplicationProperties.class);
        final String extensionClassName = applicationProperties.getDefaultBackedString(EXTENSION_PROVIDER_PROPERTY);
        try
        {
            if (!StringUtils.isBlank(extensionClassName))
            {
                container.wrapWith(((ContainerProvider) ClassLoaderUtils.loadClass(extensionClassName, getClass()).newInstance()));
            }
        }
        catch (Exception extensionClassLoadingException)
        {
            throw new RuntimeException
                    (
                            format
                                    (
                                            "Error loading PICO extension provider container class with name '%s'",
                                            extensionClassName
                                    ), extensionClassLoadingException
                    );
        }
    }

    /**
     * Returns container
     *
     * @return container
     */
    public PicoContainer getContainer()
    {
        if (container == null)
        {
            return null;
        }
        return container.getPicoContainer();
    }

    /**
     * Returns container
     *
     * @return container
     */
    public MutablePicoContainer getMutablePicoContainer()
    {
        final WrappedComponentContainer container = this.container;  // volatile read
        return (container != null) ? container.getPicoContainer() : null;
    }

    /**
     * This method registers all components with the internal pico-container.
     * @param useEagerInitialization indicates whether container should initialize all components on startup
     */
    private void registerComponents(final boolean useEagerInitialization)
    {
        initComponentContainer(useEagerInitialization, JIRA_CONTAINER);
        new ContainerRegistrar().registerComponents(container.getComponentContainer(), JiraStartupChecklist.startupOK());
    }

    private ComponentManagerStateImpl changeState(ComponentManagerStateImpl newState)
    {
        final ComponentManagerStateImpl currentState = state;

        //check whether we want to stop (what we can always do) or step state "by one"
        if (newState != ComponentManagerStateImpl.NOT_STARTED && newState.ordinal() != currentState.ordinal()+1)
        {
            throw new IllegalStateException(String.format("Cannot change ComponentManager status from %s to %s", currentState, newState));
        }

        state = newState;
        return currentState;
    }

    /**
     * Retrieves and returns the web resource manager instance
     *
     * @return web resource manager
     */
    public WebResourceManager getWebResourceManager()
    {
        return getContainer().getComponent(WebResourceManager.class);
    }

    /**
     * Retrieves and returns the attachment manager instance
     *
     * @return attachment manager
     */
    public AttachmentManager getAttachmentManager()
    {
        return getContainer().getComponent(AttachmentManager.class);
    }

    /**
     * Retrieves and returns the version manager instance
     *
     * @return version manager
     */
    public VersionManager getVersionManager()
    {
        return getContainer().getComponent(VersionManager.class);
    }

    /**
     * Retrieves and return the bulk operation manager instance
     *
     * @return bulk operation manager
     */
    public BulkOperationManager getBulkOperationManager()
    {
        return getContainer().getComponent(BulkOperationManager.class);
    }

    /**
     * Retrieves and returns the move subtask operation manager instance
     *
     * @return move subtask operation manager
     */
    public MoveSubTaskOperationManager getMoveSubTaskOperationManager()
    {
        return getContainer().getComponent(MoveSubTaskOperationManager.class);
    }

    /**
     * Retuns a singleton instance of this class.
     *
     * @return a singleton instance of this class
     *
     * @deprecated Public use of this method is deprecated - please use {@link com.atlassian.jira.component.ComponentAccessor} instead. Since v5.2.
     */
    public static ComponentManager getInstance()
    {
        return COMPONENT_MANAGER;
    }

    /**
     * Retrieves and returns a component which is an instance of given class.
     * <p>
     * In practise, this is the same as {@link #getComponent(Class)} except it will try to find a unique component that
     * implements/extends the given Class even if the Class is not an actual component key.
     * <p> Please note that this method only gets components from JIRA's core Pico Containter. That is, it retrieves
     * core components and components declared in Plugins1 plugins, but not components declared in Plugins2 plugins.
     * Plugins2 components can be retrieved via the {@link #getOSGiComponentInstanceOfType(Class)} method, but only if
     * they are public.
     *
     * @param clazz class to find a component instance by
     * @return found component
     * @see #getOSGiComponentInstanceOfType(Class)
     * @see PicoContainer#getComponent(Class))
     *
     * @deprecated since 6.0 - please use the jira-api {@link com.atlassian.jira.component.ComponentAccessor#getComponent(Class)} instead
     *
     */
     /*
       NOTE to JIRA DEVS : Stop using this method for general purpose component retrieval.  Use ComponentAccessor please.
     */
    public static <T> T getComponentInstanceOfType(final Class<T> clazz)
    {
        // Try fast approach
        T component = getComponent(clazz);
        if (component != null)
        {
            return component;
        }
        // Look the slow way
        component = clazz.cast(getInstance().getContainer().getComponent(clazz));
        if (component != null)
        {
            // Lets log this so we know there is a naughty component
            if (log.isDebugEnabled())
            {
                // Debug mode - include a stacktrace to find the caller
                try
                {
                    throw new IllegalArgumentException();
                }
                catch (IllegalArgumentException ex)
                {
                    log.warn("Unable to find component with key '" + clazz + "' - eventually found '" + component + "' the slow way.", ex);
                }
            }
            else
            {
                log.warn("Unable to find component with key '" + clazz + "' - eventually found '" + component + "' the slow way.");
            }
        }
        return component;
    }

    /**
     * Retrieves and returns a component which is an instance of given class.
     * <p>
     * In practise, this is the same as {@link #getComponentInstanceOfType(Class)} except it will fail faster if the
     * given Class is not a known component key (it also has a shorter and more meaningful name).
     * <p>
     * Please note that this method only gets components from JIRA's core Pico Containter. That is, it retrieves
     * core components and components declared in Plugins1 plugins, but not components declared in Plugins2 plugins.
     * Plugins2 components can be retrieved via the {@link #getOSGiComponentInstanceOfType(Class)} method, but only if
     * they are public.
     *
     * @param clazz class to find a component instance by
     * @return found component, or null if not found
     * @see #getOSGiComponentInstanceOfType(Class)
     * @see PicoContainer#getComponent(Object)
     *
     * @deprecated since 6.0 - please use the jira-api {@link com.atlassian.jira.component.ComponentAccessor#getComponent(Class)} instead
     */
     /*
       NOTE to JIRA DEVS : Stop using this method for general purpose component retrieval.  Use ComponentAccessor please.
     */
    public static <T> T getComponent(final Class<T> clazz)
    {
        return getInstance().getContainer().getComponent(clazz);
    }

    /**
     * Retrieves and returns a public component from OSGi land via its class name.  This method can be used to retrieve
     * a component provided via a plugins2 OSGi bundle.  Please note that components returned via this method should
     * *NEVER* be cached (e.g. in a static field) as they may be refreshed at any time as a plugin is enabled/disabled
     * or the componentManager is reinitialised (after an XML import).
     * <p>
     * Plugin developers should prefer the API method {@link com.atlassian.jira.component.ComponentAccessor#getOSGiComponentInstanceOfType(Class)}.
     * <p> It is important to note that this only works for public components. That is components with {@code
     * public="true"} declared in their XML configuration. This means that they are available for other plugins to
     * import.
     * <p> A use case for this is when for example for the dashboards plugin.  In several areas in JIRA we may want to
     * render gadgets via the {@link com.atlassian.gadgets.view.GadgetViewFactory}.  Whilst the interface for this
     * component is available in JIRA core, the implementation is provided by the dashboards OSGi bundle.  This method
     * will allow us to access it.
     *
     * @param clazz class to find an OSGi component instance for
     * @return found component
     * @see #getComponentInstanceOfType(Class)
     *
     * @deprecated since 6.0 - please use the jira-api {@link com.atlassian.jira.component.ComponentAccessor#getOSGiComponentInstanceOfType(Class)} instead
     */
     /*
       NOTE to JIRA DEVS : Stop using this method for general purpose component retrieval.  Use ComponentAccessor please.
     */
    public static <T> T getOSGiComponentInstanceOfType(final Class<T> clazz)
    {
        Assertions.notNull("class", clazz);

        OsgiContainerManager osgiContainerManager = getComponentInstanceOfType(OsgiContainerManager.class);
        if (osgiContainerManager != null)
        {
            // this is the happy path. uses a cached service tracker to get the component
            if (osgiContainerManager instanceof JiraOsgiContainerManager)
            {
                return ((JiraOsgiContainerManager) osgiContainerManager).getOsgiComponentOfType(clazz);
            }

            // in practice we will never run this code, since we have installed a JiraOsgiContainerManager into Pico.
            // however, let's degrade gracefully if it does happen for some reason. belts and braces-style.
            return getOsgiComponentOfType(clazz, osgiContainerManager);
        }

        return null;
    }

    /**
     * Returns all the components currently inside of Pico which are instances of the given class.
     *
     * @param clazz the class to search for.
     * @return a list containing all the instances of the passed class registered in JIRA's pico container.
     */
    public static <T> List<T> getComponentsOfType(final Class<T> clazz)
    {
        final PicoContainer pico = getInstance().getContainer();
        final List<ComponentAdapter<T>> adapters = pico.getComponentAdapters(clazz);
        if (adapters.isEmpty())
        {
            return Collections.emptyList();
        }
        else
        {
            final List<T> returnList = new ArrayList<T>(adapters.size());
            for (final ComponentAdapter<T> adapter : adapters)
            {
                // remove cast when we go to a Java5 pico
                returnList.add(clazz.cast(adapter.getComponentInstance(pico)));
            }
            return Collections.unmodifiableList(returnList);
        }
    }

    /**
     * Returns all the components currently inside Pico which are instances of the given class, mapping them to their
     * component key.
     *
     * @param iface The class to search for
     * @return a map, mapping the component key, to the instances of the clas registered in JIRA's pico container.
     */
    public static <T> Map<String, T> getComponentsOfTypeMap(final Class<T> iface)
    {
        final PicoContainer picoContainer = getInstance().getContainer();
        final List<ComponentAdapter<T>> componentAdaptersOfType = picoContainer.getComponentAdapters(iface);

        final Map<String, T> implementations = new HashMap<String, T>();
        for (final ComponentAdapter<T> componentAdapter : componentAdaptersOfType)
        {
            final T componentInstance = iface.cast(componentAdapter.getComponentInstance(picoContainer));
            implementations.put(String.valueOf(componentAdapter.getComponentKey()), componentInstance);
        }
        return Collections.unmodifiableMap(implementations);
    }

    private static class PluginSystem
    {
        enum State
        {
            NOT_STARTED,
            STARTED
        }

        volatile State state = State.NOT_STARTED;

        /**
         * Retrieves and returns the plugin system's lifecycle instance
         *
         * @return plugin lifecycle
         */
        public PluginSystemLifecycle getPluginSystemLifecycle()
        {
            return getComponentInstanceOfType(PluginSystemLifecycle.class);
        }

        void start()
        {
            if (state != State.NOT_STARTED)
            {
                return;
            }
            // start plugin manager first manually so that the component plugins can be startable themselves.
            final PluginSystemLifecycle pluginSystemLifecycle = getPluginSystemLifecycle();
            if (pluginSystemLifecycle instanceof Startable)
            {
                final Startable startablePluginManager = (Startable) pluginSystemLifecycle;
                try
                {
                    startablePluginManager.start();
                }
                catch (final NotificationException ex)
                {
                    // This is just a wrapper from the Plugin Events system - lets get the underlying cause.
                    final Throwable cause = ex.getCause();
                    throw new InfrastructureException("Error occurred while starting Plugin Manager. " + cause.getMessage(), cause);
                }
                catch (final Exception e)
                {
                    throw new InfrastructureException("Error occurred while starting Plugin Manager. " + e.getMessage(), e);
                }
            }
            else
            {
                throw new InfrastructureException("PluginManager does not implement startable anymore?!");
            }
            state = State.STARTED;
        }

        public void shutdown()
        {
            if (state != State.STARTED)
            {
                return;
            }
            try
            {
                getPluginSystemLifecycle().shutdown();
            }
            catch (final RuntimeException ignore)
            {
                // if the plugin system hasn't been started for some reason or has been closed down it will throw an IllegalState
                // we don't care as long as it gets into the not started state  - it also leaks RuntimeExceptions
                // at least log something to help the developer track this down
                log.error("Error occurred while shutting down the component manager.", ignore);
            }
            state = State.NOT_STARTED;
        }
    }

    /**
     * The state of the {@link ComponentManager}.
     *
     * @since 4.0
     */
    public interface State
    {
        /**
         * Has the PICO container initialised.
         *
         * @return true if the PICO container is set up
         */
        boolean isContainerInitialised();

        /**
         * Have the components registered been with PICO including plugin components.
         *
         * @return true if the plugin system has started.
         */
        boolean isPluginSystemStarted();

        /**
         * Have the components registered been with PICO including plugin components.
         *
         * @return true if components have been registered.
         */
        boolean isComponentsRegistered();

        /**
         * Has the {@link ComponentManager} started
         *
         * @return true if the component manager has started.
         */
        boolean isStarted();
    }


    /**
     ===============================================================================
     Before we had generics it made sense to have type safe accessors for the various
     components but these days it is a code smell and I think we should deprecate these
     and the {@link ManagerFactory} - BB mar 2011
     ===============================================================================
     */

    /**
     * Retrieves and returns the issue updater instance NOTE: This method is only used for tests. The fact that it
     * exists means that tests need to be rewritten
     *
     * @return issue updater
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public IssueUpdater getIssueUpdater()
    {
        return getContainer().getComponent(IssueUpdater.class);
    }

    /**
     * Retrieves and returns the Issue Creation Helper Bean instance.
     *
     * @return issue creation helper bean
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public IssueCreationHelperBean getIssueCreationHelperBean()
    {
        return getContainer().getComponent(IssueCreationHelperBean.class);
    }

    /**
     * Retrieves and returns the file icon bean instance
     *
     * @return file icon bean
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public FileIconBean getFileIconBean()
    {
        return getContainer().getComponent(FileIconBean.class);
    }

    /**
     * Retrieves and returns the issue manager instance
     *
     * @return issue manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public IssueManager getIssueManager()
    {
        return getComponentInstanceOfType(IssueManager.class);
    }

    /**
     * Retrieves and returns the workflow manager instance
     *
     * @return workflow manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public WorkflowManager getWorkflowManager()
    {
        return getContainer().getComponent(WorkflowManager.class);
    }

    /**
     * Retrieves and returns the worklog manager instance
     *
     * @return worklog manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public WorklogManager getWorklogManager()
    {
        return getContainer().getComponent(WorklogManager.class);
    }

    /**
     * Get an IssueFactory instance, particularly useful for obtaining {@link Issue} from
     *
     * @return IssueFactory
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    @SuppressWarnings ( { "JavadocReference" })
    public IssueFactory getIssueFactory()
    {
        return getContainer().getComponent(IssueFactory.class);
    }

    /**
     * Retrieves and returns the project factory instance
     *
     * @return project factory
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public ProjectFactory getProjectFactory()
    {
        return getContainer().getComponent(ProjectFactory.class);
    }

    /**
     * Retrieves and returns the constants manager
     *
     * @return constants manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public ConstantsManager getConstantsManager()
    {
        return getContainer().getComponent(ConstantsManager.class);
    }

    /**
     * Retrieves and returns the field manager instance
     *
     * @return field manager
     *
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getFieldManager()} instead. Since v4.4.
     */
    public FieldManager getFieldManager()
    {
        return getContainer().getComponent(FieldManager.class);
    }

    /**
     * Retrieves and returns the custom field manager
     *
     * @return custom field manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public CustomFieldManager getCustomFieldManager()
    {
        return getContainer().getComponent(CustomFieldManager.class);
    }

    /**
     * Retrieves and returns the issue type scheme manager instance
     *
     * @return issue type scheme manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public IssueTypeSchemeManager getIssueTypeSchemeManager()
    {
        return getContainer().getComponent(IssueTypeSchemeManager.class);
    }

    /**
     * Retrieves and returns the issue type screen scheme manager instance
     *
     * @return issue type screen scheme manager
     *
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getIssueTypeScreenSchemeManager()} instead. Since v5.0.
     */
    public IssueTypeScreenSchemeManager getIssueTypeScreenSchemeManager()
    {
        return getContainer().getComponent(IssueTypeScreenSchemeManager.class);
    }

    /**
     * Retrieves and returns the subtask manager instance
     *
     * @return subtask manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public SubTaskManager getSubTaskManager()
    {
        return getContainer().getComponent(SubTaskManager.class);
    }

    /**
     * Retrieves and returns the issuel link manager instance NOTE: Needed especially for custom workflow conditions
     * that check an issue's links for progression.
     *
     * @return issuel link manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public IssueLinkManager getIssueLinkManager()
    {
        return getContainer().getComponent(IssueLinkManager.class);
    }

    /**
     * Retrieves and returns the application properties.
     *
     * @return application properties
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public ApplicationProperties getApplicationProperties()
    {
        return getContainer().getComponent(ApplicationProperties.class);
    }

    /**
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public CrowdService getCrowdService()
    {
        return getContainer().getComponent(CrowdService.class);
    }

    /**
     * Retrieves and returns the permission manager instance
     *
     * @return permission manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public PermissionManager getPermissionManager()
    {
        return getContainer().getComponent(PermissionManager.class);
    }

    /**
     * Retrieves and returns the permission type manager instance
     *
     * @return permission type manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public PermissionTypeManager getPermissionTypeManager()
    {
        return getContainer().getComponent(PermissionTypeManager.class);
    }

    /**
     * Retrieves and returns the field layout manager
     *
     * @return field layout manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public FieldLayoutManager getFieldLayoutManager()
    {
        return getContainer().getComponent(FieldLayoutManager.class);
    }

    /**
     * Retrieves and returns the column layout manager instance
     *
     * @return column layout manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public ColumnLayoutManager getColumnLayoutManager()
    {
        return getContainer().getComponent(ColumnLayoutManager.class);
    }

    /**
     * Retrieves and returns the project manager instance
     *
     * @return project manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public ProjectManager getProjectManager()
    {
        return getContainer().getComponent(ProjectManager.class);
    }

    /**
     * Retrieves and returns the vote manager instance
     *
     * @return vote manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public VoteManager getVoteManager()
    {
        return getContainer().getComponent(VoteManager.class);
    }

    /**
     * Retrieves and returns the JIRA locale utils instance
     *
     * @return JIRA locale utils
     *
     * @deprecated Get LocaleManager/LocaleParser injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public JiraLocaleUtils getJiraLocaleUtils()
    {
        return getContainer().getComponent(JiraLocaleUtils.class);
    }

    /**
     * Retrieves and returns the plugin system's lifecycle instance
     *
     * @return plugin lifecycle
     */
    public PluginSystemLifecycle getPluginSystemLifecycle()
    {
        return pluginSystem.getPluginSystemLifecycle();
    }

    /**
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public PluginAccessor getPluginAccessor()
    {
        return getContainer().getComponent(PluginAccessor.class);
    }

    /**
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public PluginEventManager getPluginEventManager()
    {
        return getContainer().getComponent(PluginEventManager.class);
    }

    /**
     * Gets the ComponentClassManager component.
     *
     * @return the ComponentClassManager component.
     *
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getComponentClassManager()} instead. Since v5.0.
     */
    public ComponentClassManager getComponentClassManager()
    {
        return getContainer().getComponent(ComponentClassManager.class);
    }

    /**
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public PluginController getPluginController()
    {
        return getContainer().getComponent(PluginController.class);
    }

    /**
     * Retrieves and returns the upgrade manager instance
     *
     * @return upgrade manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public UpgradeManager getUpgradeManager()
    {
        return getContainer().getComponent(UpgradeManager.class);
    }

    /**
     * Retrieves and returns the renderer manager instance
     *
     * @return renderer manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public RendererManager getRendererManager()
    {
        return getContainer().getComponent(RendererManager.class);
    }

    /**
     * Retrieves and returns the field screen renderer factory instance
     *
     * @return field screen renderer factory
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public FieldScreenRendererFactory getFieldScreenRendererFactory()
    {
        return getContainer().getComponent(FieldScreenRendererFactory.class);
    }

    /**
     * Retrieves and returns the workflow scheme manager instance
     *
     * @return workflow scheme manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public WorkflowSchemeManager getWorkflowSchemeManager()
    {
        return getContainer().getComponent(WorkflowSchemeManager.class);
    }

    /**
     * Retrieves and returns the index lifecycle manager instance
     *
     * @return index lifecycle manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public IndexLifecycleManager getIndexLifecycleManager()
    {
        return getContainer().getComponent(IndexLifecycleManager.class);
    }

    /**
     * Retrieves and returns the issue index manager instance
     *
     * @return index manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public IssueIndexManager getIndexManager()
    {
        return getContainer().getComponent(IssueIndexManager.class);
    }

    /**
     * Retrieves and returns the issue service instance
     *
     * @return issue service
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public IssueService getIssueService()
    {
        return getComponentInstanceOfType(IssueService.class);
    }

    /**
     * Retrieves and returns the index path manager instance
     *
     * @return index path manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public IndexPathManager getIndexPathManager()
    {
        return getComponentInstanceOfType(IndexPathManager.class);
    }

    /**
     * Retrieves and returns the attachment path instance
     *
     * @return attachment path manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public AttachmentPathManager getAttachmentPathManager()
    {
        return getComponentInstanceOfType(AttachmentPathManager.class);
    }

    /**
     * Retrieves and returns the translation manager instance
     *
     * @return translation manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public TranslationManager getTranslationManager()
    {
        return getContainer().getComponent(TranslationManager.class);
    }

    /**
     * Retrieves and returns the JIRA authentication context instance
     *
     * @return JIRA authentication context
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public JiraAuthenticationContext getJiraAuthenticationContext()
    {
        return getContainer().getComponent(JiraAuthenticationContext.class);
    }

    /**
     * Retrieves and returns the watcher manager instance
     *
     * @return watcher manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public WatcherManager getWatcherManager()
    {
        return getContainer().getComponent(WatcherManager.class);
    }

    /**
     * Retrieves and returns the search provider instance
     *
     * @return search provider
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public SearchService getSearchService()
    {
        return getContainer().getComponent(SearchService.class);
    }

    /**
     * Retrieves and returns the search provider instance
     *
     * @return search provider
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public SearchProvider getSearchProvider()
    {
        return getContainer().getComponent(SearchProvider.class);
    }

    /**
     * Retrieves and returns the search request manager instance
     *
     * @return search request manager
     * @deprecated v3.13 please use {@link SearchRequestService}
     */
    @Deprecated
    public SearchRequestManager getSearchRequestManager()
    {
        return getContainer().getComponent(SearchRequestManager.class);
    }

    /**
     * Retrieves the search request service
     *
     * @return search request service
     * @since v3.13
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public SearchRequestService getSearchRequestService()
    {
        return getContainer().getComponent(SearchRequestService.class);
    }

    /**
     * Retrieves the search request admin service
     *
     * @return search request service
     * @since v3.13
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public SearchRequestAdminService getSearchRequestAdminService()
    {
        return getContainer().getComponent(SearchRequestAdminService.class);
    }

    /**
     * Retrieves a {@link SearchRequestFactory}
     *
     * @return search request factory
     * @since v3.13
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public SearchRequestFactory getSearchRequestFactory()
    {
        return getContainer().getComponent(SearchRequestFactory.class);
    }

    /**
     * Retrieves and returns the field screen manager instance
     *
     * @return field screen manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public FieldScreenManager getFieldScreenManager()
    {
        return getContainer().getComponent(FieldScreenManager.class);
    }

    /**
     * Retrieves and returns the field screen scheme manager instance
     *
     * @return field screen scheme manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public FieldScreenSchemeManager getFieldScreenSchemeManager()
    {
        return getContainer().getComponent(FieldScreenSchemeManager.class);
    }

    /**
     * Retrieves and returns the scheme permissions instance
     *
     * @return scheme permissions
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public SchemePermissions getSchemePermissions()
    {
        return getContainer().getComponent(SchemePermissions.class);
    }

    /**
     * Retrieves and returns the mail server manager instance
     *
     * @return mail server manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public MailServerManager getMailServerManager()
    {
        return getContainer().getComponent(MailServerManager.class);
    }

    /**
     * Retrieves and returns teh event type manager instance
     *
     * @return event type manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public EventTypeManager getEventTypeManager()
    {
        return getContainer().getComponent(EventTypeManager.class);
    }

    /**
     * Retrieves and returns the template manager instance
     *
     * @return template manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public TemplateManager getTemplateManager()
    {
        return getContainer().getComponent(TemplateManager.class);
    }

    /**
     * Retrieves and returns the user util instance
     *
     * @return user util
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public UserUtil getUserUtil()
    {
        return getContainer().getComponent(UserUtil.class);
    }

    /**
     * Retrieves and returns the assignee resolver instance
     *
     * @return assignee resolver
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public AssigneeResolver getAssigneeResolver()
    {
        return getContainer().getComponent(AssigneeResolver.class);
    }

    /**
     * Retrieves and returns the mailing list compiler instance
     *
     * @return mailing list compiler
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public MailingListCompiler getMailingListCompiler()
    {
        return getContainer().getComponent(MailingListCompiler.class);
    }

    /**
     * Retrieves and returns the subscription mail queue item factory instance
     *
     * @return subscription mail queue item factory
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public SubscriptionMailQueueItemFactory getSubscriptionMailQueueItemFactory()
    {
        return getContainer().getComponent(SubscriptionMailQueueItemFactory.class);
    }

    /**
     * Retrieves and returns the velocity manager instance
     *
     * @return velocity manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public VelocityManager getVelocityManager()
    {
        return getContainer().getComponent(VelocityManager.class);
    }

    /**
     * Retrieves and returns the comment manager instance
     *
     * @return comment manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public CommentManager getCommentManager()
    {
        return getContainer().getComponent(CommentManager.class);
    }

    /**
     * Create a new UpgradeManager. This may be needed if more upgrade tasks are added, or if the license has been
     * changed.
     */
    public void refreshUpgradeManager()
    {
        // this is very ugly. We should find a way to get Pico to reload its classes.
        container.getPicoContainer().removeComponent(UpgradeManager.class);
        container.getPicoContainer().addComponent(UpgradeManager.class, UpgradeManagerImpl.class);
    }

    /**
     * Retrieves and returns the project component manager instance
     *
     * @return project component manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public ProjectComponentManager getProjectComponentManager()
    {
        return getContainer().getComponent(ProjectComponentManager.class);
    }

    /**
     * Retrieves and returns the {@link ChangeHistoryManager} manager instance
     *
     * @return ChangeHistoryManager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public ChangeHistoryManager getChangeHistoryManager()
    {
        return getContainer().getComponent(ChangeHistoryManager.class);
    }

    /**
     * Retrieves and returns the permission context factory instance
     *
     * @return permission context factory
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public PermissionContextFactory getPermissionContextFactory()
    {
        return getContainer().getComponent(PermissionContextFactory.class);
    }

    /**
     * Retrieves and returns the user preferences manager instance
     *
     * @return user preferences manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public UserPreferencesManager getUserPreferencesManager()
    {
        return getContainer().getComponent(UserPreferencesManager.class);
    }

    /**
     * Retrieves and returns the user preferences manager instance
     *
     * @return user preferences manager
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public UserPropertyManager getUserPropertyManager()
    {
        return getContainer().getComponent(UserPropertyManager.class);
    }

    /**
     * Retrieves and returns the JIRA duration utils instance
     *
     * @return JIRA duration utils
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public JiraDurationUtils getJiraDurationUtils()
    {
        return getContainer().getComponent(JiraDurationUtils.class);
    }

    /**
     * Returns the {@link com.atlassian.jira.task.TaskManager}
     *
     * @return the {@link com.atlassian.jira.task.TaskManager}
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public TaskManager getTaskManager()
    {
        return getContainer().getComponent(TaskManager.class);
    }

    /**
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public TrustedApplicationsManager getTrustedApplicationsManager()
    {
        return getContainer().getComponent(TrustedApplicationsManager.class);
    }

    /**
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public OutlookDateManager getOutlookDateManager()
    {
        return getContainer().getComponent(OutlookDateManager.class);
    }

    /**
     * @return the {@link com.atlassian.jira.bc.portal.PortalPageService}
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public PortalPageService getPortalPageService()
    {
        return getContainer().getComponent(PortalPageService.class);
    }

    /**
     * @return the {@link com.atlassian.jira.portal.PortalPageManager}
     *
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public PortalPageManager getPortalPageManager()
    {
        return getContainer().getComponent(PortalPageManager.class);
    }

    /**
     * @deprecated Get this component injected in your constructor or use {@link com.atlassian.jira.component.ComponentAccessor} for static access instead. Since v5.2.
     */
    public AvatarManager getAvatarManager()
    {
        return getContainer().getComponent(AvatarManager.class);
    }

    /**
     * Looks up a service from the OsgiContainerManager. This method should be avoided since it creates and closes a
     * new ServiceTracker each time it is called.
     *
     * @see JiraOsgiContainerManager#getOsgiComponentOfType(Class)
     */
    private static <T> T getOsgiComponentOfType(@Nonnull Class<T> clazz, @Nonnull OsgiContainerManager osgiContainerManager)
    {
        if (log.isDebugEnabled())
        {
            log.debug(String.format("Using slow getOsgiComponentOfType() to get '%s'. COMPONENT MANAGER. Y U NO JiraOsgiContainerManager!?", clazz.getName()), new Throwable());
        }

        ServiceTracker serviceTracker = osgiContainerManager.getServiceTracker(clazz.getName());
        if (serviceTracker != null)
        {
            try
            {
                return clazz.cast(serviceTracker.getService());
            }
            finally
            {
                serviceTracker.close();
            }
        }

        return null;
    }
}
