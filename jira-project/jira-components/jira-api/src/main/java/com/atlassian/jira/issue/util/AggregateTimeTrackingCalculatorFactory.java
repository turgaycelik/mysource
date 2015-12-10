package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.Issue;

/**
 * A factory to return {@link com.atlassian.jira.issue.util.AggregateTimeTrackingCalculator} instances based on the type of
 * {@link com.atlassian.jira.issue.Issue} in play.
 *
 * @since v4.4
 */
public interface AggregateTimeTrackingCalculatorFactory
{
    /**
     * Create a new instance of time tracing calculator  based on the Issue implementation.
     * <p/>
     * Creates and returns a new instance of {@link DocumentIssueAggregateTimeTrackingCalculator} if passed in
     * a {@link com.atlassian.jira.issue.DocumentIssueImpl}, else creates and returns a new instance
     * of {@link IssueImplAggregateTimeTrackingCalculator}.
     *
     * @param issue Issue to compare
     * @return {@link DocumentIssueAggregateTimeTrackingCalculator} if passed in
     *         a {@link com.atlassian.jira.issue.DocumentIssueImpl}, else returns a {@link IssueImplAggregateTimeTrackingCalculator}
     */
    AggregateTimeTrackingCalculator getCalculator(Issue issue);
}
