package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlLocalDateSupport;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Creates clauses for queries on the due date field.
 *
 * @since v4.0
 */
public class DueDateClauseQueryFactory extends LocalDateClauseQueryFactory implements ClauseQueryFactory
{
    ///CLOVER:OFF

    public DueDateClauseQueryFactory(JqlLocalDateSupport jqlLocalDateSupport, JqlOperandResolver jqlOperandResolver)
    {
        super(SystemSearchConstants.forDueDate(), notNull("jqlLocalDateSupport", jqlLocalDateSupport), notNull("jqlOperandResolver", jqlOperandResolver));
    }
    ///CLOVER:ON
}
