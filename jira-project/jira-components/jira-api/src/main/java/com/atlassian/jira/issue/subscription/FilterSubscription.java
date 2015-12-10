package com.atlassian.jira.issue.subscription;

import java.util.Date;

import javax.annotation.Nullable;

import com.atlassian.jira.user.ApplicationUser;

/**
 * A Filter Subscription Entity Bean
 *
 * @since v6.2
 */
public interface FilterSubscription
{
    /**
     * Get the Id of the subscription.
     * @return the Id of the subscription.
     */
    long getId();

    /**
     * Get the Id of the filter subscribed to.
     * @return the Id of the filter subscribed to.
     */
    long getFilterId();

    /**
     * Get the User Key of the subscription owner.
     * @return the  User Key of the subscription owner.
     */
    String getUserKey();

    /**
     * Get the name of the group subscribed.
     * @return the  User Key of the group subscribed.
     */
    @Nullable
    String getGroupName();

    /**
     * Get the time the subscription was last sent.
     * @return the time the subscription was last sent.
     */
    @Nullable
    Date getLastRunTime();

    /**
     * Is this subscription sent, even if no issues are selected by the filter.
     * @return true if this subscription sent, even if no issues are selected by the filter.
     */
    boolean isEmailOnEmpty();
}
