package com.atlassian.jira.jql.operand;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;

import java.util.List;

/**
 * Responsible for validating {@link com.atlassian.query.operand.Operand}s and extracting the
 * {@link com.atlassian.jira.jql.operand.QueryLiteral} values from them.
 *
 * @since v4.0
 */
public interface JqlOperandResolver
{

     MessageSet validate(User searcher, Operand operand, WasClause clause);
    
    /**
     * Return the values contained within the passed operand.
     *
     * @param searcher the user performing the search
     * @param operand the operand whose values should be returned. Must not be null.
     * @param terminalClause the terminal clause that contained the operand
     * @return a list of the values in the literal. May return null on an error.
     */
    List<QueryLiteral> getValues(User searcher, Operand operand, final TerminalClause terminalClause);

    /**
     * Return the values contained within the passed operand.
     *
     * @param queryCreationContext the context of query creation
     * @param operand the operand whose values should be returned. Must not be null.
     * @param terminalClause the terminal clause that contained the operand
     * @return a list of the values in the literal. May return null on an error.
     */
    List<QueryLiteral> getValues(QueryCreationContext queryCreationContext, Operand operand, final TerminalClause terminalClause);

    /**
     * Validates the operand against its handler.
     *
     * @param user the user who is validating. May be null.
     * @param operand the operand to be validated. Must not be null.
     * @param terminalClause the terminal clause that contained the operand
     * @return a {@link com.atlassian.jira.util.MessageSet} containing any errors reported. Note: if the operand is
     * unknown, an error message will be added to the message set returned. Never null.
     */
    MessageSet validate(User user, Operand operand, final TerminalClause terminalClause);

    /**
     * Sanitise a function operand for the specified user, so that information is not leaked.
     *
     * @param searcher the user performing the search
     * @param operand the operand to sanitise; will only be sanitised if valid
     * @return the sanitised operand; never null.
     */
    FunctionOperand sanitiseFunctionOperand(User searcher, FunctionOperand operand);

    /**
     * Returns the single value contained within the passed operand. If the operand contains more than one value, an
     * exception is thrown.
     *
     * @param user the user who is retrieving the values. May be null.
     * @param operand the operand whose values should be returned. Must not be null.
     * @param clause the terminal clause that contained the operand
     * @return the single value present in the operand, or null if there is no value.
     * @throws IllegalArgumentException if the operand contains more than one value.
     */
    QueryLiteral getSingleValue(User user, Operand operand, final TerminalClause clause);

    /**
     * Returns true if the operand represents an EMPTY operand.
     *
     * @param operand the operand to check if it is a EMPTY operand     
     * @return true if the operand is an EMPTY operand, false otherwise.
     */
    boolean isEmptyOperand(Operand operand);

    /**
     * Returns true if the passed operand is a function call.
     *
     * @param operand the operand to check. Cannot be null.
     * @return true of the passed operand is a function operand, false otherwise.
     */
    boolean isFunctionOperand(Operand operand);

    /**
     * Returns true if the passed operand returns a list of values.
     *
     * @param operand the operand to check. Cannot be null.
     * @return true if the passed operand returns a list of values or false otherwise.
     */
    boolean isListOperand(Operand operand);

    /**
     * Returns true if the operand is one which is known about. This is:
     *
     * <ul>
     * <li>{@link com.atlassian.query.operand.SingleValueOperand}s
     * <li>{@link com.atlassian.query.operand.MultiValueOperand}s
     * <li>{@link com.atlassian.query.operand.EmptyOperand}s
     * <li>{@link com.atlassian.query.operand.FunctionOperand}s registered as {@link com.atlassian.jira.plugin.jql.function.JqlFunction}s
     * </ul>
     *
     * @param operand the operand; cannot be null.
     * @return true if it is known, false otherwise.
     */
    boolean isValidOperand(Operand operand);


}
