package com.atlassian.jira.jql.validator;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlLocalDateSupport;

/**
 * Clause validator for the Due Date system field.
 *
 * @since v4.0
 */
public class DueDateValidator extends LocalDateValidator
{
    ///CLOVER:OFF

    public DueDateValidator(final JqlOperandResolver operandResolver, JqlLocalDateSupport jqlLocalDateSupport)
    {
        super(operandResolver, jqlLocalDateSupport);
    }

    ///CLOVER:ON

}
