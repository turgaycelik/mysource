package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.query.operator.Operator;

import java.util.Set;

/**
 * A handler suitable for date range searchers. Supports normalisation and sanitisation of clauses.
 *
 * @since v6.0
 */
public class DateRangeSearcherClauseHandler extends SimpleCustomFieldSearcherClauseHandler
{
    public DateRangeSearcherClauseHandler(ClauseValidator validator, ClauseQueryFactory clauseQueryFactory,
            Set<Operator> supportedOperators)
    {
        super(validator, clauseQueryFactory, supportedOperators, JiraDataTypes.DATE);
    }
}
