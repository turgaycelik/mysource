package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.comparator.PriorityObjectComparator;
import com.atlassian.jira.issue.index.indexers.impl.BaseFieldIndexer;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IssueConstantInfoResolver;
import com.atlassian.jira.jql.resolver.PriorityResolver;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates queries for priority clauses.
 *
 * @since v4.0
 */
public class PriorityClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;

    public PriorityClauseQueryFactory(PriorityResolver priorityResolver, JqlOperandResolver operandResolver)
    {
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        final IssueConstantInfoResolver<Priority> constantInfoResolver = new IssueConstantInfoResolver<Priority>(priorityResolver);
        operatorFactories.add(new EqualityWithSpecifiedEmptyValueQueryFactory<Priority>(constantInfoResolver, BaseFieldIndexer.NO_VALUE_INDEX_VALUE));
        operatorFactories.add(new RelationalOperatorIdIndexValueQueryFactory<Priority>(PriorityObjectComparator.PRIORITY_OBJECT_COMPARATOR, priorityResolver, constantInfoResolver));
        delegateClauseQueryFactory = new GenericClauseQueryFactory(SystemSearchConstants.forPriority(), operatorFactories, operandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }
}
