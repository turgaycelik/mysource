package com.atlassian.jira.plugin.profile;

import com.atlassian.jira.plugin.PluggableTabPanelModuleDescriptor;

/**
 * Defines a plugin point for rendering content on the JIRA view profile page.
 *
 * @since v3.12
 */
public interface ViewProfilePanelModuleDescriptor extends PluggableTabPanelModuleDescriptor<ViewProfilePanel>
{
    public static final String TAB_KEY = "tabKey";
    public static final String DEFAULT_TAB_KEY = "admin.common.words.default";
    /**
     * Returns the "tab" key with which this panel is associated. This should be the
     * i18n property key that can be translated into the displayable tab name.
     *
     * @return the i18n key that uniquely identifies he tab and can be used to get the display name.
     */
    public String getTabKey();
}
