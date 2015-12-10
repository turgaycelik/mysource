package com.atlassian.jira.web.action.admin;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.scheduler.SchedulerHistoryService;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.Schedule;
import com.atlassian.scheduler.status.JobDetails;
import com.atlassian.webresource.api.assembler.PageBuilderService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class TestSchedulerAdmin
{
    private static final JobId TEST_JOB_ID = JobId.of("test-123");
    private static final JobRunnerKey TEST_JOB_RUNNER_KEY = JobRunnerKey.of("run-123");

    private SchedulerAdmin admin;
    private MockSchedulerService schedulerService;

    @Before
    public void setUpAdmin()
    {
        final ResourceBundle rb = ResourceBundle.getBundle(JiraWebActionSupport.class.getName(), Locale.ENGLISH);

        JobDetails mockJobDetails = mock(JobDetails.class);
        when(mockJobDetails.getJobId()).thenReturn(TEST_JOB_ID);

        schedulerService = mock(MockSchedulerService.class, Mockito.CALLS_REAL_METHODS);

        SchedulerHistoryService schedulerHistoryService = mock(SchedulerHistoryService.class);
        PageBuilderService pageBuilderService = mock(PageBuilderService.class);

        I18nHelper mockI18nHelper = mock(I18nHelper.class);
        when(mockI18nHelper.getDefaultResourceBundle()).thenReturn(rb);

        admin = new SchedulerAdmin(schedulerService, schedulerHistoryService, pageBuilderService, mockI18nHelper)
        {
            //So we don't need a mock for i18n helper
            @Override
            public String getText(String key)
            {
                return("");
            }

            @Override
            public ResourceBundle getResourceBundle()
            {
                return(rb);
            }
        };
    }

    private SchedulerAdmin.JobDetailsWrapper createDetailForJobWithInterval(long intervalMillis)
    {
        schedulerService.setMockIntervalMillis(intervalMillis);
        SchedulerAdmin.JobRunnerWrapper jobWrapper = admin.new JobRunnerWrapper(TEST_JOB_RUNNER_KEY);
        SchedulerAdmin.JobDetailsWrapper detailWrapper = jobWrapper.getJobs().get(0);

        return(detailWrapper);
    }

    @Test
    public void testSeconds()
    {
        SchedulerAdmin.JobDetailsWrapper detailWrapper = createDetailForJobWithInterval(3000L);
        assertEquals("Wrong schedule.", "3 seconds", detailWrapper.getSchedule());
    }

    @Test
    public void testSmallHours()
    {
        SchedulerAdmin.JobDetailsWrapper detailWrapper = createDetailForJobWithInterval(1000L * 60 * 60 * 4);
        assertEquals("Wrong schedule.", "4 hours", detailWrapper.getSchedule());
    }

    @Test
    public void testLargeHours()
    {
        SchedulerAdmin.JobDetailsWrapper detailWrapper = createDetailForJobWithInterval(1000L * 60 * 60 * 23);
        assertEquals("Wrong schedule.", "23 hours", detailWrapper.getSchedule());
    }

    @Test
    public void testDays()
    {
        SchedulerAdmin.JobDetailsWrapper detailWrapper = createDetailForJobWithInterval(1000L * 60 * 60 * 24);
        assertEquals("Wrong schedule.", "1 day", detailWrapper.getSchedule());
    }

    @Test
    public void testHoursAndMinutes()
    {
        SchedulerAdmin.JobDetailsWrapper detailWrapper = createDetailForJobWithInterval(1000L * 60 * 90);
        assertEquals("Wrong schedule.", "1 hour, 30 minutes", detailWrapper.getSchedule());
    }

    /**
     * A scheduler service mock that allows the interval to be set by our tests.
     */
    private static abstract class MockSchedulerService implements SchedulerService
    {
        private long interval;

        public void setMockIntervalMillis(long interval)
        {
            this.interval = interval;
        }

        @Nonnull
        @Override
        public List<JobDetails> getJobsByJobRunnerKey(@Nonnull JobRunnerKey jobRunnerKey)
        {
            if (jobRunnerKey.equals(TEST_JOB_RUNNER_KEY))
            {
                Schedule schedule = Schedule.forInterval(interval, new Date());

                JobDetails jd = mock(JobDetails.class);
                when(jd.getSchedule()).thenReturn(schedule);

                return(Collections.singletonList(jd));

            }
            else
                return(Collections.emptyList());
        }

        @Nonnull
        @Override
        public Set<JobRunnerKey> getRegisteredJobRunnerKeys()
        {
            return(Collections.emptySet());
        }
    }
}
