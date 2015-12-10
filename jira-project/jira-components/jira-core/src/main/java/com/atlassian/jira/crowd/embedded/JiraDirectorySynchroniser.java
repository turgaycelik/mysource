package com.atlassian.jira.crowd.embedded;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.directory.SynchronisableDirectory;
import com.atlassian.crowd.directory.loader.DirectoryInstanceLoader;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectorySynchronisationInformation;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.manager.directory.DirectorySynchroniser;
import com.atlassian.crowd.manager.directory.SynchronisationMode;
import com.atlassian.crowd.manager.directory.SynchronisationStatusManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Atlassian scheduler job for synchronising directories.
 *
 * @since v6.2
 */
public class JiraDirectorySynchroniser implements JobRunner
{
    public static final String CROWD_SYNC_INCREMENTAL_ENABLED = "crowd.sync.incremental.enabled";
    private static final Logger LOG = LoggerFactory.getLogger(JobRunnerResponse.class);

    @Nullable
    @Override
    public JobRunnerResponse runJob(final JobRunnerRequest request)
    {
        final SynchronisationStatusManager synchronisationStatusManager = ComponentAccessor.getComponent(SynchronisationStatusManager.class);
        final DirectorySynchroniser directorySynchroniser = ComponentAccessor.getComponent(DirectorySynchroniser.class);
        final CrowdDirectoryService directoryService = ComponentAccessor.getComponent(CrowdDirectoryService.class);
        final DirectoryInstanceLoader directoryInstanceLoader = ComponentAccessor.getComponent(DirectoryInstanceLoader.class);

        Map<String, Serializable> params = request.getJobConfig().getParameters();
        Long directoryId = (Long) params.get(JiraDirectoryPollerManager.DIRECTORY_ID);

        try
        {
            Directory directory = directoryService.findDirectoryById(directoryId);
            if (directory != null)
            {
                RemoteDirectory remoteDirectory = directoryInstanceLoader.getDirectory(directory);

                if (remoteDirectory instanceof SynchronisableDirectory)
                {
                    synchronizeDirectory(directorySynchroniser, directory, (SynchronisableDirectory) remoteDirectory);
                }
                else
                {
                    LOG.error("Unable to synchronise directory; not an instance of SynchronisableDirectory.class");
                }
                DirectorySynchronisationInformation info = synchronisationStatusManager.getDirectorySynchronisationInformation(directory);
                return JobRunnerResponse.success("Directory '" + directory.getName() + "' synchronised in " + info.getLastRound().getDurationMs() + " milliseconds.");
            }
            else
            {
                throw new DirectoryNotFoundException(directoryId);
            }
        }
        catch (DirectoryNotFoundException e)
        {
            LOG.error("Unable to synchronise directory", e);
            return JobRunnerResponse.failed(e);
        }
        catch (OperationFailedException e)
        {
            LOG.error("Unable to synchronise directory", e);
            return JobRunnerResponse.failed(e);
        }
        catch (RuntimeException e)
        {
            LOG.error("Unable to synchronise directory", e);
            throw e;
        }
    }

    @VisibleForTesting
    void synchronizeDirectory(final DirectorySynchroniser directorySynchroniser, final Directory directory, final SynchronisableDirectory remoteDirectory)
            throws DirectoryNotFoundException, OperationFailedException
    {
        SynchronisationMode synchronisationMode = getSynchronisationMode(directory);
        directorySynchroniser.synchronise(remoteDirectory, synchronisationMode);
    }

    private SynchronisationMode getSynchronisationMode(Directory directory)
    {
        final Map<String, String> attributes = directory.getAttributes();
        if(attributes == null)
        {
            return SynchronisationMode.INCREMENTAL;
        }

        final String syncMode = attributes.get(CROWD_SYNC_INCREMENTAL_ENABLED);
        if(syncMode == null || "true".equals(syncMode))
        {
            return SynchronisationMode.INCREMENTAL;
        }
        else
        {
            return SynchronisationMode.FULL;
        }
    }

}
