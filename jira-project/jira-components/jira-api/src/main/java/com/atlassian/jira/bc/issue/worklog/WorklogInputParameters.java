package com.atlassian.jira.bc.issue.worklog;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bc.issue.visibility.Visibility;
import com.atlassian.jira.issue.Issue;

import java.util.Date;

/**
 * <p>An aggregate of the input parameters required to validate and perform worklog creation and update service calls.
 * This interface has two sub-interfaces: {@link WorklogNewEstimateInputParameters} and
 * {@link WorklogAdjustmentAmountInputParameters}. They are used for specifying additional information to validation
 * service calls.
 *
 * <p>To build instances of this class, see the {@link com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl}
 * builder methods.
 *
 * @since v4.2
 * @see WorklogInputParametersImpl#builder()
 * @see WorklogInputParametersImpl#issue(com.atlassian.jira.issue.Issue)
 * @see WorklogInputParametersImpl#timeSpent(String)
 */
@PublicApi
public interface WorklogInputParameters
{
    /**
     * @return identifies the worklog that the update validation will occur against
     */
    Long getWorklogId();

    /**
     * @return issue that will have a worklog associated with it
     */
    Issue getIssue();

    /**
     * @return the time spent on the work
     */
    String getTimeSpent();

    /**
     * @return the date on which the work was performed
     */
    Date getStartDate();

    /**
     * @return The body of the comment associated with the worklog
     */
    String getComment();

    /**
     * @return The group level visibility of the worklog
     */
    String getGroupLevel();

    /**
     * @return The role level id visibility of the worklog
     */
    String getRoleLevelId();

    /**
     * @return The visibility level of the worklog
     */
    Visibility getVisibility();

    /**
     * @return do we require a check to be performed to see if the issue is in an editable state?
     */
    boolean isEditableCheckRequired();

    /**
     * @return the prefix to use when populating the error collection against specific fields. May be null to signify
     * no prefix.
     */
    String getErrorFieldPrefix();
}
