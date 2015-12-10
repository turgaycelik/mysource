package com.atlassian.jira.bc.issue.worklog;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.worklog.Worklog;

/**
 * <p>An interface which defines the base result of a {@link WorklogService} validation call. This interface has two
 * sub-interfaces for specifying {@link WorklogNewEstimateResult} and {@link WorklogAdjustmentAmountResult}, which are
 * the expected input types for their respective "do" service calls.
 *
 * <p>To create instances of this class, see the {@link com.atlassian.jira.bc.issue.worklog.WorklogResultFactory}.
 *
 * @since v4.2
 * @see com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateResult
 * @see com.atlassian.jira.bc.issue.worklog.WorklogAdjustmentAmountResult
 * @see com.atlassian.jira.bc.issue.worklog.WorklogResultFactory
 * @see com.atlassian.jira.bc.issue.worklog.WorklogService
 */
@PublicApi
public interface WorklogResult
{
    /**
     * @return the worklog constructed during validation, possibly null.
     */
    Worklog getWorklog();

    /**
     * @return true if the editable issue check is required; false otherwise. Note that this should only be false when
     * creating worklogs during issue transitions.
     */
    boolean isEditableCheckRequired();
}
