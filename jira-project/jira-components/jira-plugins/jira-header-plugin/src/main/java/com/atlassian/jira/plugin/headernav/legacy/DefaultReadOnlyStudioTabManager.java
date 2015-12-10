package com.atlassian.jira.plugin.headernav.legacy;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * This code is copied from the jira-ondemand-theme-plugin
 */
@Deprecated
@Component
public class DefaultReadOnlyStudioTabManager implements ReadOnlyStudioTabManager
{
    private static final String STUDIO_TABS_PREFIX = "studio.tabs.";

    private static final Logger log = LoggerFactory.getLogger(DefaultReadOnlyStudioTabManager.class);
    private final PluginSettingsFactory pluginSettingsFactory;

    @Autowired
    public DefaultReadOnlyStudioTabManager(PluginSettingsFactory pluginSettingsFactory)
    {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    /**
     * Get all tabs
     */
    @Override
    public List<StudioTab> getAllTabs(String projectKey)
    {
        List<StudioTab> studioTabs = collectTabs(getStudioTabSettings(projectKey));

        return studioTabs;
    }

    interface StudioTabsVisitor
    {
        void visit(PluginSettings studioTabSettings, int position, StudioTab studioTab);
    }

    private StudioTab getTab(PluginSettings studioTabSettings, int position)
    {
        Object setting = studioTabSettings.get(Integer.toString(position));
        if (setting == null)
        {
            return null;
        }
        StudioTab studioTab = StudioTab.fromString(setting.toString());
        if (studioTab == null)
        {
            log.warn("Studio tabs seem corrupted. (" + position + ", " + setting + ").");
        }
        return studioTab;
    }

    /**
     * Visits studio tabs
     *
     * @param studioTabSettings The settings for the studio tabs to visit
     * @param visitor The visitor to visit the settings
     */
    private void visitStudioTabs(final PluginSettings studioTabSettings, StudioTabsVisitor visitor)
    {
        for (int i = 0; ; i++)
        {
            final StudioTab studioTab = getTab(studioTabSettings, i);
            if (studioTab == null)
            {
                break;
            }
            else
            {
                visitor.visit(studioTabSettings, i, studioTab);
            }
        }
    }

    /**
     * Returns the studio tabs for the given project Note: Storing and reading settings involve DB operations.
     *
     * @param projectKey The project key to get the settings for.
     * @return The plugin settings
     */
    private PluginSettings getStudioTabSettings(final String projectKey)
    {
        final PluginSettings studioTabSettings;
        final String studioTabsPrefix;
        if (projectKey == null)
        {
            studioTabsPrefix = STUDIO_TABS_PREFIX;
            studioTabSettings = pluginSettingsFactory.createGlobalSettings();
        }
        else if (DEFAULT_TABS_KEY.equals(projectKey))
        {
            studioTabSettings = pluginSettingsFactory.createGlobalSettings();
            studioTabsPrefix = STUDIO_TABS_PREFIX + "default.";
        }
        else
        {
            PluginSettings ps;
            try
            {
                ps = pluginSettingsFactory.createSettingsForKey(projectKey);
            }
            catch (IllegalArgumentException iae)
            {
                ps = pluginSettingsFactory.createGlobalSettings();
            }
            studioTabSettings = ps;
            studioTabsPrefix = STUDIO_TABS_PREFIX;
        }

        return new PluginSettings()
        {
            public Object remove(String key)
            {
                return studioTabSettings.remove(studioTabsPrefix + key);
            }

            public Object put(String key, Object value)
            {
                return studioTabSettings.put(studioTabsPrefix + key, value);
            }

            public Object get(String key)
            {
                return studioTabSettings.get(studioTabsPrefix + key);
            }
        };
    }


    private List<StudioTab> collectTabs(PluginSettings studioTabSettings)
    {
        final List<StudioTab> studioTabs = new ArrayList<com.atlassian.jira.plugin.headernav.legacy.StudioTab>();
        visitStudioTabs(studioTabSettings, new StudioTabsVisitor()
        {
            public void visit(PluginSettings studioTabSettings, int position, com.atlassian.jira.plugin.headernav.legacy.StudioTab studioTab)
            {
                studioTabs.add(position, studioTab);
            }
        });
        return studioTabs;
    }


}
