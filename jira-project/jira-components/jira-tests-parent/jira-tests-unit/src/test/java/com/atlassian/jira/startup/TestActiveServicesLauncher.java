package com.atlassian.jira.startup;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.help.HelpUrls;
import com.atlassian.jira.help.MockHelpUrls;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.upgrade.PluginUpgradeLauncher;
import com.atlassian.jira.upgrade.UpgradeLauncher;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @since v6.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestActiveServicesLauncher
{
    @Rule
    public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private ClusterManager mockClusterManager;

    @Mock
    private UpgradeLauncher mockUpgradeLauncher;

    @Mock
    private PluginUpgradeLauncher mockPluginUpgradeLauncher;

    @AvailableInContainer
    private HelpUrls helpUrls = new MockHelpUrls();

    @Test
    public void testStartActiveServices()
    {
        when(mockClusterManager.isActive()).thenReturn(true);
        ActiveServicesLauncher activeServicesLauncher = new ActiveServicesLauncher(mockUpgradeLauncher, mockPluginUpgradeLauncher);
        activeServicesLauncher.start();
        verify(mockUpgradeLauncher).start();
        verify(mockPluginUpgradeLauncher).start();
    }

    @Test
    public void testNoStartPassiveServices()
    {
        when(mockClusterManager.isActive()).thenReturn(false);
        ActiveServicesLauncher activeServicesLauncher = new ActiveServicesLauncher(mockUpgradeLauncher, mockPluginUpgradeLauncher);
        activeServicesLauncher.start();
        verify(mockUpgradeLauncher, never()).start();
        verify(mockPluginUpgradeLauncher, never()).start();
    }

}
