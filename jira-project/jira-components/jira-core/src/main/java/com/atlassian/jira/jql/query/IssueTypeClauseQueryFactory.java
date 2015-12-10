package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.IssueConstantInfoResolver;
import com.atlassian.jira.jql.resolver.IssueTypeResolver;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

/**
 * Clause query factory that creates the clauses for the issue type field.
 * Only supports equality operators.
 *
 * @since v4.0
 */
public class IssueTypeClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;

    public IssueTypeClauseQueryFactory(IssueTypeResolver issueTypeResolver, JqlOperandResolver operandResolver)
    {
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        final IndexInfoResolver<IssueType> indexInfoResolver = new IssueConstantInfoResolver<IssueType>(issueTypeResolver);
        operatorFactories.add(new EqualityQueryFactory<IssueType>(indexInfoResolver));
        delegateClauseQueryFactory = new GenericClauseQueryFactory(SystemSearchConstants.forIssueType(), operatorFactories, operandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }

}
