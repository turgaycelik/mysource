package com.atlassian.jira.jql.operand;

import com.atlassian.query.operand.Operand;

import java.util.List;

/**
 * Return the values from the Operand
 *
 * @since v4.3
 */
public interface PredicateOperandHandler
{
    List<QueryLiteral> getValues();

    boolean isEmpty();

    boolean isList();

    boolean isFunction();
}
