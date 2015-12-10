package com.atlassian.jira.jql.operand;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;

import java.util.List;

/**
 * Knows how to perform validation on and get expanded values from {@link com.atlassian.query.operand.Operand}s.
 *
 * @since v4.0
 */
public interface OperandHandler <T extends Operand>
{
    /**
     * Will perform operand specific validation.
     *
     * @param searcher the user performing the search
     * @param operand the operand to validate
     * @param terminalClause the terminal clause that contains the operand
     * @return a MessageSet which will contain any validation errors or warnings or will be empty if there is nothing to
     *         report, must not be null.
     */
    MessageSet validate(User searcher, T operand, final TerminalClause terminalClause);

    /**
     * Gets the unexpanded values provided by the user on input. In the case of a function this is the output
     * values that will later be transformed into index values.
     *
     * @param queryCreationContext the context of query creation
     * @param operand the operand to get values from
     * @param terminalClause the terminal clause that contains the operand
     * @return a List of objects that represent this Operands raw values. This must be the values specified by the user.
     */
    List<QueryLiteral> getValues(QueryCreationContext queryCreationContext, T operand, final TerminalClause terminalClause);

    /**
     * @return true if the operand represents a list of values, false otherwise.
     */
    boolean isList();

    /**
     * @return true if the operand represents the absence of a value, false otherwise.
     * {@see com.atlassian.query.operand.EmptyOperand}
     */
    boolean isEmpty();

    /**
     * @return true if the operand represents a function, false otherwise.
     * {@see com.atlassian.query.operand.FunctionOperand}
     */
    boolean isFunction();
}
