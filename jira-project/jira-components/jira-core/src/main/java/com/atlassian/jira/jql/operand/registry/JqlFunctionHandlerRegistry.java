package com.atlassian.jira.jql.operand.registry;

import com.atlassian.jira.jql.operand.FunctionOperandHandler;
import com.atlassian.query.operand.FunctionOperand;

import java.util.List;

/**
 * Registry for {@link com.atlassian.jira.plugin.jql.function.JqlFunction}s. Can resolve a
 * {@link com.atlassian.jira.jql.operand.OperandHandler} of type {@link com.atlassian.query.operand.FunctionOperand}
 * for a provided {@link com.atlassian.query.operand.FunctionOperand}. The handler returned wraps a
 * registered {@link com.atlassian.jira.plugin.jql.function.JqlFunction}.
 *
 * @since v4.0
 */
public interface JqlFunctionHandlerRegistry
{
    /**
     * Fetches the associated OperandHandler for the provided FunctionOperand. The returned handler is looked up by the
     * name of the FunctionOperand (case insensitive).
     *
     * @param operand that defines the name for which we want to find the operand handler.
     * @return the operand handler associated with this operand, null if there is none.
     */
    FunctionOperandHandler getOperandHandler(FunctionOperand operand);

    /**
     * Fetches all function names ordered alphabetically.
     *
     * @return all function names ordered alphabetically, an empty collection if there are none.
     */
    List<String> getAllFunctionNames();
}
