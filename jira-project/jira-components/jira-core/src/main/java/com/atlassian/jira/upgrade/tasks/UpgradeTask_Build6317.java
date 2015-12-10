package com.atlassian.jira.upgrade.tasks;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.jira.issue.subscription.DefaultSubscriptionManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.Schedule;

import com.google.common.collect.ImmutableMap;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * Upgrade the Filter Subscription schedule.
 * <p>
 * Historical note: This upgrade task was originally numbered 6303, but it used the wrong ID column when
 * resolving the Quartz triggers, leading to incorrect results.  The fixed upgrade task was remapped first
 * to 6307 in the 6.3-OD-1 release and 6317 for development systems.  Although the upgrade task is idempotent,
 * it is based on the old quartz tables, which will not be updated once the data has migrated to the
 * new ones.  Therefore, we should not do anything as 6317 if the Quartz data has already been fixed in
 * build 6307.
 * </p>
 *
 * @since v6.3
 */
public class UpgradeTask_Build6317 extends AbstractUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(UpgradeTask_Build6317.class);

    static final String JOB_RUNNER_KEY = DefaultSubscriptionManager.class.getName();
    static final String SUBSCRIPTION_PREFIX = DefaultSubscriptionManager.class.getName();
    static final String SUBSCRIPTION_IDENTIFIER = "SUBSCRIPTION_ID";

    static final String FILTER_GROUP_NAME = "SEND_SUBSCRIPTION";
    static final String FILTER_NAME_PREFIX = "SUBSCRIPTION_";

    private final OfBizDelegator delegator;
    private final SchedulerService schedulerService;

    public UpgradeTask_Build6317(final OfBizDelegator delegator, final SchedulerService schedulerService)
    {
        super(false);
        this.delegator = delegator;
        this.schedulerService = schedulerService;
    }
    @Override
    public String getBuildNumber()
    {
        return "6317";
    }

    @Override
    public String getShortDescription()
    {
        return "Adding filter subscriptions to atlassian-scheduler";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        if (hasRunUpgradeTask6307())
        {
            LOG.info("This upgrade task already ran as 6307; skipping it...");
        }
        else
        {
            upgradeFilterSubscriptionSchedules();
        }
    }

    private boolean hasRunUpgradeTask6307()
    {
        final List<GenericValue> upgradeHistory6307 = delegator.findByField("UpgradeHistory",
                "upgradeclass", "com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6307");
        return !upgradeHistory6307.isEmpty();
    }

    void upgradeFilterSubscriptionSchedules() throws GenericEntityException, SchedulerServiceException
    {
        final List<GenericValue> jobTriggers = delegator.findByField("QRTZTriggers", "triggerGroup", FILTER_GROUP_NAME);

        for (GenericValue jobTrigger : jobTriggers)
        {
            Schedule schedule = null;
            String triggerName = jobTrigger.getString("triggerName");
            Long id = Long.parseLong(triggerName.substring(FILTER_NAME_PREFIX.length()));

            // Get the related trigger (cron or simple)
            final String triggerType = jobTrigger.getString("triggerType");
            if ("SIMPLE".equals(triggerType))
            {
                GenericValue simpleTrigger = getTriggerDetails("QRTZSimpleTriggers", jobTrigger);
                if (simpleTrigger == null)
                {
                    LOG.warn("Missing simple trigger data for filter subscription trigger '" + triggerName + '\'');
                }
                else
                {
                    Long repeatInterval = simpleTrigger.getLong("repeatInterval");
                    if (repeatInterval != null)
                    {
                        schedule = Schedule.forInterval(repeatInterval, null);
                    }
                }
            }
            else if ("CRON".equals(triggerType))
            {
                GenericValue cronTrigger = getTriggerDetails("QRTZCronTriggers", jobTrigger);
                if (cronTrigger == null)
                {
                    LOG.warn("Missing cron trigger data for filter subscription trigger '" + triggerName + '\'');
                }
                else
                {
                    // Note: the column is misspelled as "cronexperssion", but the field name is correct.
                    String cronExpression = cronTrigger.getString("cronExpression");
                    if (cronExpression != null)
                    {
                        schedule = Schedule.forCronExpression(cronExpression);
                    }
                }
            }
            else
            {
                LOG.warn("Unable to migrate filter subscription trigger '" + triggerName +
                        "'; unsupported trigger type '" + triggerType + '\'');
            }

            if (schedule != null)
            {
                JobConfig config = getJobConfig(id, schedule);
                schedulerService.scheduleJob(toJobId(id), config);
            }
        }
    }

    @Nullable
    private GenericValue getTriggerDetails(String entityName, GenericValue trigger)
    {
        final List<GenericValue> triggerDetails = delegator.findByField(entityName, "trigger", trigger.getLong("id"));
        return (triggerDetails == null || triggerDetails.isEmpty()) ? null : triggerDetails.get(0);
    }

    private static JobConfig getJobConfig(final Long subscriptionId, final Schedule schedule)
    {
        return JobConfig.forJobRunnerKey(JobRunnerKey.of(JOB_RUNNER_KEY))
                .withSchedule(schedule)
                .withParameters(ImmutableMap.<String, Serializable>of(SUBSCRIPTION_IDENTIFIER, subscriptionId));
    }

    private static JobId toJobId(final Long subId)
    {
        return JobId.of(SUBSCRIPTION_PREFIX + ':' + subId);
    }


}
