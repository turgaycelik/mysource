package com.atlassian.jira.jql.query;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.UserIndexInfoResolver;
import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

/**
 * Clause query factory that creates the clauses for user custom fields.
 * Only supports equality operators.
 *
 * @since v4.0
 */
public class UserCustomFieldClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;

    public UserCustomFieldClauseQueryFactory(final String documentConstant, UserResolver userResolver, JqlOperandResolver operandResolver)
    {
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        final UserIndexInfoResolver indexInfoResolver = new UserIndexInfoResolver(userResolver);
        operatorFactories.add(new EqualityQueryFactory<User>(indexInfoResolver));
        delegateClauseQueryFactory = new GenericClauseQueryFactory(documentConstant, operatorFactories, operandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }
}
