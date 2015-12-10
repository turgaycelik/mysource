package com.atlassian.jira.index.ha;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.atlassian.beehive.db.ClusterNodeHeartbeatService;
import com.atlassian.beehive.db.spi.ClusterLockDao;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.util.I18nHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.matchers.FileMatchers.exists;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestDisasterRecoveryLauncher
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private ApplicationProperties applicationProperties;

    @Mock
    @AvailableInContainer
    private JiraHome jiraHome;

    @Mock
    @AvailableInContainer
    private ClusterLockDao clusterLockDao;

    @Mock
    @AvailableInContainer
    private ClusterNodeHeartbeatService heartbeatService;

    @Mock
    @AvailableInContainer
    private I18nHelper i18nHelper;

    private DisasterRecoveryLauncher launcher;
    private File jiraHomeDir;
    private File jiraImportDir;

    @Before
    public void setup()
    {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        jiraHomeDir = new File(baseDir, "jira-home-" + System.currentTimeMillis());
        jiraImportDir = new File(baseDir, "jira-import-" + System.currentTimeMillis());
        new File(jiraImportDir, "indexsnapshots").mkdirs();
        when(jiraHome.getHome()).thenReturn(jiraHomeDir);
        when(jiraHome.getImportDirectory()).thenReturn(jiraImportDir);
        launcher = new DisasterRecoveryLauncher();
    }

    @After
    public void tearDown()
    {
        deleteQuietly(jiraHomeDir);
        deleteQuietly(jiraImportDir);
    }

    @Test
    public void testGetRecoveryModes() throws Exception
    {
        assertThat(launcher.getRecoveryMode(), is(DisasterRecoveryLauncher.RecoveryMode.PRIMARY));
        when(applicationProperties.getOption("disaster.recovery")).thenReturn(true);
        assertThat(launcher.getRecoveryMode(), is(DisasterRecoveryLauncher.RecoveryMode.COLD));
        launcher.start();
        assertThat(launcher.getRecoveryMode(), is(DisasterRecoveryLauncher.RecoveryMode.SECONDARY));
    }

    @Test
    public void testSnapshotsMovedOnColdStart() throws Exception
    {
        // DRL validates the zip before attempting to restore. This is hard to mock
        // so for the moment just verify that the snapshot is moved from the import dir
        // to the archive dir.
        when(applicationProperties.getOption("disaster.recovery")).thenReturn(true);
        final File indexImportDir = new File(jiraImportDir, "indexsnapshots");
        indexImportDir.mkdirs();
        new File(indexImportDir, "IndexSnapshot_test.zip").createNewFile();
        launcher.start();
        assertThat(jiraImportDir.list(), emptyArray());
        final File archiveDir = new File(jiraHomeDir, "old");
        final File archivedSnapshots = new File(archiveDir, "indexsnapshots");
        final File snapshotFile = new File(archivedSnapshots, "IndexSnapshot_test.zip");
        assertThat(snapshotFile, exists());
    }

    @Test
    public void testLocksClearedOnColdStart() throws Exception
    {
        when(applicationProperties.getOption("disaster.recovery")).thenReturn(true);
        final String myNodeId = "NodeMe";
        final List<String> primaryNodes = Arrays.asList("Node1", "Node2", "Node3", myNodeId);
        when(heartbeatService.findLiveNodes()).thenReturn(primaryNodes);
        when(heartbeatService.getNodeId()).thenReturn(myNodeId);
        launcher.earlyStart();
        verify(clusterLockDao).deleteLocksHeldByNode("Node1");
        verify(clusterLockDao).deleteLocksHeldByNode("Node2");
        verify(clusterLockDao).deleteLocksHeldByNode("Node3");
    }

}