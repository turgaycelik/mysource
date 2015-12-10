package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

/**
 * A clause query factory that handles the summary system field.
 *
 * @since v4.0
 */
public class SummaryClauseQueryFactory implements ClauseQueryFactory
{
    static final int SUMMARY_BOOST_FACTOR = 9;
    private final ClauseQueryFactory delegateClauseQueryFactory;

    public SummaryClauseQueryFactory(JqlOperandResolver operandResolver)
    {
        delegateClauseQueryFactory = getDelegate(operandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        final QueryFactoryResult queryFactoryResult = delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
        if (queryFactoryResult != null && queryFactoryResult.getLuceneQuery() != null)
        {
            // Summary always gets a boost of 9
            queryFactoryResult.getLuceneQuery().setBoost(SUMMARY_BOOST_FACTOR);
        }
        return queryFactoryResult;
    }

    ClauseQueryFactory getDelegate(final JqlOperandResolver operandResolver)
    {
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        operatorFactories.add(new LikeQueryFactory());
        return new GenericClauseQueryFactory(SystemSearchConstants.forSummary(), operatorFactories, operandResolver);
    }
}
