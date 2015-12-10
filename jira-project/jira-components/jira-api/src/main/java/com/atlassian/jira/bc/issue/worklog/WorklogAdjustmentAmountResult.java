package com.atlassian.jira.bc.issue.worklog;

import com.atlassian.annotations.PublicApi;

/**
 * <p>A more-specific {@link WorklogResult} which additionally defines an "adjustment amount" value.
 *
 * <p>To create instances of this class, see the {@link com.atlassian.jira.bc.issue.worklog.WorklogResultFactory}.
 *
 * @since v4.2
 * @see com.atlassian.jira.bc.issue.worklog.WorklogResult
 * @see com.atlassian.jira.bc.issue.worklog.WorklogResultFactory
 * @see com.atlassian.jira.bc.issue.worklog.WorklogService
 */
@PublicApi
public interface WorklogAdjustmentAmountResult extends WorklogResult
{
    /**
     * @return the adjustment amount in seconds to use
     *
     * @see WorklogService#createWithManuallyAdjustedEstimate(com.atlassian.jira.bc.JiraServiceContext, WorklogAdjustmentAmountResult,boolean)
     * @see WorklogService#deleteWithManuallyAdjustedEstimate(com.atlassian.jira.bc.JiraServiceContext, WorklogAdjustmentAmountResult,boolean)
     */
    Long getAdjustmentAmount();
}
