package com.atlassian.sal.jira.scheduling;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;

import com.atlassian.sal.api.scheduling.PluginJob;
import com.atlassian.sal.api.scheduling.PluginScheduler;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.atlassian.scheduler.SchedulerRuntimeException;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;

import static com.atlassian.scheduler.JobRunnerResponse.aborted;
import static com.atlassian.util.concurrent.Assertions.notNull;

/**
 * A plugin scheduler that is backed by the atlassian-scheduler library.
 * SAL should get its own in {@code v2.11.0}.
 *
 * @since v6.2
 */
public class JiraPluginScheduler implements PluginScheduler, JobRunner
{
    static final JobRunnerKey JOB_RUNNER_KEY = JobRunnerKey.of(JiraPluginScheduler.class.getName());

    // Must keep them locally because the scheduler service only accepts Serializable args, not arbitrary Objects
    private final ConcurrentMap<JobId,JobDescriptor> descriptors = new ConcurrentHashMap<JobId,JobDescriptor>();

    private final SchedulerService schedulerService;

    public JiraPluginScheduler(SchedulerService schedulerService)
    {
        this.schedulerService = schedulerService;

        // Registering here would allow "this" to escape the thread before it is properly constructed.
        // See Java Concurrency in Practice, section 3.2.1.
    }

    @Override
    public void scheduleJob(String jobKey, Class<? extends PluginJob> jobClass, Map<String, Object> jobDataMap, Date startTime, long repeatInterval)
    {
        // Register here, instead.  It doesn't matter if we register multiple times.
        schedulerService.registerJobRunner(JOB_RUNNER_KEY, this);

        final JobId jobId = toJobId(jobKey);
        descriptors.put(jobId, new JobDescriptor(jobClass, jobDataMap));
        final JobConfig jobConfig = JobConfig.forJobRunnerKey(JOB_RUNNER_KEY)
                .withRunMode(RunMode.RUN_LOCALLY)
                .withSchedule(Schedule.forInterval(repeatInterval, startTime));
        try
        {
            schedulerService.scheduleJob(jobId, jobConfig);
        }
        catch (SchedulerServiceException sse)
        {
            throw new SchedulerRuntimeException(sse.getMessage(), sse);
        }
    }

    @Override
    public void unscheduleJob(String jobKey)
    {
        final JobId jobId = toJobId(jobKey);
        if (descriptors.remove(jobId) == null)
        {
            throw new IllegalArgumentException("Job descriptor not found for job '" + jobKey + "'");
        }
        schedulerService.unscheduleJob(jobId);
    }

    @Nonnull
    @Override
    public JobRunnerResponse runJob(JobRunnerRequest jobRunnerRequest)
    {
        final JobDescriptor descriptor = descriptors.get(jobRunnerRequest.getJobId());
        if (descriptor == null)
        {
            return aborted("Job descriptor not found");
        }
        return descriptor.runJob();
    }

    static JobId toJobId(String jobKey)
    {
        return JobId.of(JiraPluginScheduler.class.getSimpleName() + ':' + jobKey);
    }



    static class JobDescriptor
    {
        final Class<? extends PluginJob> jobClass;
        final Map<String,Object> jobDataMap;

        @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")  // Required for compatibility
        JobDescriptor(final Class<? extends PluginJob> jobClass, final Map<String,Object> jobDataMap)
        {
            this.jobClass = notNull("jobClass", jobClass);
            this.jobDataMap = jobDataMap;
        }

        @Nonnull
        JobRunnerResponse runJob()
        {
            final PluginJob job;
            try
            {
                job = jobClass.newInstance();
            }
            catch (InstantiationException e)
            {
                return aborted(e.toString());
            }
            catch (IllegalAccessException e)
            {
                return aborted(e.toString());
            }

            job.execute(jobDataMap);
            return JobRunnerResponse.success();
        }
    }
}
