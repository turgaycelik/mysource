package com.atlassian.jira.bc.filter;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.subscription.FilterSubscription;
import com.atlassian.jira.user.ApplicationUser;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Provides high level access to CRUD and query FilterSubscriptions.
 */
@PublicApi
public interface FilterSubscriptionService
{
    /**
     * Validates a given cron expression
     * Errors are passed back in the {@link com.atlassian.jira.util.ErrorCollection}
     * of the {@link com.atlassian.jira.bc.JiraServiceContext}
     *
     * @param context jira service context
     * @param expr    Expression to evaluate
     */
    public void validateCronExpression(JiraServiceContext context, String expr);

    /**
     * Create and store the Cron Trigger and subscription
     *
     * @param context      jira service context
     * @param filterId     Id of the filter subscribing to
     * @param groupName    Sent to group (may be null if sent to self)
     * @param expr         Cron expression to store
     * @param emailOnEmpty send email if filter returns no results
     */
    public void storeSubscription(JiraServiceContext context, Long filterId, String groupName, String expr, boolean emailOnEmpty);

    /**
     * Updates the subscription to the new given values and persists.
     *
     * @param context        jira service context
     * @param subscriptionId Id of the subscription being updated
     * @param groupName      Sent to group (may be null if sent to self)
     * @param expr           Cron expression to store
     * @param emailOnEmpty   send email if filter returns no results
     */
    public void updateSubscription(JiraServiceContext context, Long subscriptionId, String groupName, String expr, boolean emailOnEmpty);

    /**
     * Renders a human readable description of the given cron expression or returns the cron expression if
     * it can't be parsed by the {@link com.atlassian.core.cron.parser.CronExpressionParser}.
     *
     * @param context        the jira service context.
     * @param cronExpression a cron expression.
     * @return a locale-specific sentence describing the cron string (or on failure, the cron string).
     */
    public String getPrettySchedule(JiraServiceContext context, String cronExpression);

    /**
     * Retrieves a list of subscriptions that a given user can see for a given filter.  I.e. The owner can see all
     * subscriptions for a filter, otherwise you can only see your own subscriptions.
     *
     * @param user   The user that can see the subscriptions
     * @param filter The filter with teh associated subscriptions
     * @return A Collection of GenericValue subscriptions
     */
    public Collection<FilterSubscription> getVisibleFilterSubscriptions(ApplicationUser user, SearchRequest filter);

    /**
     * Retrieves a list of subscriptions that a given user can see for a given filter.  I.e. The owner can see all
     * subscriptions for a filter, otherwise you can only see your own subscriptions.
     *
     * @param user   The user that can see the subscriptions
     * @param filter The filter with teh associated subscriptions
     * @return A Collection of GenericValue subscriptions
     * @deprecated Use use {@link #getVisibleFilterSubscriptions(ApplicationUser, SearchRequest)} ()} instead. Since v6.2.
     */
    public Collection<GenericValue> getVisibleSubscriptions(ApplicationUser user, SearchRequest filter);

    /**
     * Retrieves a list of subscriptions that a given user can see for a given filter.  I.e. The owner can see all
     * subscriptions for a filter, otherwise you can only see your own subscriptions.
     *
     * @param user   The user that can see the subscriptions
     * @param filter The filter with teh associated subscriptions
     * @return A Collection of GenericValue subscriptions
     * @deprecated Use use {@link #getVisibleFilterSubscriptions(ApplicationUser, SearchRequest)} ()} instead. Since v6.2.
     */
    public Collection<GenericValue> getVisibleSubscriptions(User user, SearchRequest filter);

    /**
     * Retrieve the cron expression associated with this subscription
     * @param subscription
     * @return the cron expression associated with this subscription
     */
    String getCronExpression(JiraServiceContext context, FilterSubscription subscription);

    /**
     * Returns the next send time for this subscription.
     * This may return null if the scheduler does not support the reporting of next send times.
     * @param sub The subscription
     * @return Next send time
     */
    @Nullable
    Date getNextSendTime(@Nonnull FilterSubscription sub);
}

