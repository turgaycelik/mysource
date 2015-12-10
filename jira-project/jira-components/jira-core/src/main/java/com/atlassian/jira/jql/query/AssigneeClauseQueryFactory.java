package com.atlassian.jira.jql.query;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstantsWithEmpty;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.UserIndexInfoResolver;
import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

/**
 * Clause query factory that creates the clauses for the assginee field.
 * Only supports equality operators.
 *
 * @since v4.0
 */
public class AssigneeClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;

    public AssigneeClauseQueryFactory(UserResolver userResolver, JqlOperandResolver operandResolver)
    {
        final UserFieldSearchConstantsWithEmpty searchConstants = SystemSearchConstants.forAssignee();
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        final UserIndexInfoResolver indexInfoResolver = new UserIndexInfoResolver(userResolver);
        operatorFactories.add(new EqualityWithSpecifiedEmptyValueQueryFactory<User>(indexInfoResolver, searchConstants.getEmptyIndexValue()));
        delegateClauseQueryFactory = new GenericClauseQueryFactory(searchConstants.getIndexField(), operatorFactories, operandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }
}
