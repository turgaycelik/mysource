package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.ResolutionDateSearcher;
import com.atlassian.jira.jql.context.SimpleClauseContextFactory;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.ResolutionDateClauseQueryFactory;
import com.atlassian.jira.jql.validator.ResolutionDateValidator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the {@link com.atlassian.jira.issue.fields.ResolutionDateSystemField}.
 *
 * @since v4.0
 */
@InjectableComponent
public final class ResolutionDateSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public ResolutionDateSearchHandlerFactory(final ComponentFactory componentFactory, ResolutionDateClauseQueryFactory factory,
            ResolutionDateValidator validator, final FieldClausePermissionChecker.Factory clausePermissionFactory)
    {
        super(componentFactory, SystemSearchConstants.forResolutionDate(),
                ResolutionDateSearcher.class, factory, validator, clausePermissionFactory,
                new SimpleClauseContextFactory(), null);
    }
}
