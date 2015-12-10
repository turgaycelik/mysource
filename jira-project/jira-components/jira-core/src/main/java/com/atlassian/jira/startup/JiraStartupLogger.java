package com.atlassian.jira.startup;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.security.login.LoginLoggers;
import com.atlassian.jira.cache.HashRegistryCache;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.BuildUtilsInfoImpl;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.modzdetector.Modifications;
import com.atlassian.modzdetector.ModzRegistryException;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.impl.UnloadablePlugin;
import com.atlassian.util.concurrent.ThreadFactories;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.atlassian.jira.config.properties.JiraSystemProperties.isDevMode;

/**
 * This class prints information to the log when JIRA is "about to start" and when it "has started"
 * <p/>
 * This is really here for JIRA admins and support staff to know when JIRA is starting and hence if anything goes wrong
 * during the boot they can tell.  Also is gives a strong indication of when JIRA was "restarted"
 *
 * @since v3.13
 */
public class JiraStartupLogger
{
    private static final Logger log = Logger.getLogger(JiraStartupLogger.class);
    private final BuildUtilsInfo buildUtilsInfo = new BuildUtilsInfoImpl();

    /**
     * Returns the underlying Logger to be used directly.
     *
     * @return the underlying Logger to be used directly.
     */
    public static Logger log()
    {
        return log;
    }

    /**
     * Prints a single message to the log with "stars" around it
     *
     * @param message the message to print
     * @param logLevel the log level
     */
    public void printMessage(final String message, final Level logLevel)
    {
        final FormattedLogMsg logMsg = new FormattedLogMsg(log);
        logMsg.add(message);
        logMsg.printMessage(logLevel, true);
    }

    /**
     * Prints a series of messages to the log with "stars" around them
     *
     * @param messages the messages to print
     * @param logLevel the log level
     */
    public void printMessage(final Collection<String> messages, final Level logLevel)
    {
        final FormattedLogMsg logMsg = new FormattedLogMsg(log);
        logMsg.addAll(messages);
        logMsg.printMessage(logLevel, true);
    }

    /**
     * This prints a message that JIRA is "starting".  Remember this differs from "started" and is also the first output
     * when a JIRA instance is "re-started".
     * <p/>
     * NOTE : It must be very careful not to "bring up" the JIRA world from an unintended class dependency.  At present
     * this code will only get basic JIRA build info, App Server Info and Java JVM Info.  It is veyr careful not to
     * <p/>
     */
    public void printStartingMessage()
    {
        final FormattedLogMsg logMsg = new FormattedLogMsg(log);
        logMsg.add("JIRA starting...");
        logMsg.printMessage(Level.INFO);

        // we really don't expect these to throw exceptions in any way BUT just in case
        try
        {
            final JiraSystemInfo info = new JiraSystemInfo(logMsg, buildUtilsInfo);
            info.obtainBasicInfo(ServletContextProvider.getServletContext());
            if (! isDevMode())
            {
                info.obtainSystemProperties();
            }
        }
        catch (final RuntimeException rte)
        {
            // ok we don't want the logging to stop the servlet context initialisation
            // JIRA can fail later in a more spectacular way
            log.error("Cannnot obtain basic JIRA information", rte);
        }
        catch (final Error e)
        {
            log.error("Cannot obtain basic JIRA information", e);
        }
        logMsg.printMessage(Level.INFO, false);
    }

    /**
     * Once the JIRA database checks out as OK, we can proceed to show more JIRA information
     */
    public void printStartingMessageDatabaseOK()
    {
        final FormattedLogMsg logMsg = new FormattedLogMsg(log);
        try
        {
            logMsg.add("Database configuration OK");

            final JiraSystemInfo info = new JiraSystemInfo(logMsg, buildUtilsInfo);
            info.obtainDatabaseConfigurationInfo();
            logMsg.printMessage(Level.INFO, false);
        }
        catch (final RuntimeException rte)
        {
            // ok we don;t want the logging to stop the servlet context initialisation
            // JIRA can fail later in a more spectacular way
            log.error("Cannot obtain JIRA database information", rte);
        }
    }

    /**
     * Once JIRA has SUCCESSFULLY started, this is shown.  JIRA is now UP and the  world is our oyster!
     */
    public void printStartedMessage()
    {
        FormattedLogMsg logMsg = new FormattedLogMsg(log);
        try
        {
            final JiraSystemInfo info = new JiraSystemInfo(logMsg, buildUtilsInfo);
            info.obtainUserDirectoyInfo();
            info.obtainJiraAppProperties();
            info.obtainDatabaseStatistics();
            info.obtainUpgradeHistory();
            info.obtainFilePaths(ComponentAccessor.getComponent(JiraHome.class));
            info.obtainPlugins();
            info.obtainListeners();
            info.obtainServices();
            info.obtainTrustedApps();
            info.obtainSystemPathProperties();

            if (!isDevMode())
            {
                logMsg.printMessage(Level.INFO, false);
            }
        }
        catch (final RuntimeException rte)
        {
            // ok we don't want the logging to stop the servlet context initialisation
            // JIRA can fail later in a more spectacular way
            log.error("Cannnot obtain JIRA system information", rte);
        }

        logMsg = new FormattedLogMsg(log);

        Collection<UnloadablePlugin> disabledPlugins = new HashSet<UnloadablePlugin>();
        for (Plugin plugin : ComponentAccessor.getPluginAccessor().getPlugins())
        {
            if (plugin instanceof UnloadablePlugin)
            {
                disabledPlugins.add((UnloadablePlugin) plugin);
            }
        }
        if (!disabledPlugins.isEmpty())
        {
            final StringBuilder sb = new StringBuilder().append("The following plugins failed to load:");
            for (UnloadablePlugin disabledPlugin : disabledPlugins)
            {
                sb.append("\n");
                sb.append(disabledPlugin.getName());
                String errorMessage = disabledPlugin.getErrorText();
                if (errorMessage == null || errorMessage.length() == 0)
                {
                    errorMessage = "(no error message)";
                }
                sb.append(": ").append(errorMessage);
            }

            logMsg.add(sb.toString());
            logMsg.printMessage(Level.WARN, true);
        }

        logMsg = new FormattedLogMsg(log);

        final StringBuilder sb = new StringBuilder()
                .append("JIRA ").append(buildUtilsInfo.getVersion())
                .append(" build: ").append(buildUtilsInfo.getCurrentBuildNumber())
                .append(" started. You can now access JIRA through your web browser.");

        logMsg.add(sb.toString());
        logMsg.printMessage(Level.INFO);

        // tell the security log that JIRA has restarted to help people trying to track down security problems
        LoginLoggers.LOGIN_SECURITY_EVENTS.warn(sb);

        final ExecutorService pool = Executors.newSingleThreadExecutor(ThreadFactories.namedThreadFactory("Modification Check", ThreadFactories.Type.DAEMON));
        pool.execute(new ModificationCheck(logMsg));
    }

    private static class ModificationCheck implements Runnable
    {
        private final FormattedLogMsg logMsg;

        public ModificationCheck(FormattedLogMsg logMsg) {this.logMsg = logMsg;}

        public void run()
        {
            try {
                final HashRegistryCache registry = ComponentAccessor.getComponentOfType(HashRegistryCache.class);
                final Modifications modifications = registry.getModifications();

                String modifiedFilesDescription  = "None";
                String removedFilesDescription = "None";

                if (!modifications.modifiedFiles.isEmpty())
                {
                    modifiedFilesDescription = StringUtils.join(modifications.modifiedFiles, ", ");
                }
                if (!modifications.removedFiles.isEmpty())
                {
                    removedFilesDescription = StringUtils.join(modifications.removedFiles, ", ");
                }

                logMsg.outputHeader("Modifications");
                logMsg.outputProperty("Modified Files", modifiedFilesDescription);
                logMsg.outputProperty("Removed Files", removedFilesDescription);
                logMsg.printMessage(Level.INFO, false);
            }
            catch(ModzRegistryException e) {
                logMsg.add("Could not determine modifications: " + e.toString());
                logMsg.printMessage(Level.WARN, false);
            }
        }
    }
}
