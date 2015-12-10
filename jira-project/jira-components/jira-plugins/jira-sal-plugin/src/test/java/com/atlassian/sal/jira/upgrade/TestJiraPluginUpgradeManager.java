package com.atlassian.sal.jira.upgrade;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.impl.DefaultPluginEventManager;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.plugin.PluginAccessor;

import java.util.*;

public class TestJiraPluginUpgradeManager
{
    private final Random rand = new Random();

    private Set<String> uniqueKeys;
    private List<PluginUpgradeTask> upgradeTasks;
    @Mock
    private TransactionTemplate transactionTemplate;
    @Mock
    private PluginAccessor pluginAccessor;
    @Mock
    private PluginSettingsFactory pluginSettingsFactory;

    private JiraPluginUpgradeManager jiraPluginUpgradeManager;

    @Before
    public void setUp()
    {
        initMocks(this);
        final MockComponentWorker componentAccessorWorker = new MockComponentWorker();
        componentAccessorWorker.addMock(PluginEventManager.class, new DefaultPluginEventManager());
        ComponentAccessor.initialiseWorker(componentAccessorWorker);
        upgradeTasks = new ArrayList<PluginUpgradeTask>();
        uniqueKeys = new HashSet<String>();
        jiraPluginUpgradeManager = new JiraPluginUpgradeManager(upgradeTasks, transactionTemplate, pluginAccessor,
            pluginSettingsFactory);
    }

    @Test
    public void testSalIsFirst()
    {
        // Run several times to ensure that the order of the HashMap used in the super method doesn't affect our result.
        for (int i = 0; i < 10; i++)
        {
            addRandomUpgradeTask();
            addRandomUpgradeTask();
            addRandomUpgradeTask();
            addRandomUpgradeTask();
            addRandomUpgradeTask();
            addRandomUpgradeTask();
            addRandomUpgradeTask();
            createUpgradeTask(JiraPluginUpgradeManager.SAL_PLUGIN_KEY);
            assertEquals(JiraPluginUpgradeManager.SAL_PLUGIN_KEY,
                jiraPluginUpgradeManager.getUpgradeTasks().keySet().iterator().next());
            assertEquals(uniqueKeys, jiraPluginUpgradeManager.getUpgradeTasks().keySet());
        }
    }

    private void createUpgradeTask(String key)
    {
        PluginUpgradeTask pluginUpgradeTask = mock(PluginUpgradeTask.class);
        when(pluginUpgradeTask.getPluginKey()).thenReturn(key);
        upgradeTasks.add(pluginUpgradeTask);
        uniqueKeys.add(key);
    }

    private void addRandomUpgradeTask()
    {
        // We use random upgrade task names because whether this test is actually properly testing it or not depends
        // on the order of things stored in a HashMap.  Using random keys should ensure that it is at least sometimes
        // properly testing it.
        createUpgradeTask("key" + (char) ('a' + rand.nextInt(26)) + (char) ('a' + rand.nextInt(26)) +
            (char) ('a' + rand.nextInt(26)) + (char) ('a' + rand.nextInt(26)));

    }

}
