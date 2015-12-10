package com.atlassian.jira.startup;

import com.atlassian.fugue.Option;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.JiraFailedPluginTracker;
import com.atlassian.plugin.PluginArtifact;
import com.google.common.collect.Lists;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

/**
 * Called at the end of JITA to report on what plugins have failed to load
 *
 * @since v6.0
 */
public class FailedPluginsLauncher implements JiraLauncher
{
    @Override
    public void start()
    {

        JiraFailedPluginTracker failedPluginTracker = ComponentAccessor.getComponent(JiraFailedPluginTracker.class);
        ArrayList<JiraFailedPluginTracker.PluginInfo> failedPlugins = Lists.newArrayList(failedPluginTracker.getFailedPlugins());
        if (!failedPlugins.isEmpty())
        {
            StringBuilder sb = new StringBuilder();
            sb.append(format(""
                    + "___ FAILED PLUGIN REPORT _____________________"
                    + "\n"));

            int size = failedPlugins.size();
            sb.append(format("\n%d %s failed to load during JIRA startup.", size, size == 1 ? "plugin" : "plugins"));
            for (JiraFailedPluginTracker.PluginInfo failedPlugin : failedPlugins)
            {
                printFailedPlugin(sb, failedPlugin, "failed to load.");
            }

            List<JiraFailedPluginTracker.PluginInfo> unaccountedForPlugins = Lists.newArrayList(failedPluginTracker.getUnaccountedForPlugins());
            size = unaccountedForPlugins.size();
            if (!unaccountedForPlugins.isEmpty())
            {
                sb.append(format("\n%d %s are unaccounted for.", size, size == 1 ? "plugin" : "plugins"));
                sb.append(format("\nUnaccounted for plugins load as artifacts but fail to resolve into full plugins."));
                for (JiraFailedPluginTracker.PluginInfo pluginInfo : unaccountedForPlugins)
                {
                    printFailedPlugin(sb, pluginInfo, "is unaccounted for.");
                }
            }
            sb.append("\n");
            JiraStartupLogger logger = new JiraStartupLogger();
            logger.printMessage(sb.toString(), Level.WARN);
        }
    }

    private void printFailedPlugin(final StringBuilder sb, final JiraFailedPluginTracker.PluginInfo failedPlugin, final String reason)
    {
        Option<PluginArtifact> pluginArtifact = failedPlugin.getPluginArtifact();
        List<String> failureCauses = failedPlugin.getFailureCauses().getOrElse(Collections.<String>emptyList());
        List<JiraFailedPluginTracker.ServiceDependency> missingDeps = failedPlugin.getDependencies();

        sb.append(format("\n\n\t'%s' - '%s'  " + reason, failedPlugin.getPluginKey(), failedPlugin.getPluginName()));
        for (String failureCause : failureCauses)
        {
            sb.append(format("\n\t\t%s", failureCause));
        }
        if (!missingDeps.isEmpty())
        {
            sb.append(format("\n\n\t\tIt has the following missing service dependencies :"));
            for (JiraFailedPluginTracker.ServiceDependency dep : missingDeps)
            {
                sb.append(format("\n\t\t\t %s of type %s", dep.getServiceName(), dep.getServiceClass()));
            }
        }
        if (pluginArtifact.isDefined())
        {
            sb.append(format("\n\n\t\tIt was loaded from %s", pluginArtifact.get().toFile().getPath()));
        }
    }

    @Override
    public void stop()
    {
    }
}
