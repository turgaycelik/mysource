package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

/**
 * A query factory that generates lucene queries for the description system field.
 *
 * @since v4.0
 */
public class DescriptionClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;

    ///CLOVER:OFF

    public DescriptionClauseQueryFactory(JqlOperandResolver operandResolver)
    {
        delegateClauseQueryFactory = getDelegate(operandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }

    ClauseQueryFactory getDelegate(final JqlOperandResolver operandResolver)
    {
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        operatorFactories.add(new LikeQueryFactory());
        return new GenericClauseQueryFactory(SystemSearchConstants.forDescription(), operatorFactories, operandResolver);
    }

    ///CLOVER:ON
}
