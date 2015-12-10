package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.comparator.ResolutionObjectComparator;
import com.atlassian.jira.issue.index.indexers.impl.BaseFieldIndexer;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.ResolutionIndexInfoResolver;
import com.atlassian.jira.jql.resolver.ResolutionResolver;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates queries for resolution clauses.
 *
 * @since v4.0
 */
public class ResolutionClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;

    public ResolutionClauseQueryFactory(ResolutionResolver resolutionResolver, JqlOperandResolver operandResolver)
    {
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        final ResolutionIndexInfoResolver resolutionIndexInfoResolver = new ResolutionIndexInfoResolver(resolutionResolver);
        operatorFactories.add(new EqualityWithSpecifiedEmptyValueQueryFactory<Resolution>(resolutionIndexInfoResolver, BaseFieldIndexer.NO_VALUE_INDEX_VALUE));
        operatorFactories.add(new RelationalOperatorIdIndexValueQueryFactory<Resolution>(ResolutionObjectComparator.RESOLUTION_OBJECT_COMPARATOR, resolutionResolver, resolutionIndexInfoResolver));
        delegateClauseQueryFactory = new GenericClauseQueryFactory(SystemSearchConstants.forResolution(), operatorFactories, operandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }
}
