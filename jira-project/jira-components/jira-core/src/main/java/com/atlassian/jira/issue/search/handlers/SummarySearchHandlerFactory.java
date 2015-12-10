package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.SummaryQuerySearcher;
import com.atlassian.jira.jql.context.SimpleClauseContextFactory;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.SummaryClauseQueryFactory;
import com.atlassian.jira.jql.validator.SummaryValidator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the {@link SummarySearchHandlerFactory}.
 *
 * @since v4.0
 */
@InjectableComponent
public final class SummarySearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public SummarySearchHandlerFactory(final ComponentFactory componentFactory, SummaryClauseQueryFactory queryFactory,
            SummaryValidator queryValidator, final FieldClausePermissionChecker.Factory clausePermissionFactory)
    {
        super(componentFactory, SystemSearchConstants.forSummary(), SummaryQuerySearcher.class,
                queryFactory, queryValidator, clausePermissionFactory,
                new SimpleClauseContextFactory(), null);
    }
}
