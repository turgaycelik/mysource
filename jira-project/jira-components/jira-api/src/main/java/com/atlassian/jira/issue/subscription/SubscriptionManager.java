/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.subscription;

import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.user.ApplicationUser;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.quartz.Trigger;

@PublicApi
public interface SubscriptionManager
{
    public static final String SUBSCRIPTION_IDENTIFIER = "SUBSCRIPTION_ID";

    boolean hasSubscription(ApplicationUser user, Long filterId) throws GenericEntityException;

    /**
     * @deprecated Use {@link #hasSubscription(ApplicationUser, Long)} instead. Since v6.0.
     */
    boolean hasSubscription(User user, Long filterId) throws GenericEntityException;

    FilterSubscription getFilterSubscription(ApplicationUser user, Long subId) throws GenericEntityException;

    /**
     * @deprecated Use {@link #getFilterSubscription(ApplicationUser, Long)} instead. Since v6.0.
     */
    GenericValue getSubscription(ApplicationUser user, Long subId) throws GenericEntityException;

    /**
     * @deprecated Use {@link #getFilterSubscription(ApplicationUser, Long)} instead. Since v6.0.
     */
    GenericValue getSubscription(User user, Long subId) throws GenericEntityException;

    List<FilterSubscription> getFilterSubscriptions(ApplicationUser user, Long filterId) throws GenericEntityException;

    /**
     * @deprecated Use {@link #getFilterSubscriptions(ApplicationUser, Long)} instead. Since v6.0.
     * @throws GenericEntityException
     */
    List<GenericValue> getSubscriptions(ApplicationUser user, Long filterId) throws GenericEntityException;

    /**
     * @deprecated Use {@link #getFilterSubscriptions(ApplicationUser, Long)} instead. Since v6.0.
     * @throws GenericEntityException
     */
    List<GenericValue> getSubscriptions(User user, Long filterId) throws GenericEntityException;

    GenericValue createSubscription(ApplicationUser user, Long filterId, String groupName, Long period, Boolean emailOnEmpty);

    /**
     * @deprecated Use {@link #createSubscription(com.atlassian.jira.user.ApplicationUser, Long, String, org.quartz.Trigger, Boolean)} instead. Since v6.0.
     */
    GenericValue createSubscription(User user, Long filterId, String groupName, Long period, Boolean emailOnEmpty);

    /**
     * Creates a new subscription based on the passed in filter id and fired
     * in accordance with the passed in trigger
     *
     * @param user         the current user performing this operation
     * @param filterId     Id of the filter subscribing to
     * @param groupName    Sent ot group
     * @param cronExpression The Cron expression for the subscription
     * @param emailOnEmpty send email if filter returns no results
     * @return GenericValue representing new subscription
     */
    FilterSubscription createSubscription(ApplicationUser user, Long filterId, String groupName, String cronExpression, Boolean emailOnEmpty);

    /**
     * Creates a new subscription based on the passed in filter id and fired
     * in accordance with the passed in trigger
     *
     * @param user         the current user performing this operation
     * @param filterId     Id of the filter subscribing to
     * @param groupName    Sent ot group
     * @param trigger      The trigger to store
     * @param emailOnEmpty send email if filter returns no results
     * @return GenericValue representing new subscription
     * @deprecated Since v6.2. Quartz internals are no longer supported through the JIRA api
     *                      use {@link #createSubscription(ApplicationUser, Long, String, String, Boolean)}
     */
    GenericValue createSubscription(ApplicationUser user, Long filterId, String groupName, Trigger trigger, Boolean emailOnEmpty);

    /**
     * @deprecated Use {@link #createSubscription(ApplicationUser, Long, String, String, Boolean)} instead. Since v6.0.
     * Creates a new subscription based on the passed in filter id and fired
     * in accordance with the passed in trigger
     *
     * @param user         the current user performing this operation
     * @param filterId     Id of the filter subscribing to
     * @param groupName    Sent ot group
     * @param trigger      The trigger to store
     * @param emailOnEmpty send email if filter returns no results
     * @return GenericValue representing new subscription
     */
    GenericValue createSubscription(User user, Long filterId, String groupName, Trigger trigger, Boolean emailOnEmpty);

    void deleteSubscription(Long subId) throws GenericEntityException;

    /**
     * @deprecated Use {@link #runSubscription(Long)}. Since v6.2
     */
    void runSubscription(GenericValue subId) throws GenericEntityException;

    void runSubscription(Long subId) throws GenericEntityException;

    void runSubscription(ApplicationUser user, Long subId) throws GenericEntityException;

    /** @deprecated Use {@link #runSubscription(com.atlassian.jira.user.ApplicationUser, Long)} instead. Since v6.0.
     */
    void runSubscription(User user, Long subId) throws GenericEntityException;

    /**
     * Get a subscription by Id
     * @param subId Subscription Id
     * @return Subscription
     * @throws GenericEntityException
     * @since v6.2
     */
    FilterSubscription getFilterSubscription(Long subId) throws GenericEntityException;

    /**
     * @deprecated Since v6.2. Quartz internals are no longer supported through the JIRA api.
     *                         Use {@link #getFilterSubscription(Long)} instead
     */
    GenericValue getSubscriptionFromTriggerName(String triggerName) throws GenericEntityException;

    /**
     * @deprecated Since v6.2. Quartz internals are no longer supported through the JIRA api
     */
    Trigger getTriggerFromSubscription(GenericValue subscription);

    /**
     *
     * @param user           the current user performing this operation
     * @param subscriptionId identifies the subscription to update
     * @param groupName      (optional) the name of the group to receive the email
     * @param trigger        The trigger to update the subscription with
     * @throws DataAccessException if there is a problem persisting the data.
     *
     * @deprecated Since v6.2. Quartz internals are no longer supported through the JIRA api
     *                      use {@link #updateSubscription(ApplicationUser, Long, String, String, Boolean)}
     */
    void updateSubscription(ApplicationUser user, Long subscriptionId, String groupName, Trigger trigger, Boolean emailOnEmpty) throws DataAccessException;

    /**
     *
     * @param user           the current user performing this operation
     * @param subscriptionId identifies the subscription to update
     * @param groupName      (optional) the name of the group to receive the email
     * @param cronExpression The Cron expression to update the subscription with
     * @throws DataAccessException if there is a problem persisting the data.
     */
    void updateSubscription(ApplicationUser user, Long subscriptionId, String groupName, String cronExpression, Boolean emailOnEmpty) throws DataAccessException;

    /**
     * @deprecated Use use {@link #updateSubscription(ApplicationUser, Long, String, String, Boolean)} instead. Since v6.0.
     *
     * @param user           the current user performing this operation
     * @param subscriptionId identifies the subscription to update
     * @param groupName      (optional) the name of the group to receive the email
     * @param trigger        The trigger to update the subscription with
     * @throws DataAccessException if there is a problem persisting the data.
     */
    void updateSubscription(User user, Long subscriptionId, String groupName, Trigger trigger, Boolean emailOnEmpty) throws DataAccessException;

    List<FilterSubscription> getAllFilterSubscriptions(Long filterId);

    List<FilterSubscription> getAllFilterSubscriptions();

    /**
     * @deprecated Use use {@link #getAllFilterSubscriptions(Long filterId)} instead. Since v6.2.
     */
    List<GenericValue> getAllSubscriptions(Long filterId);

    /**
     * @deprecated Use use {@link #getAllFilterSubscriptions()} instead. Since v6.2.
     */
    List<GenericValue> getAllSubscriptions();

    void deleteSubscriptionsForUser(ApplicationUser user) throws GenericEntityException;

    /**
     * @deprecated Use {@link #deleteSubscriptionsForUser(ApplicationUser)} instead. Since v6.0.
     */
    void deleteSubscriptionsForUser(User user) throws GenericEntityException;

    void deleteSubscriptionsForGroup(Group group) throws GenericEntityException;

    /**
     * Retrieve the cron expression associated with this subscription
     * @param subscription
     * @return the cron expression associated with this subscription
     */
    String getCronExpressionForSubscription(FilterSubscription subscription);

    /**
     * Returns the next send time for this subscription.
     * This may return null if the scheduler does not support the reporting of next send times.
     * @param sub The subscription
     * @return Next send time
     */
    @Nullable
    Date getNextSendTime(@Nonnull FilterSubscription sub);
}
