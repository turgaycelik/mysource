package com.atlassian.jira.jql.validator;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.timezone.TimeZoneManager;

/**
 * Clause validator for the Created Date system field.
 *
 * @since v4.0
 */
public class CreatedDateValidator extends DateValidator
{
    ///CLOVER:OFF

    public CreatedDateValidator(final JqlOperandResolver operandResolver, TimeZoneManager timeZoneManager)
    {
        super(operandResolver, timeZoneManager);
    }

    ///CLOVER:ON
}
