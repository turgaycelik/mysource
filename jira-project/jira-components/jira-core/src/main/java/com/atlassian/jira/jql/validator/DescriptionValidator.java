package com.atlassian.jira.jql.validator;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;

/**
 * A clause validator for the description system field.
 *
 * @since v4.0
 */
public class DescriptionValidator extends FreeTextFieldValidator
{
    ///CLOVER:OFF
    public DescriptionValidator(JqlOperandResolver operandResolver)
    {
        super(SystemSearchConstants.forDescription().getIndexField(), operandResolver);
    }
    ///CLOVER:ON
}
