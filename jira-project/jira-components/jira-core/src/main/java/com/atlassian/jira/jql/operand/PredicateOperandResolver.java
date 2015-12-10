package com.atlassian.jira.jql.operand;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.query.operand.Operand;

import java.util.List;

/**
 *  Responsible for validating {@link com.atlassian.query.operand.Operand}s and extracting the
 *  String values from them.
 *
 * @since v4.3
 */
public interface PredicateOperandResolver
{
    /**
     *
     * @param searcher    the {@link User} performing the lookup
     * @param field           a String representing the field over which you are searching
     * @param operand     the Operand containing the values used to search
     * @return   a List of values obtained from the operand
     */

    List<QueryLiteral> getValues(User searcher, String field, Operand operand);

           /**
     * Returns true if the operand represents an EMPTY operand.
     *
     * @param operand the operand to check if it is a EMPTY operand
     * @return true if the operand is an EMPTY operand, false otherwise.
     */
    boolean isEmptyOperand(User searcher, String field, Operand operand);

    /**
     * Returns true if the passed operand is a function call.
     *
     * @param operand the operand to check. Cannot be null.
     * @return true of the passed operand is a function operand, false otherwise.
     */
    boolean isFunctionOperand(User searcher, String field, Operand operand);

    /**
     * Returns true if the passed operand returns a list of values.
     *
     * @param operand the operand to check. Cannot be null.
     * @return true if the passed operand returns a list of values or false otherwise.
     */
    boolean isListOperand(User searcher, String field, Operand operand);

}
