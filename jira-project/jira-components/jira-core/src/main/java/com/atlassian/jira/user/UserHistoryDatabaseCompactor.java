package com.atlassian.jira.user;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.extension.Startable;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 *  Job which clears userhistoryitem table from entries older than 90 days
 *
 * @since v6.4
 */
public class UserHistoryDatabaseCompactor implements Startable, JobRunner
{

    private static final Logger LOG = LoggerFactory.getLogger(UserHistoryDatabaseCompactor.class);
    private static final long DAILY= TimeUnit.DAYS.toMillis(1);
    private static final JobRunnerKey JOB_RUNNER_KEY = JobRunnerKey.of(UserHistoryDatabaseCompactor.class.getName());
    private static final JobId JOB_ID = JobId.of(UserHistoryDatabaseCompactor.class.getName());

    private final UserHistoryStore userHistoryStore;

    public UserHistoryDatabaseCompactor(final UserHistoryStore userHistoryStore)
    {
        this.userHistoryStore = userHistoryStore;
    }

    @Nullable
    @Override
    public JobRunnerResponse runJob(final JobRunnerRequest jobRunnerRequest)
    {
        try
        {
            final Long removeThreshold = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90);
            userHistoryStore.removeHistoryOlderThan(removeThreshold);
        }
        catch (Exception ex)
        {
            LOG.warn("Exception occurred when running " + JOB_RUNNER_KEY + " job.", ex);
            return JobRunnerResponse.failed(ex);
        }
        return JobRunnerResponse.success();
    }

    @Override
    public void start() throws Exception
    {
        final SchedulerService scheduler = ComponentAccessor.getComponent(SchedulerService.class);
        scheduler.registerJobRunner(JOB_RUNNER_KEY, this);

        final JobConfig jobConfig = JobConfig.forJobRunnerKey(JOB_RUNNER_KEY)
                .withRunMode(RunMode.RUN_LOCALLY)
                .withSchedule(Schedule.forInterval(DAILY, null));

        scheduler.scheduleJob(JOB_ID, jobConfig);
    }
}
