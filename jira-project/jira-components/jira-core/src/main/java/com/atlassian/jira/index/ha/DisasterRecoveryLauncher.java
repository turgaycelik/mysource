package com.atlassian.jira.index.ha;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Set;

import com.atlassian.beehive.db.ClusterNodeHeartbeatService;
import com.atlassian.beehive.db.spi.ClusterLockDao;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.startup.JiraLauncher;
import com.atlassian.jira.task.TaskProgressSink;

import com.google.common.collect.Sets;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will check for a disaster recovery scenario and restore the index if required.
 *
 * @since v6.3.5
 */
public class DisasterRecoveryLauncher implements JiraLauncher
{
    public enum RecoveryMode {PRIMARY, SECONDARY, COLD}
    private static final String DR_PROPERTY_KEY = "disaster.recovery";
    private static final String SEGMENT_NAME = "/segments.gen";
    private static final Logger LOG = LoggerFactory.getLogger(DisasterRecoveryLauncher.class);

    /**
     * Returns the recovery mode of the instance. This is one of:
     * <ul>
     * <li>PRIMARY - the main JIRA instance, which has no special DR configuration set</li>
     * <li>SECONDARY - the recovery instance as indicated by the disaster.recovery property being true</li>
     * <li>COLD - a secondary instance on initialisation. This indicates an instance that is currently doing
     *              work to recover after which it will report itself as SECONDARY</li>
     * </ul>
     * @return the active recovery mode
     */
    public RecoveryMode getRecoveryMode()
    {
        if (!ComponentAccessor.getComponent(ApplicationProperties.class).getOption(DR_PROPERTY_KEY))
        {
            return RecoveryMode.PRIMARY;
        }
        if (getSnapshotArchiveDirectory().exists())
        {
            return RecoveryMode.SECONDARY;
        }
        return RecoveryMode.COLD;
    }

    @Override
    public void start()
    {
        RecoveryMode mode = getRecoveryMode();
        if (mode == RecoveryMode.COLD)
        {
            LOG.info("Starting cold instance");
            try
            {
                restoreIndex();
            }
            finally
            {
                // Even if we failed to restore a snapshot, move the
                // snapshots out of the way so we don't blow away any
                // indexing we might do from now on.
                moveOldIndexSnapshots();
            }
        }
        else if (mode == RecoveryMode.SECONDARY)
        {
            LOG.info("Starting secondary instance");
        }
    }

    @Override
    public void stop()
    {
    }

    /**
     * Clearing of the cluster locks needs to happen before plugin load as plugins may
     * take locks during initialisation.
     */
    public void earlyStart()
    {
        RecoveryMode mode = getRecoveryMode();
        if (mode == RecoveryMode.COLD)
        {
            LOG.info("Cold instance early start");
            clearClusterLocks();
        }
    }

    /**
     * Index restore needs to happen on a cold instance after plugins have loaded as
     * we will need to reindex issues in the database that did not make the snapshot.
     */
    private void restoreIndex()
    {
        final File sourceDir = getSnapshotImportDirectory();
        final File[] files = sourceDir.listFiles(IndexUtils.INDEX_SNAPSHOT_FILTER);
        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
        for (File file : files)
        {
            if (validIndexZipFile(file))
            {
                LOG.info("Recovering snapshot file '" + file.getPath() + "'");
                final IndexRecoveryManager recoveryManager = ComponentAccessor.getComponent(IndexRecoveryManager.class);
                try
                {
                    recoveryManager.recoverIndexFromBackup(file, TaskProgressSink.NULL_SINK);
                }
                catch (IndexException indexException)
                {
                    LOG.error("Could not recover from file '" + file.getPath() + "'", indexException);
                }
                return;
            }
        }
        LOG.error("No valid index backup found in '" + sourceDir + "'");
    }

    private void moveOldIndexSnapshots()
    {
        try
        {
            FileUtils.moveDirectoryToDirectory(getSnapshotImportDirectory(), getSnapshotArchiveDirectory(), true);
        }
        catch (IOException ex)
        {
            LOG.error("Could not archive snapshot directory", ex);
        }
    }

    private File getSnapshotImportDirectory()
    {
        final File snapshotImportDir = new File(ComponentAccessor.getComponent(JiraHome.class).getImportDirectory(), "indexsnapshots");
        snapshotImportDir.mkdirs();
        return snapshotImportDir;
    }

    private File getSnapshotArchiveDirectory()
    {
        return new File(ComponentAccessor.getComponent(JiraHome.class).getHome(), "old");
    }

    private boolean validIndexZipFile(final File zipFile)
    {
        final String zipPath = zipFile.getPath();
        LOG.debug("Validating file '" + zipPath + "'");
        // We want a zip file with an index for issues, comments and change history.
        Set<String> remainingEntries = Sets.newHashSet(
                IndexPathManager.Directory.ISSUES_SUBDIR + SEGMENT_NAME,
                IndexPathManager.Directory.COMMENTS_SUBDIR + SEGMENT_NAME,
                IndexPathManager.Directory.CHANGE_HISTORY_SUBDIR + SEGMENT_NAME);

        try
        {
            ZipFile file = new ZipFile(zipFile.getAbsolutePath());
            try
            {
                Enumeration<ZipArchiveEntry> entries = file.getEntries();
                while (entries.hasMoreElements())
                {
                    ZipArchiveEntry entry = entries.nextElement();
                    remainingEntries.remove(entry.getName());
                }
                boolean result = remainingEntries.isEmpty();
                if (!result)
                {
                    LOG.warn("Not a valid index snapshot '" + zipPath + "'");
                }
                return result;
            }
            finally
            {
                ZipFile.closeQuietly(file);
            }
        }
        catch (IOException e)
        {
            LOG.warn("Can't access zip file '" + zipPath + "'", e);
            return false;
        }
    }

    private void clearClusterLocks()
    {
        final ClusterLockDao clusterLockDao = ComponentAccessor.getComponent(ClusterLockDao.class);
        final ClusterNodeHeartbeatService heartbeatService = ComponentAccessor.getComponent(ClusterNodeHeartbeatService.class);
        // Locks held by non-live nodes get cleared when there is an attempt to acquire
        // them, so we only need to consider 'live' ones here.
        for (String node : heartbeatService.findLiveNodes())
        {
            if (!node.equals(heartbeatService.getNodeId()))
            {
                LOG.info("Clearing locks held by '" + node + "'");
                clusterLockDao.deleteLocksHeldByNode(node);
            }
        }
    }

}
