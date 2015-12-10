package com.atlassian.jira.jql.query;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.parameters.lucene.PermissionQueryFactory;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.PermissionClauseQueryFactory.CurrentUser.Presence;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.UserIndexInfoResolver;
import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.Predicate;
import com.atlassian.query.clause.TerminalClause;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.collect.CollectionUtil.contains;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Class that decorates another {@link ClauseQueryFactory} and adds a
 * permission query to it. This class also handles the case where you
 * can override this permission check if the user is looking for
 * themselves by passing a {@link CurrentUser} implementation.
 *
 * @since 4.1
 */
class PermissionClauseQueryFactory implements ClauseQueryFactory
{
    /**
     * Static factory for 
     * 
     * @param operandResolver for resolving the operand values
     * @param userResolver operand values are user names
     * @param permissionQueryFactory for getting our permission queries
     * @param field the field name
     * @return a {@link PermissionClauseQueryFactory} configured with the View Votes and Watches permission
     */
    static PermissionClauseQueryFactory create(final JqlOperandResolver operandResolver, final UserResolver userResolver, final PermissionQueryFactory permissionQueryFactory, final String field)
    {
        final UserIndexInfoResolver indexInfoResolver = new UserIndexInfoResolver(userResolver);
        final List<OperatorSpecificQueryFactory> operatorQueryFactory = Collections.<OperatorSpecificQueryFactory> singletonList(new EqualityQueryFactory<com.atlassian.crowd.embedded.api.User>(
            indexInfoResolver));
        final GenericClauseQueryFactory clauseQueryFactory = new GenericClauseQueryFactory(field, operatorQueryFactory, operandResolver);
        final PermissionClauseQueryFactory permissionClauseQueryFactory = new PermissionClauseQueryFactory(clauseQueryFactory,
            permissionQueryFactory, operandResolver, Permissions.VIEW_VOTERS_AND_WATCHERS, new PermissionClauseQueryFactory.OverrideField(field),
            indexInfoResolver);
        return permissionClauseQueryFactory;
    }

    private final ClauseQueryFactory clauseQueryFactory;
    private final PermissionQueryFactory permissionQueryFactory;
    private final JqlOperandResolver operandResolver;
    private final CurrentUser currentUser;
    private final IndexInfoResolver<com.atlassian.crowd.embedded.api.User> infoResolver;
    private final int permissionId;


    PermissionClauseQueryFactory(final ClauseQueryFactory clauseQueryFactory, final PermissionQueryFactory permissionQueryFactory, final JqlOperandResolver operandResolver, final int permissionId, final CurrentUser currentUser, final IndexInfoResolver<User> infoResolver)
    {
        this.clauseQueryFactory = notNull("clauseQueryFactory", clauseQueryFactory);
        this.permissionQueryFactory = notNull("permissionQueryFactory", permissionQueryFactory);
        this.operandResolver = notNull("operandResolver", operandResolver);
        this.currentUser = notNull("currentUser", currentUser);
        this.infoResolver = notNull("infoResolver", infoResolver);
        this.permissionId = permissionId;
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        final QueryFactoryResult delegateResult = clauseQueryFactory.getQuery(queryCreationContext, terminalClause);
        if (queryCreationContext.isSecurityOverriden())
        {
            return delegateResult;
        }

        final Query permissionQuery = getPermissionQuery(queryCreationContext, terminalClause);
        if (permissionQuery == null)
        {
            return delegateResult;
        }
        final BooleanQuery query = new BooleanQuery();
        query.add(delegateResult.getLuceneQuery(), Occur.MUST);
        query.add(permissionQuery, Occur.MUST);
        return new QueryFactoryResult(query, delegateResult.mustNotOccur());
    }

    Query getPermissionQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        final ApplicationUser user = ApplicationUsers.from(queryCreationContext.getUser());
        final List<QueryLiteral> values = operandResolver.getValues(queryCreationContext, terminalClause.getOperand(), terminalClause);
        final Presence userPresence = CurrentUser.Presence.get(user, values, infoResolver);
        return currentUser.rewrite(permissionQueryFactory.getQuery(user, permissionId), userPresence, user, infoResolver);
    }

    /**
     * Handler for dealing with the current user. Some permissions
     * need to be ignored if we are searching for the current user.
     */
    interface CurrentUser
    {
        Query rewrite(Query query, Presence userInLiterals, ApplicationUser user, IndexInfoResolver<User> resolver);

        enum Presence
        {
            NOT,
            CONTAINS,
            ONLY;

            static Presence get(final ApplicationUser user, final Collection<QueryLiteral> values, final IndexInfoResolver<User> resolver)
            {
                final boolean hasUser = (user != null) && contains(values, new UserEquals(user, resolver));
                return (!hasUser) ? Presence.NOT : values.size() == 1 ? Presence.ONLY : Presence.CONTAINS;
            }
        }
    }

    /**
     * Override security and add an (OR field:user) IF the current user is in the search.
     * If the search is only for the current user then no need to do anything (return null).
     */
    static class OverrideField implements CurrentUser
    {
        private final String field;

        public OverrideField(final String field)
        {
            this.field = field;
        }

        public Query rewrite(final Query query, final Presence hasUser, final ApplicationUser user, final IndexInfoResolver<User> resolver)
        {
            switch (hasUser)
            {
                case NOT:
                    // the user isn't in the search terms return just the permission query
                    return query;
                case ONLY:
                    // only the user is in the search terms so return a blank query
                    return null;
                case CONTAINS:
                {
                    final BooleanQuery result = new BooleanQuery();
                    result.add(new TermQuery(new Term(field, user.getKey())), Occur.SHOULD);
                    result.add(query, Occur.SHOULD);
                    return result;
                }
                default:
                    throw new IllegalArgumentException("Unknown Presence: " + hasUser);
            }
        }
    }

    static class UserEquals implements Predicate<QueryLiteral>
    {
        private final String expectedUserKey;
        private final IndexInfoResolver<User> infoResolver;

        UserEquals(final ApplicationUser user, final IndexInfoResolver<User> infoResolver)
        {
            this.expectedUserKey = user.getKey();
            this.infoResolver = infoResolver;
        }

        public boolean evaluate(final QueryLiteral literal)
        {
            if (literal.isEmpty())
            {
                return false;
            }
            for (final String userKey : infoResolver.getIndexedValues(literal.asString()))
            {
                if (expectedUserKey.equals(userKey))
                {
                    return true;
                }
            }
            return false;
        }
    }
}
