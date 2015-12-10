package com.atlassian.jira.scheduler;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.scheduler.core.LifecycleAwareSchedulerService;
import com.atlassian.scheduler.quartz1.Quartz1SchedulerService;
import com.atlassian.util.concurrent.LazyReference;

import org.quartz.Calendar;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.SchedulerMetaData;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.UnableToInterruptJobException;
import org.quartz.spi.JobFactory;

/**
 * Delegating Quartz scheduler.
 * <p>
 * This is necessary because otherwise Quartz will blow up on an empty database.
 * Direct access to Quartz is deprecated and this functionality should be removed
 * entirely for JIRA v7.0.
 * </p>
 *
 * @since v6.3
 */
public class LazyDelegatingQuartz1Scheduler implements Scheduler
{
    private final LazyReference<Scheduler> delegateRef;

    public LazyDelegatingQuartz1Scheduler(LifecycleAwareSchedulerService lifecycleAwareSchedulerService)
    {
        final Quartz1SchedulerService schedulerService = (Quartz1SchedulerService)lifecycleAwareSchedulerService;
        this.delegateRef = new LazyReference<Scheduler>()
        {
            @Override
            protected Scheduler create() throws Exception
            {
                return createClusteredQuartz1Scheduler(schedulerService);
            }
        };
    }

    private Scheduler getDelegate() throws SchedulerException
    {
        try
        {
            return delegateRef.get();
        }
        catch (LazyReference.InitializationException ex)
        {
            throw new SchedulerException("Error creating quartz scheduler", ex.getCause());
        }
    }



    @Override
    public String getSchedulerName() throws SchedulerException
    {
        return getDelegate().getSchedulerName();
    }

    @Override
    public String getSchedulerInstanceId() throws SchedulerException
    {
        return getDelegate().getSchedulerInstanceId();
    }

    @Override
    public SchedulerContext getContext() throws SchedulerException
    {
        return getDelegate().getContext();
    }

    @Override
    public void start() throws SchedulerException
    {
        getDelegate().start();
    }

    @Override
    public void startDelayed(final int seconds) throws SchedulerException
    {
        getDelegate().startDelayed(seconds);
    }

    @Override
    public boolean isStarted() throws SchedulerException
    {
        return getDelegate().isStarted();
    }

    @Override
    public void standby() throws SchedulerException
    {
        getDelegate().standby();
    }

    @Override
    public boolean isInStandbyMode() throws SchedulerException
    {
        return getDelegate().isInStandbyMode();
    }

    @Override
    public void shutdown() throws SchedulerException
    {
        getDelegate().shutdown();
    }

    @Override
    public void shutdown(final boolean waitForJobsToComplete) throws SchedulerException
    {
        getDelegate().shutdown(waitForJobsToComplete);
    }

    @Override
    public boolean isShutdown() throws SchedulerException
    {
        return getDelegate().isShutdown();
    }

    @Override
    public SchedulerMetaData getMetaData() throws SchedulerException
    {
        return getDelegate().getMetaData();
    }

    @Override
    public List getCurrentlyExecutingJobs() throws SchedulerException
    {
        return getDelegate().getCurrentlyExecutingJobs();
    }

    @Override
    public void setJobFactory(final JobFactory factory) throws SchedulerException
    {
        getDelegate().setJobFactory(factory);
    }

    @Override
    public Date scheduleJob(final JobDetail jobDetail, final Trigger trigger) throws SchedulerException
    {
        return getDelegate().scheduleJob(jobDetail, trigger);
    }

    @Override
    public Date scheduleJob(final Trigger trigger) throws SchedulerException
    {
        return getDelegate().scheduleJob(trigger);
    }

    @Override
    public boolean unscheduleJob(final String triggerName, final String groupName) throws SchedulerException
    {
        return getDelegate().unscheduleJob(triggerName, groupName);
    }

    @Override
    public Date rescheduleJob(final String triggerName, final String groupName, final Trigger newTrigger)
            throws SchedulerException
    {
        return getDelegate().rescheduleJob(triggerName, groupName, newTrigger);
    }

    @Override
    public void addJob(final JobDetail jobDetail, final boolean replace) throws SchedulerException
    {
        getDelegate().addJob(jobDetail, replace);
    }

    @Override
    public boolean deleteJob(final String jobName, final String groupName) throws SchedulerException
    {
        return getDelegate().deleteJob(jobName, groupName);
    }

    @Override
    public void triggerJob(final String jobName, final String groupName) throws SchedulerException
    {
        getDelegate().triggerJob(jobName, groupName);
    }

    @Override
    public void triggerJobWithVolatileTrigger(final String jobName, final String groupName) throws SchedulerException
    {
        getDelegate().triggerJobWithVolatileTrigger(jobName, groupName);
    }

    @Override
    public void triggerJob(final String jobName, final String groupName, final JobDataMap data)
            throws SchedulerException
    {
        getDelegate().triggerJob(jobName, groupName, data);
    }

    @Override
    public void triggerJobWithVolatileTrigger(final String jobName, final String groupName, final JobDataMap data)
            throws SchedulerException
    {
        getDelegate().triggerJobWithVolatileTrigger(jobName, groupName, data);
    }

    @Override
    public void pauseJob(final String jobName, final String groupName) throws SchedulerException
    {
        getDelegate().pauseJob(jobName, groupName);
    }

    @Override
    public void pauseJobGroup(final String groupName) throws SchedulerException
    {
        getDelegate().pauseJobGroup(groupName);
    }

    @Override
    public void pauseTrigger(final String triggerName, final String groupName) throws SchedulerException
    {
        getDelegate().pauseTrigger(triggerName, groupName);
    }

    @Override
    public void pauseTriggerGroup(final String groupName) throws SchedulerException
    {
        getDelegate().pauseTriggerGroup(groupName);
    }

    @Override
    public void resumeJob(final String jobName, final String groupName) throws SchedulerException
    {
        getDelegate().resumeJob(jobName, groupName);
    }

    @Override
    public void resumeJobGroup(final String groupName) throws SchedulerException
    {
        getDelegate().resumeJobGroup(groupName);
    }

    @Override
    public void resumeTrigger(final String triggerName, final String groupName) throws SchedulerException
    {
        getDelegate().resumeTrigger(triggerName, groupName);
    }

    @Override
    public void resumeTriggerGroup(final String groupName) throws SchedulerException
    {
        getDelegate().resumeTriggerGroup(groupName);
    }

    @Override
    public void pauseAll() throws SchedulerException
    {
        getDelegate().pauseAll();
    }

    @Override
    public void resumeAll() throws SchedulerException
    {
        getDelegate().resumeAll();
    }

    @Override
    public String[] getJobGroupNames() throws SchedulerException
    {
        return getDelegate().getJobGroupNames();
    }

    @Override
    public String[] getJobNames(final String groupName) throws SchedulerException
    {
        return getDelegate().getJobNames(groupName);
    }

    @Override
    public Trigger[] getTriggersOfJob(final String jobName, final String groupName) throws SchedulerException
    {
        return getDelegate().getTriggersOfJob(jobName, groupName);
    }

    @Override
    public String[] getTriggerGroupNames() throws SchedulerException
    {
        return getDelegate().getTriggerGroupNames();
    }

    @Override
    public String[] getTriggerNames(final String groupName) throws SchedulerException
    {
        return getDelegate().getTriggerNames(groupName);
    }

    @Override
    public Set getPausedTriggerGroups() throws SchedulerException
    {
        return getDelegate().getPausedTriggerGroups();
    }

    @Override
    public JobDetail getJobDetail(final String jobName, final String jobGroup) throws SchedulerException
    {
        return getDelegate().getJobDetail(jobName, jobGroup);
    }

    @Override
    public Trigger getTrigger(final String triggerName, final String triggerGroup) throws SchedulerException
    {
        return getDelegate().getTrigger(triggerName, triggerGroup);
    }

    @Override
    public int getTriggerState(final String triggerName, final String triggerGroup) throws SchedulerException
    {
        return getDelegate().getTriggerState(triggerName, triggerGroup);
    }

    @Override
    public void addCalendar(final String calName, final Calendar calendar, final boolean replace, final boolean updateTriggers)
            throws SchedulerException
    {
        getDelegate().addCalendar(calName, calendar, replace, updateTriggers);
    }

    @Override
    public boolean deleteCalendar(final String calName) throws SchedulerException
    {
        return getDelegate().deleteCalendar(calName);
    }

    @Override
    public Calendar getCalendar(final String calName) throws SchedulerException
    {
        return getDelegate().getCalendar(calName);
    }

    @Override
    public String[] getCalendarNames() throws SchedulerException
    {
        return getDelegate().getCalendarNames();
    }

    @Override
    public boolean interrupt(final String jobName, final String groupName) throws UnableToInterruptJobException
    {
        try
        {
            // LazyReference should *not* be marking this @Nullable!
            //noinspection ConstantConditions
            return delegateRef.get().interrupt(jobName, groupName);
        }
        catch (LazyReference.InitializationException ex)
        {
            throw new UnableToInterruptJobException(ex);
        }
    }

    @Override
    public void addGlobalJobListener(final JobListener jobListener) throws SchedulerException
    {
        getDelegate().addGlobalJobListener(jobListener);
    }

    @Override
    public void addJobListener(final JobListener jobListener) throws SchedulerException
    {
        getDelegate().addJobListener(jobListener);
    }

    @Override
    public boolean removeGlobalJobListener(final String name) throws SchedulerException
    {
        return getDelegate().removeGlobalJobListener(name);
    }

    @Override
    public boolean removeJobListener(final String name) throws SchedulerException
    {
        return getDelegate().removeJobListener(name);
    }

    @Override
    public List getGlobalJobListeners() throws SchedulerException
    {
        return getDelegate().getGlobalJobListeners();
    }

    @Override
    public Set getJobListenerNames() throws SchedulerException
    {
        return getDelegate().getJobListenerNames();
    }

    @Override
    public JobListener getGlobalJobListener(final String name) throws SchedulerException
    {
        return getDelegate().getGlobalJobListener(name);
    }

    @Override
    public JobListener getJobListener(final String name) throws SchedulerException
    {
        return getDelegate().getJobListener(name);
    }

    @Override
    public void addGlobalTriggerListener(final TriggerListener triggerListener) throws SchedulerException
    {
        getDelegate().addGlobalTriggerListener(triggerListener);
    }

    @Override
    public void addTriggerListener(final TriggerListener triggerListener) throws SchedulerException
    {
        getDelegate().addTriggerListener(triggerListener);
    }

    @Override
    public boolean removeGlobalTriggerListener(final String name) throws SchedulerException
    {
        return getDelegate().removeGlobalTriggerListener(name);
    }

    @Override
    public boolean removeTriggerListener(final String name) throws SchedulerException
    {
        return getDelegate().removeTriggerListener(name);
    }

    @Override
    public List getGlobalTriggerListeners() throws SchedulerException
    {
        return getDelegate().getGlobalTriggerListeners();
    }

    @Override
    public Set getTriggerListenerNames() throws SchedulerException
    {
        return getDelegate().getTriggerListenerNames();
    }

    @Override
    public TriggerListener getGlobalTriggerListener(final String name) throws SchedulerException
    {
        return getDelegate().getGlobalTriggerListener(name);
    }

    @Override
    public TriggerListener getTriggerListener(final String name) throws SchedulerException
    {
        return getDelegate().getTriggerListener(name);
    }

    @Override
    public void addSchedulerListener(final SchedulerListener schedulerListener) throws SchedulerException
    {
        getDelegate().addSchedulerListener(schedulerListener);
    }

    @Override
    public boolean removeSchedulerListener(final SchedulerListener schedulerListener) throws SchedulerException
    {
        return getDelegate().removeSchedulerListener(schedulerListener);
    }

    @Override
    public List getSchedulerListeners() throws SchedulerException
    {
        return getDelegate().getSchedulerListeners();
    }



    static Scheduler createClusteredQuartz1Scheduler(final Quartz1SchedulerService schedulerService)
    {
        // JDEV-28428 SCHEDULER-11: Make sure the webapp class loader is the one in effect when we access Quartz
        // for the first time, as otherwise some Plugin's class loader might be used accidentally.
        final Thread thd = Thread.currentThread();
        final ClassLoader originalClassLoader = thd.getContextClassLoader();
        final ClassLoader webappClassLoader = ComponentAccessor.class.getClassLoader();
        try
        {
            thd.setContextClassLoader(webappClassLoader);
            return schedulerService.getClusteredQuartzScheduler();
        }
        finally
        {
            thd.setContextClassLoader(originalClassLoader);
        }
    }
}
