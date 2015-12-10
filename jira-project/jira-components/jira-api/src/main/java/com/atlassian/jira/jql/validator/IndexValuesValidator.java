package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.IndexValueConverter;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;

import java.util.List;

/**
 * A Clause Validator for validating the values of fields with raw index values (votes, numbers).
 *
 */
abstract class IndexValuesValidator implements ClauseValidator
{
    private final JqlOperandResolver jqlOperandResolver;
    private final IndexValueConverter indexValueConverter;
    private final boolean emptyValuesSupported;

    IndexValuesValidator(final JqlOperandResolver jqlOperandResolver, IndexValueConverter indexValueConverter)
    {
        this(jqlOperandResolver, indexValueConverter, true);
    }

    IndexValuesValidator(final JqlOperandResolver jqlOperandResolver, final IndexValueConverter indexValueConverter, final boolean emptyValuesSupported)
    {
        this.jqlOperandResolver = jqlOperandResolver;
        this.indexValueConverter = indexValueConverter;
        this.emptyValuesSupported = emptyValuesSupported;
    }

    abstract void addError(final MessageSet messageSet, final User searcher, TerminalClause terminalClause, final QueryLiteral literal);

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        Operand operand = terminalClause.getOperand();
        MessageSet messageSet = new MessageSetImpl();
        final List<QueryLiteral> literals = jqlOperandResolver.getValues(searcher, operand, terminalClause);
        if (literals != null)
        {
            for (QueryLiteral literal : literals)
            {
                if (!literal.isEmpty() && indexValueConverter.convertToIndexValue(literal) == null)
                {
                    addError(messageSet, searcher, terminalClause, literal);
                }
                else if (literal.isEmpty() && !emptyValuesSupported)
                {
                    addError(messageSet, searcher, terminalClause, literal);
                }
            }
        }
        return messageSet;
    }
}
