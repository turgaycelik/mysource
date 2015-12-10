package com.atlassian.jira.upgrade.tasks;

import java.io.File;

import javax.annotation.Nullable;

import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.plugin.JiraFailedPluginTracker;
import com.atlassian.jira.plugin.PluginPath;
import com.atlassian.jira.startup.FormattedLogMsg;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.plugin.PluginAccessor;

import com.google.common.base.Function;
import com.google.common.io.PatternFilenameFilter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

/**
 * Remove rogue I18n remotable plugin made obsolete by Atlassian Connect changes (JRA-34978)
 */
public class UpgradeTask_Build6200 extends AbstractUpgradeTask
{
    public static final String PLUGIN_KEY = "remotable.plugins.i18n";
    private final EntityEngine entityEngine;
    private final PluginPath pluginPath;
    private final PluginAccessor pluginAccessor;
    private final JiraFailedPluginTracker failedPluginTracker;
    private static final Logger logger = Logger.getLogger(UpgradeTask_Build6200.class);

    public UpgradeTask_Build6200(EntityEngine entityEngine, PluginPath pluginPath, PluginAccessor pluginAccessor, JiraFailedPluginTracker failedPluginTracker)
    {
        super(false);
        this.entityEngine = entityEngine;
        this.pluginPath = pluginPath;
        this.pluginAccessor = pluginAccessor;
        this.failedPluginTracker = failedPluginTracker;
    }

    @Override
    public String getBuildNumber()
    {
        return "6200";
    }

    @Override
    public String getShortDescription()
    {
        return "Remove rogue I18n remotable plugin made obsolete by Atlassian Connect changes (JRA-34978)";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        entityEngine.delete(Delete.from("PluginVersion").whereEqual("key", PLUGIN_KEY));
        PatternFilenameFilter remotableI18nPluginFinder = new PatternFilenameFilter(PLUGIN_KEY + ".*\\.jar");

        final File[] roguePluginFiles = pluginPath.getInstalledPluginsDirectory().listFiles(remotableI18nPluginFinder);
        boolean allDeletedSuccessfully = true;
        for (File file : roguePluginFiles)
        {
            boolean couldDeleteFile = false;
            final String failureMessage = "Failed to delete rogue remotable i18n plugin " + file.getAbsolutePath();
            try
            {
                if (file.canWrite())
                {
                    couldDeleteFile = file.delete();
                }

                allDeletedSuccessfully &= couldDeleteFile;

                if (!couldDeleteFile)
                {
                    logger.error(failureMessage);
                }
                else
                {
                    logger.info("Successfully deleted rogue remotable i18n plugin " + file.getAbsolutePath());
                }
            }
            catch (Exception e)
            {
                logger.error(failureMessage, e);
                allDeletedSuccessfully = false;
            }
        }
        if (roguePluginFiles.length > 0)
        {
            // Add a SUMMARY of above errors
            logFailedPluginExplanation(allDeletedSuccessfully, transform(newArrayList(roguePluginFiles), new Function<File, String>()
            {
                @Override
                public String apply(@Nullable File input)
                {
                    return (input != null) ? input.getAbsolutePath() : "";
                }
            }));
        }
    }

    private void logFailedPluginExplanation(boolean allDeletedSuccessfully, Iterable<String> roguePluginFilenames)
    {
        final FormattedLogMsg logMsg = new FormattedLogMsg(logger);
        String resultSummary;
        if (allDeletedSuccessfully)
        {
            StringBuilder resultReporter = new StringBuilder();
            resultReporter.append("This plugin is obsolete and has been removed from the following locations to avoid any further false alarms:");
            for (String filename: roguePluginFilenames)
            {
                resultReporter.append(" " + filename);
            }
            resultSummary = resultReporter.toString();
        }
        else
        {
            StringBuilder resultReporter = new StringBuilder();
            resultReporter.append("However, not all of the following instances of the plugin could be deleted. Please check JIRA's file permissions and manually delete the remaining plugin artifacts:");
            for (String filename: roguePluginFilenames)
            {
                resultReporter.append(" " + filename);
            }
            resultSummary = resultReporter.toString();
        }

        logMsg.add("___ FALSE ALARM _____________________"
                + "\n\nThe earlier message reporting an error enabling the plugin '" + PLUGIN_KEY
                + "', and the plugin's inclusion in the failed plugin report, can be safely ignored.\n"
                + resultSummary);
        logMsg.printMessage(Level.WARN, true);
    }
}
