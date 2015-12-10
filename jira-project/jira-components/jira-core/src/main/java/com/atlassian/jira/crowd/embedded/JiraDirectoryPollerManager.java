package com.atlassian.jira.crowd.embedded;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.atlassian.crowd.directory.monitor.poller.DirectoryPoller;
import com.atlassian.crowd.manager.directory.SynchronisationMode;
import com.atlassian.crowd.manager.directory.monitor.DirectoryMonitorRegistrationException;
import com.atlassian.crowd.manager.directory.monitor.DirectoryMonitorUnregistrationException;
import com.atlassian.crowd.manager.directory.monitor.poller.DirectoryPollerManager;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.Schedule;
import com.atlassian.scheduler.status.JobDetails;

import com.google.common.collect.ImmutableMap;

import org.joda.time.DateTime;

import static com.atlassian.scheduler.config.RunMode.RUN_ONCE_PER_CLUSTER;

/**
 * JIRA implementation of atlassian-scheduler based Directory Poller Manager
 *
 * @since v6.2
 */
public class JiraDirectoryPollerManager implements DirectoryPollerManager
{
    private static final JobRunnerKey JOB_RUNNER_KEY = JobRunnerKey.of(JiraDirectoryPollerManager.class.getName());
    public static final String DIRECTORY_ID = "DIRECTORY_ID";
    private final SchedulerService schedulerService;

    public JiraDirectoryPollerManager(final SchedulerService schedulerService)
    {
        this.schedulerService = schedulerService;
        this.schedulerService.registerJobRunner(JOB_RUNNER_KEY, new JiraDirectorySynchroniser());
    }

    @Override
    public void addPoller(final DirectoryPoller poller) throws DirectoryMonitorRegistrationException
    {
        final Date oneMinuteFromNow = new DateTime().plusMinutes(1).toDate();
        final JobConfig config = JobConfig.forJobRunnerKey(JOB_RUNNER_KEY)
                .withRunMode(RUN_ONCE_PER_CLUSTER)
                .withSchedule(Schedule.forInterval(poller.getPollingInterval() * 1000, oneMinuteFromNow))
                .withParameters(ImmutableMap.<String, Serializable>of(DIRECTORY_ID, poller.getDirectoryID()));
        try
        {
            schedulerService.scheduleJob(getJobId(poller), config);
        }
        catch (SchedulerServiceException e)
        {
            throw new DirectoryMonitorRegistrationException(e);
        }
    }

    private JobId getJobId(final DirectoryPoller poller)
    {
        final long directoryID = poller.getDirectoryID();
        return getJobId(directoryID);
    }

    private JobId getJobId(final long directoryID)
    {
        return JobId.of(JiraDirectoryPollerManager.class.getName() + "." + directoryID);
    }

    @Override
    public boolean hasPoller(final long directoryID)
    {
        JobDetails jobDetails = schedulerService.getJobDetails(getJobId(directoryID));
        return jobDetails != null;
    }

    @Override
    public void triggerPoll(final long directoryID, final SynchronisationMode synchronisationMode)
    {
        JobConfig config = JobConfig.forJobRunnerKey(JOB_RUNNER_KEY)
                .withRunMode(RUN_ONCE_PER_CLUSTER)
                .withSchedule(Schedule.runOnce(null))
                .withParameters(ImmutableMap.<String, Serializable>of("DIRECTORY_ID", directoryID));
        try
        {
            schedulerService.scheduleJobWithGeneratedId(config);
        }
        catch (SchedulerServiceException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean removePoller(final long directoryID) throws DirectoryMonitorUnregistrationException
    {
        boolean exists = hasPoller(directoryID);
        schedulerService.unscheduleJob(getJobId(directoryID));
        return exists;
    }

    @Override
    public void removeAllPollers()
    {
        List<JobDetails> jobs = schedulerService.getJobsByJobRunnerKey(JOB_RUNNER_KEY);
        for (JobDetails job : jobs)
        {
            schedulerService.unscheduleJob(job.getJobId());
        }
    }
}
