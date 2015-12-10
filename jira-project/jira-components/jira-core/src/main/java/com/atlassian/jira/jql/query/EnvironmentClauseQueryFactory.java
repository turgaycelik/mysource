package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

/**
 * Clause query factory for the environment system field.
 *
 * @since v4.0
 */
public class EnvironmentClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;

    ///CLOVER:OFF

    public EnvironmentClauseQueryFactory(JqlOperandResolver operandResolver)
    {
        delegateClauseQueryFactory = getDelegate(operandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }

    ClauseQueryFactory getDelegate(final JqlOperandResolver jqlOperandResolver)
    {
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        operatorFactories.add(new LikeQueryFactory());
        return new GenericClauseQueryFactory(SystemSearchConstants.forEnvironment(), operatorFactories, jqlOperandResolver);
    }

    ///CLOVER:ON
}
