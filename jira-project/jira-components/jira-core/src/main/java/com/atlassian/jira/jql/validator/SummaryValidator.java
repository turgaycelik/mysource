package com.atlassian.jira.jql.validator;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;

/**
 * A validator for the summary field that is a simple wrapper around the text field validator.
 *
 * @since v4.0
 */
public class SummaryValidator extends FreeTextFieldValidator
{
    ///CLOVER:OFF
    public SummaryValidator(JqlOperandResolver operandResolver)
    {
        super(SystemSearchConstants.forSummary().getIndexField(), operandResolver);
    }
    ///CLOVER:ON
}
