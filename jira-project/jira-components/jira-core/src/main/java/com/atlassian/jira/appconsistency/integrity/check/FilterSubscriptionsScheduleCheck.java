package com.atlassian.jira.appconsistency.integrity.check;

import java.util.List;

import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.amendment.DeleteEntityAmendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.issue.subscription.DefaultSubscriptionManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.status.JobDetails;

import org.ofbiz.core.entity.GenericValue;

/**
 * Checks and fixes the case where we have a FilterSubscription that has no corresponding
 * Quartz trigger.
 */
public class FilterSubscriptionsScheduleCheck extends BaseFilterSubscriptionsCheck
{

    private final SchedulerService schedulerService;

    public FilterSubscriptionsScheduleCheck(OfBizDelegator ofBizDelegator, final SchedulerService schedulerService, int id)
    {
        super(ofBizDelegator, id);
        this.schedulerService = schedulerService;
    }

    public String getDescription()
    {
        return getI18NBean().getText("admin.integrity.check.filter.subscriptions.trigger.desc");
    }

    // Ensure that the filter subscriptions table does not contain references to search requests that have been deleted.
    protected void doRealCheck(boolean correct, GenericValue subscription, List<DeleteEntityAmendment> messages) throws IntegrityException
    {
        // try to find the related quartz trigger, if null then flag
        JobDetails jobDetails = getScheduledJob(subscription);

        if (jobDetails == null)
        {
            if (correct)
            {
                // flag the current subscription for deletion
                messages.add(new DeleteEntityAmendment(Amendment.CORRECTION, getI18NBean().getText("admin.integrity.check.filter.subscriptions.trigger.message", subscription.getString("id")), subscription));
            }
            else
            {
                messages.add(new DeleteEntityAmendment(Amendment.ERROR, getI18NBean().getText("admin.integrity.check.filter.subscriptions.trigger.preview", subscription.getString("id")), subscription));
            }
        }
    }

    private JobDetails getScheduledJob(GenericValue subscription)
    {
        JobId jobId = toJobId(subscription.getLong("id"));
        return schedulerService.getJobDetails(jobId);
    }

    private JobId toJobId(final Long subId)
    {
        return JobId.of(DefaultSubscriptionManager.SUBSCRIPTION_PREFIX + ":" + subId);
    }


}
