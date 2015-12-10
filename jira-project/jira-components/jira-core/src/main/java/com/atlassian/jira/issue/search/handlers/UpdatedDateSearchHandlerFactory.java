package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.UpdatedDateSearcher;
import com.atlassian.jira.jql.context.SimpleClauseContextFactory;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.UpdatedDateClauseQueryFactory;
import com.atlassian.jira.jql.validator.UpdatedDateValidator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the {@link com.atlassian.jira.issue.search.handlers.UpdatedDateSearchHandlerFactory}.
 *
 * @since v4.0
 */
@InjectableComponent
public final class UpdatedDateSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public UpdatedDateSearchHandlerFactory(final ComponentFactory componentFactory, UpdatedDateClauseQueryFactory factory,
            UpdatedDateValidator validator, final FieldClausePermissionChecker.Factory clausePermissionFactory)
    {
        super(componentFactory, SystemSearchConstants.forUpdatedDate(), 
                UpdatedDateSearcher.class, factory, validator, clausePermissionFactory,
                new SimpleClauseContextFactory(), null);
    }
}
