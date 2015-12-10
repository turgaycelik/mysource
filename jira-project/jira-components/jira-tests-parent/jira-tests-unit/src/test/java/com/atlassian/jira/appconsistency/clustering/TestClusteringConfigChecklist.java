package com.atlassian.jira.appconsistency.clustering;

import java.io.File;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.ClusterNodeProperties;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.startup.JiraClusteringConfigChecklist;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Test the clustering startup checks
 *
 * @since v6.1
 */
public class TestClusteringConfigChecklist
{
    private JiraClusteringConfigChecklist clusterConfigChecklist;

    @Mock
    private ClusterManager mockClusterManager;
    @Mock
    private ClusterNodeProperties mockClusterNodeProperties;
    @Mock
    private JiraHome mockJiraHome;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        when(mockClusterManager.isClustered()).thenReturn(true);
        when(mockClusterNodeProperties.propertyFileExists()).thenReturn(true);
        clusterConfigChecklist = new JiraClusteringConfigChecklist(mockClusterManager, mockClusterNodeProperties, new MockI18nHelper(), mockJiraHome);
    }

    @Test
    public void testStartupOkWithoutPropertiesFile()
    {
        when(mockClusterManager.isClustered()).thenReturn(false);
        when(mockClusterNodeProperties.propertyFileExists()).thenReturn(false);
        assertTrue("Startup is ok (no cluster.properties, so checks skipped)", clusterConfigChecklist.startupOK());
    }

    @Test
    public void testStartupOkWithEverythingInOrder()
    {
        when(mockClusterNodeProperties.getNodeId()).thenReturn("a");
        when(mockClusterNodeProperties.getSharedHome()).thenReturn("b");
        when(mockClusterManager.isClusterLicensed()).thenReturn(true);
        when(mockJiraHome.getLocalHome()).thenReturn(new File("hlocal"));
        when(mockJiraHome.getHome()).thenReturn(new File("b"));

        assertTrue("Startup is fine (all checks pass)", clusterConfigChecklist.startupOK());
    }

    @Test
    public void testNoNodeId()
    {
        when(mockClusterNodeProperties.getSharedHome()).thenReturn("b");
        when(mockClusterManager.isClusterLicensed()).thenReturn(true);
        when(mockJiraHome.getLocalHome()).thenReturn(new File("hlocal"));
        when(mockJiraHome.getHome()).thenReturn(new File("b"));

        assertStartupCheckFails(NodeIdCheck.NAME);
    }

    @Test
    public void testNoSharedHome()
    {
        when(mockClusterNodeProperties.getNodeId()).thenReturn("a");
        when(mockClusterManager.isClusterLicensed()).thenReturn(true);
        assertStartupCheckFails(SharedHomeCheck.NAME, "startup.shared.home.check.missing");
    }

    @Test
    public void testSameLocalAndSharedHome()
    {
        when(mockClusterNodeProperties.getNodeId()).thenReturn("a");
        when(mockClusterManager.isClusterLicensed()).thenReturn(true);
        when(mockClusterNodeProperties.getSharedHome()).thenReturn("hlocal");
        when(mockJiraHome.getLocalHome()).thenReturn(new File("hlocal"));
        when(mockJiraHome.getLocalHomePath()).thenReturn("hlocal");
        when(mockJiraHome.getHome()).thenReturn(new File("hlocal"));

        assertStartupCheckFails(SharedHomeCheck.NAME, "startup.shared.home.check.sameaslocal [hlocal]");
    }

    @Test
    public void testNotLicensed()
    {
        when(mockClusterNodeProperties.getNodeId()).thenReturn("a");
        when(mockClusterNodeProperties.getSharedHome()).thenReturn("b");
        when(mockJiraHome.getLocalHome()).thenReturn(new File("hlocal"));
        when(mockJiraHome.getHome()).thenReturn(new File("b"));

        assertStartupCheckFails(ClusterLicenseCheck.NAME);
    }

    /**
     * Multiple invocations should not result in different results.
     */
    @Test
    public void testMultipleInvocationsSuccess()
    {
        when(mockClusterNodeProperties.getNodeId()).thenReturn("a");
        when(mockClusterNodeProperties.getSharedHome()).thenReturn("b");
        when(mockClusterManager.isClusterLicensed()).thenReturn(true);
        when(mockJiraHome.getLocalHome()).thenReturn(new File("hlocal"));
        when(mockJiraHome.getHome()).thenReturn(new File("b"));

        assertTrue("Startup is fine (all checks pass)", clusterConfigChecklist.startupOK());
        assertTrue("Startup is fine (all checks pass)", clusterConfigChecklist.startupOK());
    }

    /**
     * Multiple invocations should not result in different results.
     */
    @Test
    public void testMultipleInvocationsFailure()
    {
        when(mockClusterNodeProperties.getSharedHome()).thenReturn("b");
        when(mockClusterManager.isClusterLicensed()).thenReturn(true);
        when(mockJiraHome.getLocalHome()).thenReturn(new File("hlocal"));
        when(mockJiraHome.getHome()).thenReturn(new File("b"));

        assertStartupCheckFails(NodeIdCheck.NAME);
        assertStartupCheckFails(NodeIdCheck.NAME);
    }

    private void assertStartupCheckFails(final String expectedName)
    {
        assertFalse("Startup fails with expected reason", clusterConfigChecklist.startupOK());
        assertEquals("We return the right failed startCheck", expectedName, clusterConfigChecklist.getFailedStartupChecks().get(0).getName());
    }

    private void assertStartupCheckFails(final String expectedName, final String expectedDescription)
    {
        assertFalse("Startup fails with expected reason", clusterConfigChecklist.startupOK());
        assertEquals("We return the right failed startCheck", expectedName, clusterConfigChecklist.getFailedStartupChecks().get(0).getName());
        assertEquals("Wrong fault description.", expectedDescription, clusterConfigChecklist.getFailedStartupChecks().get(0).getFaultDescription());
    }

}
