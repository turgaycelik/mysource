/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.action.ActionViewData;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.atlassian.scheduler.SchedulerHistoryService;
import com.atlassian.scheduler.SchedulerRuntimeException;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;
import com.atlassian.scheduler.status.JobDetails;
import com.atlassian.scheduler.status.RunDetails;
import com.atlassian.scheduler.status.RunOutcome;
import com.atlassian.webresource.api.assembler.PageBuilderService;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@WebSudoRequired
public class SchedulerAdmin extends JiraWebActionSupport
{
    public static final int HOURS_PER_DAY = 24;
    public static final int DAYS_PER_WEEK = 7;

    private final SchedulerService schedulerService;
    private final SchedulerHistoryService schedulerHistoryService;
    private final PageBuilderService pageBuilderService;
    private final Set<JobRunnerKey> registeredJobRunners;
    private final JiraDurationUtils.DurationFormatter durationFormatter;

    public SchedulerAdmin(SchedulerService schedulerService, SchedulerHistoryService schedulerHistoryService, PageBuilderService pageBuilderService, I18nHelper i18nHelper)
    {
        this.schedulerService = schedulerService;
        this.schedulerHistoryService = schedulerHistoryService;
        this.pageBuilderService = pageBuilderService;
        this.durationFormatter = new JiraDurationUtils.PrettyDurationFormatter(HOURS_PER_DAY, DAYS_PER_WEEK, i18nHelper);
        registeredJobRunners = schedulerService.getRegisteredJobRunnerKeys();
    }

    @Override
    protected String doExecute() throws Exception
    {
        pageBuilderService.assembler().resources().requireContext("scheduler-admin");
        return super.doExecute();
    }

    @ActionViewData("success")
    public List<JobRunnerWrapper> getJobRunners()
    {
        return Ordering.natural().immutableSortedCopy(
                Iterables.transform(schedulerService.getJobRunnerKeysForAllScheduledJobs(), new Function<JobRunnerKey, JobRunnerWrapper>()
                {
                    @Override
                    public JobRunnerWrapper apply(@Nullable final JobRunnerKey jobRunnerKey)
                    {
                        return new JobRunnerWrapper(notNull(jobRunnerKey));
                    }
                })
        );
    }

    public class JobRunnerWrapper implements Comparable<JobRunnerWrapper>
    {
        private final JobRunnerKey jobRunnerKey;
        private final List<JobDetailsWrapper> jobs = Lists.newArrayList();
        private final int numberOfFailedJobs;
        private final TreeSet<String> schedule = Sets.newTreeSet();

        public JobRunnerWrapper(final JobRunnerKey jobRunnerKey)
        {
            this.jobRunnerKey = jobRunnerKey;
            for (JobDetails jobDetails : schedulerService.getJobsByJobRunnerKey(jobRunnerKey))
            {
                jobs.add(new JobDetailsWrapper(jobDetails, schedulerHistoryService.getLastRunForJob(jobDetails.getJobId())));

                switch(jobDetails.getSchedule().getType())
                {
                    case CRON_EXPRESSION:
                        schedule.add(getText("admin.schedulerdetails.schedule.cron"));
                        break;
                    case INTERVAL:
                        schedule.add(getText("admin.schedulerdetails.schedule.interval"));
                        break;
                    default:
                        throw new IllegalArgumentException("unknown scheduler type " + jobDetails.getSchedule().getType());
                }
            }
            this.numberOfFailedJobs = countNumberOfFailedJobs(jobs);
        }

        protected int countNumberOfFailedJobs(final List<JobDetailsWrapper> jobs)
        {
            int numberOfFailedJobs = 0;
            for (JobDetailsWrapper job : jobs)
            {
                if (job.getLastRun() != null && job.getLastRun().getRunOutcome() != RunOutcome.SUCCESS)
                {
                    ++numberOfFailedJobs;
                }
            }
            return numberOfFailedJobs;
        }

        @Nonnull
        public TreeSet<String> getSchedule()
        {
            return schedule;
        }

        @Nonnull
        public JobRunnerKey getKey()
        {
            return jobRunnerKey;
        }

        @Nonnull
        public List<JobDetailsWrapper> getJobs()
        {
            return jobs;
        }

        public int getNumberOfSuccessfulJobs()
        {
            return jobs.size() - getNumberOfFailedJobs();
        }

        public int getNumberOfFailedJobs()
        {
            return numberOfFailedJobs;
        }

        @Nonnull
        public String getStatusIcon()
        {
            if (numberOfFailedJobs == jobs.size())
            {
                return "error";
            }
            else if (numberOfFailedJobs > 0)
            {
                return "warning";
            }
            return "success";
        }

        public boolean isRemoveable()
        {
            return !registeredJobRunners.contains(jobRunnerKey);
        }

        @Override
        public int compareTo(final JobRunnerWrapper o)
        {
            return getKey().compareTo(o.getKey());
        }
    }

    public class JobDetailsWrapper
    {
        private final JobDetails jobDetails;
        private final RunDetails lastRun;

        private JobDetailsWrapper(final JobDetails jobDetails, final RunDetails lastRun)
        {
            this.jobDetails = jobDetails;
            this.lastRun = lastRun;
        }

        @Nonnull
        public JobId getJobId()
        {
            return jobDetails.getJobId();
        }

        public boolean isRunLocally()
        {
            return jobDetails.getRunMode() == RunMode.RUN_LOCALLY;
        }

        @Nonnull
        public String getSchedule()
        {
            final Schedule schedule = jobDetails.getSchedule();
            switch(schedule.getType())
            {
                case CRON_EXPRESSION:
                    return schedule.getCronScheduleInfo().getCronExpression();
                case INTERVAL:
                    final long intervalInMillis = schedule.getIntervalScheduleInfo().getIntervalInMillis();
                    return intervalInMillis == 0 ? getText("admin.schedulerdetails.run.once") : formatDuration(intervalInMillis);
                default:
                    return schedule.toString();
            }
        }

        @CheckForNull
        public Date getNextRunTime() {return jobDetails.getNextRunTime();}

        @Nonnull
        public String getParameters() {return jobDetails.getParameters().toString();}

        public boolean isRunnable() {return jobDetails.isRunnable();}

        @Nonnull
        public String getLastRunTime()
        {
            return lastRun != null ? lastRun.getStartTime().toString() : "";
        }

        @Nonnull
        public String getLastRunDuration()
        {
            return lastRun != null ? formatDuration(lastRun.getDurationInMillis()) : "";
        }

        @Nonnull
        public String formatDuration(Long milliseconds)
        {
            if (milliseconds < 1000)
            {
                return getText("admin.schedulerdetails.milliseconds", milliseconds);
            }

            return durationFormatter.format(milliseconds / 1000);
        }

        @Nonnull
        public String getLastRunMessage()
        {
            return lastRun != null ? lastRun.getMessage() : "";
        }

        @Nonnull
        public RunDetails getLastRun()
        {
            return lastRun;
        }

        @Nullable
        public String getStatusIcon()
        {
            if (lastRun != null)
            {
                return lastRun.getRunOutcome() == RunOutcome.SUCCESS ? "success" : "error";
            }
            return null;
        }

        @Nullable
        public String getParametersOrException()
        {
            try
            {
                return String.valueOf(jobDetails.getParameters());
            }
            catch (SchedulerRuntimeException sre)
            {
                return getText("admin.schedulerdetails.cannot.access.parameters");
            }
        }

    }
}
