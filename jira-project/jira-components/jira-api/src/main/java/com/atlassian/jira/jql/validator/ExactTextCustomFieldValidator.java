package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;

/**
 * A validator for exact text custom fields.
 *
 * @since v4.0
 */
public class ExactTextCustomFieldValidator implements ClauseValidator
{
    private final SupportedOperatorsValidator supportedOperatorsValidator;
    //CLOVER:OFF
    public ExactTextCustomFieldValidator()
    {
        supportedOperatorsValidator = getSupportedOperatorsValidator();
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        return supportedOperatorsValidator.validate(searcher, terminalClause);
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);
    }
    ///CLOVER:ON
}
