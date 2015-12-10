package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.Issue;

/**
 * A util object that calculates an issue's aggregate Time Tracking values.
 *
 * @since v3.11
 */
public interface AggregateTimeTrackingCalculator
{
    /**
     * Method to retrieve a bean that contains all Aggregate Time Tracking information.
     * An aggregate in considered the value of the sub-task values plus the issue's own value.
     *
     * @param issue issue to calculate aggregates for
     * @return The bean containing all aggregate values.
     */
    public AggregateTimeTrackingBean getAggregates(Issue issue);
}
