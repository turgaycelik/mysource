package com.atlassian.jira.upgrade.tasks;

import java.sql.Timestamp;
import java.util.List;

import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

import com.google.common.collect.ImmutableList;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * Upgrade the Quartz scheduler tables
 *
 * @since v6.3
 */
public class UpgradeTask_Build6302 extends AbstractUpgradeTask
{
    static final List<String> DEPRECATED_JOB_GROUPS = ImmutableList.of(
            "SEND_SUBSCRIPTION");

    static final List<String> DEPRECATED_TRIGGER_GROUPS = ImmutableList.of(
            "SEND_SUBSCRIPTION");

    static final List<String> LOCK_NAMES = ImmutableList.of(
            "TRIGGER_ACCESS",
            "JOB_ACCESS",
            "CALENDAR_ACCESS",
            "STATE_ACCESS",
            "MISFIRE_ACCESS" );

    private final OfBizDelegator delegator;
    private final ServiceManager serviceManager;

    public UpgradeTask_Build6302(final OfBizDelegator delegator, final ServiceManager serviceManager)
    {
        super(false);
        this.delegator = delegator;
        this.serviceManager = serviceManager;
    }

    @Override
    public String getBuildNumber()
    {
        return "6302";
    }

    @Override
    public String getShortDescription()
    {
        return "Upgrading quartz scheduler tables";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        upgradeJobDetails();
        upgradeTriggers();
        upgradeSimpleTriggers();
        upgradeCronTriggers();
        addLocks();

        // JDEV-28000 Just in case the service manager thinks it scheduled stuff already; this will make it re-check
        serviceManager.refreshAll();
    }

    private void upgradeJobDetails() throws GenericEntityException
    {
        // For idempotency remove any rows from the target tables before we start
        delegator.removeByCondition("JQRTZJobDetails", null);

        List<GenericValue> jobDetails = delegator.findAll("QRTZJobDetails");
        for (GenericValue jobDetail : jobDetails)
        {
            if (DEPRECATED_JOB_GROUPS.contains(jobDetail.getString("jobGroup")))
            {
                continue;
            }
            FieldMap values = new FieldMap();
            values.putAll(jobDetail);
            values.remove("id");
            Boolean isDurable = Boolean.valueOf(jobDetail.getString("isDurable"));
            values.put("isDurable", isDurable);
            values.put("isVolatile", false);
            Boolean isStateful = Boolean.valueOf(jobDetail.getString("isStateful"));
            values.put("isStateful", isStateful);
            values.put("requestsRecovery", Boolean.FALSE);
            values.remove("jobData");    // Atlassian scheduler's job store never supported persisting this & EntityEngine is broken for blobs
            delegator.makeValue("JQRTZJobDetails", values).create();
        }
    }

    private void upgradeTriggers() throws GenericEntityException
    {
        // For idempotency remove any rows from the target tables before we start
        delegator.removeByCondition("JQRTZTriggers", null);

        List<GenericValue> jobTriggers = delegator.findAll("QRTZTriggers");
        for (GenericValue jobTrigger : jobTriggers)
        {
            if (DEPRECATED_TRIGGER_GROUPS.contains(jobTrigger.getString("triggerGroup")))
            {
                continue;
            }
            FieldMap values = new FieldMap();
            values.putAll(jobTrigger);
            // Get the job from the old tables
            GenericValue job = delegator.findById("QRTZJobDetails", jobTrigger.getLong("job"));
            if (job != null)
            {
                values.remove("id");
                values.remove("job");
                values.put("schedName", "JIRA_scheduler");
                values.put("jobName", job.getString("jobName"));
                values.put("jobGroup", job.getString("jobGroup"));
                values.put("isVolatile", false);
                values.put("nextFireTime", toSeconds(values.get("nextFire")));
                values.remove("nextFire");
                values.put("startTime", toSeconds(values.get("startTime")));
                values.put("endTime", toSeconds(values.get("endTime")));
                values.put("description", (job.getString("jobName") + job.getString("jobGroup")));
                values.put("triggerState", "NORMAL");

                delegator.makeValue("JQRTZTriggers", values).create();
            }
        }
    }

    private Long toSeconds(final Object aDate)
    {
        return aDate == null ? null : ((Timestamp) aDate).getTime();
    }

    private void upgradeSimpleTriggers() throws GenericEntityException
    {
        // For idempotency remove any rows from the target tables before we start
        delegator.removeByCondition("JQRTZSimpleTriggers", null);

        List<GenericValue> jobSimpleTriggers = delegator.findAll("QRTZSimpleTriggers");
        for (GenericValue jobSimpleTrigger : jobSimpleTriggers)
        {
            FieldMap values = new FieldMap();
            values.putAll(jobSimpleTrigger);
            // Get the job from the old tables
            GenericValue trigger = delegator.findById("QRTZTriggers", jobSimpleTrigger.getLong("trigger"));
            if (trigger != null && !DEPRECATED_TRIGGER_GROUPS.contains(trigger.getString("triggerGroup")))
            {
                values.remove("id");
                values.remove("trigger");
                values.put("triggerName", trigger.getString("triggerName"));
                values.put("triggerGroup", trigger.getString("triggerGroup"));
                delegator.makeValue("JQRTZSimpleTriggers", values).create();
            }
        }
    }

    private void upgradeCronTriggers() throws GenericEntityException
    {
        // For idempotency remove any rows from the target tables before we start
        delegator.removeByCondition("JQRTZCronTriggers", null);

        List<GenericValue> jobCronTriggers = delegator.findAll("QRTZCronTriggers");
        for (GenericValue jobCronTrigger : jobCronTriggers)
        {
            FieldMap values = new FieldMap();
            values.putAll(jobCronTrigger);
            // Get the job from the old tables
            GenericValue trigger = delegator.findById("QRTZTriggers", jobCronTrigger.getLong("trigger"));
            if (trigger != null && !DEPRECATED_TRIGGER_GROUPS.contains(trigger.getString("triggerGroup")))
            {
                values.remove("id");
                values.remove("trigger");
                values.put("triggerName", trigger.getString("triggerName"));
                values.put("triggerGroup", trigger.getString("triggerGroup"));
                delegator.makeValue("JQRTZCronTriggers", values).create();
            }
        }
    }

    private void addLocks() throws GenericEntityException
    {
        // For idempotency remove any rows from the target tables before we start
        delegator.removeByCondition("JQRTZLocks", null);

        for (String lockName : LOCK_NAMES)
        {
            delegator.makeValue("JQRTZLocks", FieldMap.build("lockName", lockName)).create();
        }
    }
}
