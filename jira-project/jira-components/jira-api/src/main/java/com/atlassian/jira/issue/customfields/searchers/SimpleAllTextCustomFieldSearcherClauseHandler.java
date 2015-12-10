package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.query.operator.Operator;

import java.util.Set;

/**
 * Implements the {@link AllTextCustomFieldSearcherClauseHandler} marker interface.
 * The supported operators must also include the LIKE operator to be included in the "all text" clause.
 *
 * @see com.atlassian.jira.issue.customfields.searchers.AllTextCustomFieldSearcherClauseHandler
 * @since v4.0
 */
@NonInjectableComponent
public class SimpleAllTextCustomFieldSearcherClauseHandler extends SimpleCustomFieldSearcherClauseHandler implements AllTextCustomFieldSearcherClauseHandler
{
    public SimpleAllTextCustomFieldSearcherClauseHandler(final ClauseValidator validator, final ClauseQueryFactory clauseQueryFactory,
            final Set<Operator> supportedOperators, final JiraDataType supportedType)
    {
        super(validator, clauseQueryFactory, supportedOperators, supportedType);
    }
}
