package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.EnvironmentQuerySearcher;
import com.atlassian.jira.jql.context.SimpleClauseContextFactory;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.EnvironmentClauseQueryFactory;
import com.atlassian.jira.jql.validator.EnvironmentValidator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the description field.
 *
 * @since v4.0
 */
@InjectableComponent
public final class EnvironmentSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public EnvironmentSearchHandlerFactory(final ComponentFactory componentFactory, EnvironmentClauseQueryFactory queryFactory,
            EnvironmentValidator queryValidator, final FieldClausePermissionChecker.Factory clausePermissionFactory)
    {
        super(componentFactory, SystemSearchConstants.forEnvironment(),
                EnvironmentQuerySearcher.class, queryFactory, queryValidator, clausePermissionFactory,
                new SimpleClauseContextFactory(), null);
    }
}
