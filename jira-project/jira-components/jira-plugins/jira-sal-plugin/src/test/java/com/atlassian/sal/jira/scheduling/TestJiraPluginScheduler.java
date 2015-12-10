package com.atlassian.sal.jira.scheduling;

import java.util.Date;

import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.sal.api.scheduling.PluginJob;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;

import com.google.common.collect.ImmutableMap;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.sal.jira.scheduling.JiraPluginScheduler.JOB_RUNNER_KEY;
import static com.atlassian.sal.jira.scheduling.JiraPluginScheduler.toJobId;
import static com.atlassian.scheduler.config.RunMode.RUN_LOCALLY;
import static com.atlassian.scheduler.config.Schedule.forInterval;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestJiraPluginScheduler
{
    private SchedulerService schedulerService;
    private JiraPluginScheduler scheduler;

    @Before
    public void setUp() throws Exception
    {
        schedulerService = mock(SchedulerService.class);
        scheduler = new JiraPluginScheduler(schedulerService);
    }

    @After
    public void tearDown() throws Exception
    {
        schedulerService = null;
        scheduler = null;
    }



    @Test
    public void testScheduleAndRepeat() throws SchedulerServiceException
    {
        final FieldMap parameters = FieldMap.build("Hello", 42L, "World", true);
        final Date startTime = new DateTime().plusMillis(1000).toDate();
        final long repeatInterval = 50L;

        scheduler.scheduleJob("myjob", PluginJob.class, parameters, startTime, repeatInterval);

        verify(schedulerService).registerJobRunner(JOB_RUNNER_KEY, scheduler);
        verify(schedulerService).scheduleJob(toJobId("myjob"), JobConfig.forJobRunnerKey(JOB_RUNNER_KEY)
                .withRunMode(RUN_LOCALLY)
                .withSchedule(forInterval(repeatInterval, startTime)));
    }

    @Test
    public void testScheduleNow() throws SchedulerServiceException
    {
        final ImmutableMap<String,Object> parameters = ImmutableMap.of();
        final long repeatInterval = 1500L;
        scheduler.scheduleJob("myjob", PluginJob.class, parameters, null, repeatInterval);

        verify(schedulerService).registerJobRunner(JOB_RUNNER_KEY, scheduler);
        verify(schedulerService).scheduleJob(toJobId("myjob"), JobConfig.forJobRunnerKey(JOB_RUNNER_KEY)
                .withRunMode(RUN_LOCALLY)
                .withSchedule(forInterval(repeatInterval, null)));
    }

}
