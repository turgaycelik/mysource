package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IssueConstantInfoResolver;
import com.atlassian.jira.jql.resolver.StatusResolver;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

/**
 * Clause query factory that creates the clauses for the status field.
 * Only supports equality operators.
 *
 * @since v4.0
 */
public class StatusClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;

    public StatusClauseQueryFactory(StatusResolver statusResolver, JqlOperandResolver operandResolver)
    {
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        final IssueConstantInfoResolver<Status> constantInfoResolver = new IssueConstantInfoResolver<Status>(statusResolver);
        operatorFactories.add(new EqualityQueryFactory<Status>(constantInfoResolver));
        delegateClauseQueryFactory = new GenericClauseQueryFactory(SystemSearchConstants.forStatus(), operatorFactories, operandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }
}
