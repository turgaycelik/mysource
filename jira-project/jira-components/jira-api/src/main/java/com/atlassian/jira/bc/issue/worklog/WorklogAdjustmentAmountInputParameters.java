package com.atlassian.jira.bc.issue.worklog;

import com.atlassian.annotations.PublicApi;

/**
 * <p>A more-specific class of {@link WorklogInputParameters} required to validate and perform worklog creation and
 * update service calls which additionally manually adjust the remaining estimate by a specified amount.
 *
 * <p>To build instances of this class, see the {@link com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl}
 * builder methods. Moreover, you must use the correct <code>build</code> method to get the right return type.
 *
 * @since v4.2
 * @see WorklogInputParametersImpl#builder()
 * @see WorklogInputParametersImpl#issue(com.atlassian.jira.issue.Issue)
 * @see WorklogInputParametersImpl#timeSpent(String)
 * @see WorklogInputParametersImpl.Builder#buildAdjustmentAmount()
 */
@PublicApi
public interface WorklogAdjustmentAmountInputParameters extends WorklogInputParameters
{
    /**
     * @return The amount to adjust the issue's remaining estimate by.
     */
    String getAdjustmentAmount();
}