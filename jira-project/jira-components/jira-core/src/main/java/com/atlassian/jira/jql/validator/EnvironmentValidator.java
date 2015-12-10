package com.atlassian.jira.jql.validator;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;

/**
 * A clause validator for environment system field.
 *
 * @since v4.0
 */
public class EnvironmentValidator extends FreeTextFieldValidator
{
    ///CLOVER:OFF
    public EnvironmentValidator(JqlOperandResolver operandResolver)
    {
        super(SystemSearchConstants.forEnvironment().getIndexField(), operandResolver);
    }
    ///CLOVER:ON
}
