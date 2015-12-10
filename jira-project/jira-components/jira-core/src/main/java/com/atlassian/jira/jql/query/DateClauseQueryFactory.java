package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates clauses for queries on date-time fields.
 *
 * @since v4.0
 */
public class DateClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;

    ///CLOVER:OFF
    public DateClauseQueryFactory(SimpleFieldSearchConstants constants, JqlDateSupport jqlDateSupport, JqlOperandResolver operandResolver)
    {
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        operatorFactories.add(new DateEqualityQueryFactory(jqlDateSupport));
        operatorFactories.add(new DateRelationalQueryFactory(jqlDateSupport));
        this.delegateClauseQueryFactory = new GenericClauseQueryFactory(constants, operatorFactories, operandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }
    ///CLOVER:ON
}
