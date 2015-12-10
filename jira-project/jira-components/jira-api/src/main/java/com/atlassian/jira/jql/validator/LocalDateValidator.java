package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlLocalDateSupport;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * All the date validators essentially do the same thing - validate against operators and then against the date values.
 *
 * @since v4.4
 */
public class LocalDateValidator implements ClauseValidator
{
    private final SupportedOperatorsValidator supportedOperatorsValidator;
    private final LocalDateValueValidator localDateValueValidator;

    public LocalDateValidator(final JqlOperandResolver operandResolver, JqlLocalDateSupport jqlLocalDateSupport)
    {
        notNull("operandResolver", operandResolver);
        this.localDateValueValidator = new LocalDateValueValidator(operandResolver, jqlLocalDateSupport);
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        MessageSet errors = supportedOperatorsValidator.validate(searcher, terminalClause);
        if (!errors.hasAnyErrors())
        {
            errors = localDateValueValidator.validate(searcher, terminalClause);
        }
        return errors;
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, OperatorClasses.RELATIONAL_ONLY_OPERATORS);
    }
}
