package com.atlassian.jira.bc.issue.worklog;

import com.atlassian.annotations.PublicApi;

/**
 * <p>A more-specific {@link WorklogResult} which additionally defines a "new estimate" value.
 *
 * <p>To create instances of this class, see the {@link com.atlassian.jira.bc.issue.worklog.WorklogResultFactory}.
 *
 * @since v4.2
 * @see com.atlassian.jira.bc.issue.worklog.WorklogResult
 * @see com.atlassian.jira.bc.issue.worklog.WorklogResultFactory
 * @see com.atlassian.jira.bc.issue.worklog.WorklogService
 */
@PublicApi
public interface WorklogNewEstimateResult extends WorklogResult
{
    /**
     * @return the new estimate in seconds to set
     *
     * @see WorklogService#createWithNewRemainingEstimate(com.atlassian.jira.bc.JiraServiceContext, WorklogNewEstimateResult,boolean)
     * @see WorklogService#updateWithNewRemainingEstimate(com.atlassian.jira.bc.JiraServiceContext, WorklogNewEstimateResult,boolean)
     * @see WorklogService#deleteWithNewRemainingEstimate(com.atlassian.jira.bc.JiraServiceContext, WorklogNewEstimateResult,boolean)
     */
    Long getNewEstimate();
}
