package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.CommentQuerySearcher;
import com.atlassian.jira.jql.context.SimpleClauseContextFactory;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.CommentClauseQueryFactory;
import com.atlassian.jira.jql.validator.CommentValidator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the comments clause.
 *
 * @since v4.0
 */
@InjectableComponent
public final class CommentSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public CommentSearchHandlerFactory(final ComponentFactory componentFactory, CommentClauseQueryFactory queryFactory,
            CommentValidator queryValidator, final FieldClausePermissionChecker.Factory clausePermissionFactory)
    {
        super(componentFactory, SystemSearchConstants.forComments(),
                CommentQuerySearcher.class, queryFactory, queryValidator, clausePermissionFactory,
                new SimpleClauseContextFactory(), null);
    }
}
