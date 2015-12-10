package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlDateSupport;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Creates clauses for queries on the resolution date field.
 *
 * @since v4.0
 */
public class ResolutionDateClauseQueryFactory extends DateClauseQueryFactory implements ClauseQueryFactory
{
    ///CLOVER:OFF

    public ResolutionDateClauseQueryFactory(JqlDateSupport jqlDateSupport, JqlOperandResolver jqlOperandResolver)
    {
        super(SystemSearchConstants.forResolutionDate(), notNull("jqlDateSupport", jqlDateSupport), notNull("jqlOperandResolver", jqlOperandResolver));
    }

    ///CLOVER:ON
}
