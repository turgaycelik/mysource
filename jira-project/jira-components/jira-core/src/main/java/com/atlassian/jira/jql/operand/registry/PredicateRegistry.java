package com.atlassian.jira.jql.operand.registry;


import com.atlassian.jira.jql.values.ClauseValuesGenerator;


/**
 * registry for {@link com.atlassian.query.operator.Operator}s. Resolves a Predicate and returns the
 * most appropriate {@link com.atlassian.jira.jql.ClauseHandler} handler .
 *
 * @since v4.4
 */
public interface PredicateRegistry
{
    ClauseValuesGenerator getClauseValuesGenerator(String predicateName, String fieldName);
}
