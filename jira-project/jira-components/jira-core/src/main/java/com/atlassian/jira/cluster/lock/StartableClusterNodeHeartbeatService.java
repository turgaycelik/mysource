package com.atlassian.jira.cluster.lock;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.atlassian.beehive.db.AbstractClusterNodeHeartbeatService;
import com.atlassian.beehive.db.spi.ClusterNodeHeartBeatDao;
import com.atlassian.core.util.Clock;
import com.atlassian.jira.extension.Startable;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;

/**
 * JIRA implementation of ClusterNodeHeartbeatService that is Startable
 *
 * @since 6.3
 */
public class StartableClusterNodeHeartbeatService extends AbstractClusterNodeHeartbeatService implements Startable
{
    private static final String SHARED_HOME_FILE_WRITER_JOB_RUNNER_KEY_NAME = StartableClusterNodeHeartbeatService.class.getName() + ".sharedHomeFileWriter";
    private static final JobRunnerKey SHARED_HOME_FILE_WRITER_JOB_RUNNER_KEY = JobRunnerKey.of(SHARED_HOME_FILE_WRITER_JOB_RUNNER_KEY_NAME);

    private final SchedulerService schedulerService;
    private final SharedHomeNodeStatusWriter sharedHomeNodeStatusWriter;
    private final Clock clock;

    public StartableClusterNodeHeartbeatService(final ClusterNodeHeartBeatDao clusterNodeHeartBeatDao, final SchedulerService schedulerService,
            final SharedHomeNodeStatusWriter sharedHomeNodeStatusWriter, final Clock clock)
    {
        super(clusterNodeHeartBeatDao, schedulerService);
        this.schedulerService = schedulerService;
        this.sharedHomeNodeStatusWriter = sharedHomeNodeStatusWriter;
        this.clock = clock;
    }

    @Override
    public void start() throws Exception
    {
        startHeartbeat();
    }

    @Override
    protected void startHeartbeat() throws SchedulerServiceException
    {
        super.startHeartbeat();

        //Initially run explicitly to get the first run out immediately
        SharedHomeFileWriterJobRunner runner = new SharedHomeFileWriterJobRunner();
        runner.writeFile();

        schedulerService.registerJobRunner(SHARED_HOME_FILE_WRITER_JOB_RUNNER_KEY, runner);
        JobConfig jobConfig = JobConfig.forJobRunnerKey(SHARED_HOME_FILE_WRITER_JOB_RUNNER_KEY)
                                .withRunMode(RunMode.RUN_LOCALLY)
                                .withSchedule(Schedule.forInterval(TimeUnit.MINUTES.toMillis(1), null));
        schedulerService.scheduleJob(JobId.of(SHARED_HOME_FILE_WRITER_JOB_RUNNER_KEY_NAME), jobConfig);
    }

    private class SharedHomeFileWriterJobRunner implements JobRunner
    {
        @Nullable
        @Override
        public JobRunnerResponse runJob(JobRunnerRequest request)
        {
            writeFile();
            return JobRunnerResponse.success();
        }

        private void writeFile()
        {
            long now = clock.getCurrentDate().getTime();
            NodeSharedHomeStatus status = new NodeSharedHomeStatus(getNodeId(), now);
            sharedHomeNodeStatusWriter.writeNodeStatus(status);
        }
    }
}
