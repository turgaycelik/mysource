package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlDateSupport;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Creates clauses for queries on the created date field.
 *
 * @since v4.0
 */
public class CreatedDateClauseQueryFactory extends DateClauseQueryFactory implements ClauseQueryFactory
{
    //CLOVER:OFF
    public CreatedDateClauseQueryFactory(JqlDateSupport jqlDateSupport, JqlOperandResolver jqlOperandResolver)
    {
        super(SystemSearchConstants.forCreatedDate(), notNull("jqlDateSupport", jqlDateSupport), notNull("jqlOperandResolver", jqlOperandResolver));
    }
    //CLOVER:ON
}
