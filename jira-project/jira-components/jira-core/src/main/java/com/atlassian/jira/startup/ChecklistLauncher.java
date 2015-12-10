package com.atlassian.jira.startup;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.servlet.ServletContext;

import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.SystemPropertyKeys;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericDelegator;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static com.atlassian.jira.config.properties.SystemPropertyKeys.JELLY_SYSTEM_PROPERTY;
import static com.atlassian.jira.config.properties.SystemPropertyKeys.JIRA_I18N_RELOADBUNDLES;
import static com.atlassian.jira.config.properties.SystemPropertyKeys.PLUGINS_DISABLE_SPRING_BEAN_MEATADATA_CACHE;
import static com.atlassian.plugin.webresource.DefaultResourceBatchingConfiguration.PLUGIN_WEBRESOURCE_BATCHING_OFF;

/**
 * Listens to Web application startup and shutdown events to check that JIRA is valid to startup, and do whatever clean
 * up may be required on shutdown. <p> When JIRA is not valid to start, then the <code>JiraStartupChecklistFilter</code>
 * will disallow access to JIRA. </p>
 *
 * @see JiraStartupChecklist
 * @see JiraStartupChecklistFilter
 * @since v4.0
 */
public class ChecklistLauncher implements JiraLauncher
{
    private static final Logger log = Logger.getLogger(ChecklistLauncher.class);
    private final JiraProperties jiraSystemProperties;

    public ChecklistLauncher(final JiraProperties jiraSystemProperties)
    {
        this.jiraSystemProperties = jiraSystemProperties;
    }

    public void start()
    {
        try
        {
            setJiraDevMode();
            setupJiraDefaults();
            runStartupChecks(ServletContextProvider.getServletContext());
        }
        catch (RuntimeException rte)
        {
            log.fatal("A RuntimeException occurred during ChecklistLauncher initialisation - " + rte.getMessage() + ".", rte);
            throw rte;
        }
        catch (Error error)
        {
            log.fatal("An Error occurred during ChecklistLauncher initialisation - " + error.getMessage() + ".", error);
            throw error;
        }
    }

    private void setupJiraDefaults()
    {
        // define default values for system variables:
        final Map<String, Object> defaults = ImmutableMap.<String, Object>builder()
                .put(PLUGIN_WEBRESOURCE_BATCHING_OFF, jiraSystemProperties.isDevMode()) // dev mode on -> batching off.
                .put(PLUGINS_DISABLE_SPRING_BEAN_MEATADATA_CACHE, false)
                .build();

        // set them if not set:
        for (final Map.Entry<String, Object> entry : defaults.entrySet())
        {
            if (jiraSystemProperties.getProperty(entry.getKey()) == null)
            {
                jiraSystemProperties.setProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    public void stop()
    {
        JiraStartupChecklist.stop();

        removeAnyJohnsonEvents();

        //cleanup any temporary attachments that for some reason did not get cleaned up when real attachments
        //got created or when session timeouts happened unbinding the {@link com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor}
        deleteTemporaryAttachmentsDir();

        //on shutdown remove the JUL-to-SLF4JBridgeHandler, to make sure we wont get NPE exceptions when
        //Tomcat nulls out static Logger instances on shutdown and SLF4J is trying to pass JUL logging along
        //to log4j.
        SLF4JBridgeHandler.uninstall();

    }

    private void removeAnyJohnsonEvents()
    {
        // this works around a problem in Johnson where there is in fact a static variable of events
        // that can resurrect itself
        final JohnsonEventContainer container = JohnsonEventContainer.get(ServletContextProvider.getServletContext());
        @SuppressWarnings ({ "unchecked" })
        final Collection<Event> events = container.getEvents();
        for (final Event event : events)
        {
            container.removeEvent(event);
        }
    }

    private void runStartupChecks(final ServletContext servletContext)
    {
        JiraStartupLogger.log().info("Running JIRA startup checks.");
        if (JiraStartupChecklist.startupOK())
        {
            JiraStartupLogger.log().info("JIRA pre-database startup checks completed successfully.");
        }
        else
        {
            JiraStartupLogger.log().fatal("Startup check failed. JIRA will be locked.");
            // Lock the DB for ANY of the checks failing.
            GenericDelegator.lock();
            // TODO: Do we really need to raise a Johnson Event? We use our own filter to stop HTTP access.
            // This code was copied from the original "DatabaseCompatibilityEnforcer" Listener
            // Add a Johnson event to stop any other ServletContextListener's from trying to do anything.
            final Event event = new Event(
                    EventType.get(JiraStartupChecklist.getFailedStartupCheck().getName()),
                    JiraStartupChecklist.getFailedStartupCheck().getFaultDescription(),
                    EventLevel.get(EventLevel.ERROR));
            JohnsonEventContainer.get(servletContext).addEvent(event);
        }
    }


    private void setJiraDevMode()
    {
        if (jiraSystemProperties.isDevMode())
        {
            // turn off minification of web resources, available from plugins 2.3 onwards
            setDefault("atlassian.webresource.disable.minification", "true");

            // disable mail
            setDefault("atlassian.mail.senddisabled", "true");
            setDefault("atlassian.mail.fetchdisabled", "true", "atlassian.mail.popdisabled");

            // disable caches
            setDefault("com.atlassian.gadgets.dashboard.ignoreCache", "true");
            setDefault("atlassian.disable.caches", "true");

            // turn on jelly
            setDefault(JELLY_SYSTEM_PROPERTY, "true");

            //jira dev mode should also set atlassian.dev.mode to true if it isn't already set!
            setDefault(SystemPropertyKeys.ATLASSIAN_DEV_MODE, "true");

            setDefault(SystemPropertyKeys.WEBSUDO_IS_DISABLED, "true");

            // turn on i18n reload
            setDefault(JIRA_I18N_RELOADBUNDLES, "true");
        }
    }

    private void setDefault(final String key, final String value, final String... relatedKeys)
    {
        if (jiraSystemProperties.getProperty(key) != null)
        {
            log.debug("Trying to set already defined system property '" + key + "' to '" + value + "' because development mode is on. Leaving as current value '" + jiraSystemProperties.getProperty(key) + "'.");
            return;
        }
        else if (relatedKeys != null)
        {
            for (final String relatedKey : relatedKeys)
            {
                final String sysVal = jiraSystemProperties.getProperty(relatedKey);
                if (sysVal != null)
                {
                    final String mesg = "Trying to set system property '" + key + "' to '" + value + "' because development mode is on. But related property '" + relatedKey + "' is already set to '" + sysVal + "'. So not setting.";
                    if (sysVal.equals(value))
                    {
                        log.debug(mesg);
                    }
                    else
                    {
                        log.warn(mesg);
                    }
                    return;
                }
            }
        }
        log.info("Setting system property '" + key + "' to '" + value + "' for development mode.");
        jiraSystemProperties.setProperty(key, value);
    }

    /**
     * Deletes the temporary attachments directory.
     */
    private void deleteTemporaryAttachmentsDir()
    {
        try
        {
            final File attachmentDirectory = AttachmentUtils.getTemporaryAttachmentDirectory();
            try
            {
                FileUtils.deleteDirectory(attachmentDirectory);
            }
            catch (IOException ioException)
            {
                log.warn("Warning: (" + ioException.getMessage() + ") deleting temporary attachments directory '" + attachmentDirectory + "' on shutdown. Ignoring since this is not required.", ioException);
            }
        }
        catch (Exception tempAttachmentsDirectoryNotRemovedException)
        {
            log.warn("Couldn't delete the temporary attachments directory.", tempAttachmentsDirectoryNotRemovedException);
        }
    }
}
