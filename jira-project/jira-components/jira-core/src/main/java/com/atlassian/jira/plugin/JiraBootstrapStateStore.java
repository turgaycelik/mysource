package com.atlassian.jira.plugin;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.plugin.manager.DefaultPluginPersistentState;
import com.atlassian.plugin.manager.PluginPersistentState;
import com.atlassian.plugin.manager.PluginPersistentStateStore;

import java.util.Map;


/**
 * Bootstrap plugin system should only load default state
 * @since v6.1
 */
public class JiraBootstrapStateStore implements PluginPersistentStateStore
{
    public void save(PluginPersistentState state)
    {
        // do nothing
    }

    public PluginPersistentState load()
    {
        return DefaultPluginPersistentState.Builder.create().toState();
    }
}
