package com.atlassian.jira.plugin;

/**
 * Serves as the basis of a tab panel that can be loaded in to an {@link com.atlassian.jira.web.action.AbstractPluggableTabPanelAction }
 *
 * @since v6.1
 */
public interface PluggableTabPanelModuleDescriptor<T> extends JiraResourcedModuleDescriptor<T>, OrderableModuleDescriptor
{
}
