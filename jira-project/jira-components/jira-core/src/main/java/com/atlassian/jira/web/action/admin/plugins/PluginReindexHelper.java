package com.atlassian.jira.web.action.admin.plugins;

/**
 * Provides help with some business logic around the management of plugins and the requirement for reindex.
 *
 * @since v4.0
 */
public interface PluginReindexHelper
{
    /**
     * @param moduleKey the complete module key e.g. <code>com.atlassian.jira.plugin.system.customfieldtypes:numberrange</code>
     * @return true if module is a custom field type or custom field searcher
     */
    boolean doesEnablingPluginModuleRequireMessage(String moduleKey);

    /**
     * @param pluginKey the plugin key
     * @return true if plugin contains a module which is a custom field type or custom field searcher
     */
    boolean doesEnablingPluginRequireMessage(String pluginKey);
}
