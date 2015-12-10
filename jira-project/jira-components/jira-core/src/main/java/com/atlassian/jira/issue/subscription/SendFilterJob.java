/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.subscription;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

public class SendFilterJob implements JobRunner
{
    private static final Logger log = Logger.getLogger(SendFilterJob.class);

    @Nullable
    @Override
    public JobRunnerResponse runJob(final JobRunnerRequest jobRunnerRequest)
    {
        final Map<String,Serializable> parameters = jobRunnerRequest.getJobConfig().getParameters();
        Long subscriptionId = (Long) parameters.get(SubscriptionManager.SUBSCRIPTION_IDENTIFIER);
        try
        {
            SubscriptionManager dsm = ComponentAccessor.getSubscriptionManager();
            FilterSubscription subscription = dsm.getFilterSubscription(subscriptionId);
            if (subscription == null)
            {
                return JobRunnerResponse.failed("No filter subscription for id " + subscriptionId);
            }
            dsm.runSubscription(subscription.getId());
            return JobRunnerResponse.success();
        }
        catch (GenericEntityException e)
        {
            log.error("Filter Subscription failed for id " + subscriptionId, e);
            return JobRunnerResponse.failed("No filter subscription for id " + subscriptionId);
        }
    }
}
