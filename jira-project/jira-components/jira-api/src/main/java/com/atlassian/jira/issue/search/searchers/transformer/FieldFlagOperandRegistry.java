package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.query.operand.Operand;

import java.util.Set;

/**
 * Provides a registry for a field, navigator flag values and operand associations.
 *
 * @since v4.0
 */
public interface FieldFlagOperandRegistry
{
    /**
     * Retrieves the operand associated with a field and navigator flag value pair.
     * For example the issuetype field has the -2 navigator flag value which maps to the
     * {@link com.atlassian.query.operand.FunctionOperand} corresponding to the
     * {@link com.atlassian.jira.plugin.jql.function.AllStandardIssueTypesFunction}
     *  
     * @param fieldName The name of the field
     * @param flagValue The navigator flag value
     * @return the operand associated with the field and navigator flag value; null if there is none
     */
    Operand getOperandForFlag(String fieldName, String flagValue);

    /**
     * Retrieves the navigator flag values associated with the field name and operand pair.
     *
     * @param fieldName The name of the field
     * @param operand the {@link Operand}
     * @return The navigator flag value associated with the field name and operand pair; null if there is none
     */
    Set<String> getFlagForOperand(String fieldName, Operand operand);
}
