package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.CreatedDateSearcher;
import com.atlassian.jira.jql.context.SimpleClauseContextFactory;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.CreatedDateClauseQueryFactory;
import com.atlassian.jira.jql.validator.CreatedDateValidator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the created date clause.
 *
 * @since v4.0
 */
@InjectableComponent
public final class CreatedDateSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public CreatedDateSearchHandlerFactory(final ComponentFactory componentFactory, CreatedDateClauseQueryFactory factory,
            CreatedDateValidator validator, final FieldClausePermissionChecker.Factory clausePermissionFactory)
    {
        super(componentFactory, SystemSearchConstants.forCreatedDate(),
                CreatedDateSearcher.class, factory, validator, clausePermissionFactory,
                new SimpleClauseContextFactory(), null);
    }
}
