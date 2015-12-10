package com.atlassian.sal.jira.upgrade;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import com.atlassian.sal.core.upgrade.DefaultPluginUpgradeManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JiraPluginUpgradeManager extends DefaultPluginUpgradeManager
{
    static final String SAL_PLUGIN_KEY = "com.atlassian.sal.jira";
    private static final String SAL_UPGRADE_LOCK_NAME = SAL_PLUGIN_KEY + ".upgrade";
    private final ClusterLockService clusterLockService;

    public JiraPluginUpgradeManager(List<PluginUpgradeTask> upgradeTasks, TransactionTemplate transactionTemplate,
            PluginAccessor pluginAccessor, PluginSettingsFactory pluginSettingsFactory)
    {
        super(upgradeTasks, transactionTemplate, pluginAccessor, pluginSettingsFactory, ComponentAccessor.getPluginEventManager());
        clusterLockService = ComponentAccessor.getComponent(ClusterLockService.class);
    }

    protected Map<String, List<PluginUpgradeTask>> getUpgradeTasks()
    {
        Map<String, List<PluginUpgradeTask>> upgradeTasks = super.getUpgradeTasks();
        List<PluginUpgradeTask> salTasks = upgradeTasks.get(SAL_PLUGIN_KEY);
        if (salTasks == null)
        {
            return upgradeTasks;
        }
        else
        {
            Map<String, List<PluginUpgradeTask>> sortedUpgradeTasks = new LinkedHashMap<String, List<PluginUpgradeTask>>();
            sortedUpgradeTasks.put(SAL_PLUGIN_KEY, salTasks);
            // According to the LinkedHashMap contract, when the sal tasks are reinserted into the map, they will still
            // stay in first position, so the method below is safe.
            sortedUpgradeTasks.putAll(upgradeTasks);
            return sortedUpgradeTasks;
        }
    }

    @Override
    public List<Message> upgradeInternal()
    {
        ClusterLock lock = clusterLockService.getLockForName(SAL_UPGRADE_LOCK_NAME);
        lock.lock();
        try
        {
            return super.upgradeInternal();
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public List<Message> upgradeInternal(Plugin plugin)
    {
        ClusterLock lock = clusterLockService.getLockForName(SAL_UPGRADE_LOCK_NAME);
        lock.lock();
        try
        {
            return super.upgradeInternal(plugin);
        }
        finally
        {
            lock.unlock();
        }
    }
}
