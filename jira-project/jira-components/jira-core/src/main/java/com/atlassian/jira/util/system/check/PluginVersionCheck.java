package com.atlassian.jira.util.system.check;

import com.atlassian.jira.startup.JiraStartupLogger;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.system.VersionNumber;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Level;

import javax.servlet.ServletContext;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Checks a certain subset of plugins installed, to ensure that their minVersion is greater or equal to the current
 * application version.  Currently this subset is hard-coded here, however in the future this list should probably be
 * retrieved from a plugin repository of some sort.
 *
 * @since v4.0
 */
public class PluginVersionCheck
{
    private final JiraStartupLogger startupLog = new JiraStartupLogger();
    private final PluginAccessor pluginAccessor;
    private final BuildUtilsInfo buildUtilsInfo;
    private static final String PAC_LINK = "https://plugins.atlassian.com/search/by/jira";

    //defines the plugins to check and what type of check to carry out against them!
    private final Map<String, PluginPredicate<Plugin>> pluginsToCheck = new LinkedHashMap<String, PluginPredicate<Plugin>>();

    public PluginVersionCheck(PluginAccessor pluginAccessor, final BuildUtilsInfo buildUtilsInfo)
    {
        this.pluginAccessor = notNull("pluginAccessor", pluginAccessor);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);

        //init the list of plugins to check
        pluginsToCheck.put("com.atlassian.jira.ext.charting", new MinVersionCheckPredicate(buildUtilsInfo));
        pluginsToCheck.put("com.atlassian.jira.plugin.labels", new BlackListPredicate());
    }

    public void check(final ServletContext context)
    {
        final Set<Plugin> outdatedPlugins = new HashSet<Plugin>();
        for (Map.Entry<String, PluginPredicate<Plugin>> pluginEntry : pluginsToCheck.entrySet())
        {
            final Plugin plugin = pluginAccessor.getPlugin(pluginEntry.getKey());
            //looks like the plugin isn't installed.
            if (plugin == null)
            {
                continue;
            }
            if(pluginEntry.getValue().evaluate(plugin))
            {
                outdatedPlugins.add(plugin);
            }
        }

        //if any outdated plugins were found throw up a warning!
        if (!outdatedPlugins.isEmpty())
        {
            addErrors(context, outdatedPlugins);
        }
    }

    void addErrors(final ServletContext context, final Set<Plugin> outdatedPlugins)
    {
        final String description = "Plugins found that are not compatible with JIRA v" + buildUtilsInfo.getVersion() + ".  "
                + "The following plugins need to be either removed or updated in order for JIRA to start successfully:";

        final StringBuilder pluginNames = new StringBuilder();
        for (Plugin outdatedPlugin : outdatedPlugins)
        {
            pluginNames.append(pluginsToCheck.get(outdatedPlugin.getKey()).getErrorMessage(outdatedPlugin)).append("\n");
        }

        final String cacMessage = "Please visit " + PAC_LINK + " to download the latest versions of these plugins.";

        final List<String> messages = CollectionBuilder.newBuilder("", description, "", pluginNames.toString().trim(), "", cacMessage, "").asList();
        startupLog.printMessage(messages, Level.ERROR);

        final Event event = new Event(EventType.get("outdated-plugins-installed"), description + "<p/>" +
                TextUtils.plainTextToHtml(pluginNames.toString()) + "<p/>" + TextUtils.plainTextToHtml(cacMessage),
                EventLevel.get(EventLevel.ERROR));
        final JohnsonEventContainer eventCont = JohnsonEventContainer.get(context);
        if (eventCont != null)
        {
            eventCont.addEvent(event);
        }
    }

    static interface PluginPredicate<T> extends Predicate<T>
    {
        String getErrorMessage(T input);
    }

    /**
     * For plugins that should just be removed.  Returns true for all plugins!
     */
    static class BlackListPredicate implements PluginPredicate<Plugin>
    {
        public boolean evaluate(final Plugin input)
        {
            return true;
        }

        public String getErrorMessage(final Plugin input)
        {
            return "* The plugin '" + input.getName() + "' (" + input.getKey() +
                    ") is no longer compatible with this version of JIRA and needs to be removed. Its functionality has been incorporated into JIRA core.";
        }
    }

    /**
     * Checks that the plugins minVersion is not greater than the application version.  If it is a warning will
     * be shown that the plugin probably needs to be removed and downgraded to a version that works with this
     * version of JIRA.
     */
    static class MinVersionCheckPredicate implements PluginPredicate<Plugin>
    {
        private final BuildUtilsInfo buildUtilsInfo;

        MinVersionCheckPredicate(BuildUtilsInfo buildUtilsInfo)
        {
            this.buildUtilsInfo = buildUtilsInfo;
        }

        public boolean evaluate(final Plugin input)
        {
            final String minVersionStr = Float.toString(input.getPluginInformation().getMinVersion());
            final VersionNumber pluginMinVersion = new VersionNumber(minVersionStr);
            final VersionNumber appVersion = new VersionNumber(getJiraVersionStringWithoutSuffix());
            // now if the plugin's minVersion is greater than to the current app version, this plugin needs to be remove.
            return pluginMinVersion.isGreaterThan(appVersion);
        }

        //removes last part from a version if it's 4.0-SNAPSHOT or 4.0.1-M1 for example.
        private String getJiraVersionStringWithoutSuffix()
        {
            final String version = buildUtilsInfo.getVersion();
            if (version.contains("-"))
            {
                return version.substring(0, version.indexOf("-"));
            }
            return version;
        }

        public String getErrorMessage(final Plugin input)
        {
            final StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("* ").append(input.getName()).append(" (").append(input.getKey()).append(")");
            final float pluginMinVersion = input.getPluginInformation().getMinVersion();
            final String pluginVersion = input.getPluginInformation().getVersion();
            errorMessage.append(" v").append(pluginVersion).append(" - requires JIRA v").append(pluginMinVersion).append(" minimum");
            return errorMessage.toString();
        }
    }
}
