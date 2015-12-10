package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.ResolutionSearcher;
import com.atlassian.jira.jql.context.SimpleClauseContextFactory;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.ResolutionClauseQueryFactory;
import com.atlassian.jira.jql.validator.ResolutionValidator;
import com.atlassian.jira.jql.values.ResolutionClauseValuesGenerator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the {@link com.atlassian.jira.issue.fields.ResolutionDateSystemField}.
 *
 * @since v4.0
 */
@InjectableComponent
public final class ResolutionSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public ResolutionSearchHandlerFactory(final ComponentFactory componentFactory, final ResolutionClauseQueryFactory factory,
            final ResolutionValidator validator, final FieldClausePermissionChecker.Factory clausePermissionFactory,
            final ConstantsManager constantsManager)
    {
        super(componentFactory, SystemSearchConstants.forResolution(), ResolutionSearcher.class,
                factory, validator, clausePermissionFactory,
                new SimpleClauseContextFactory(),
                new ResolutionClauseValuesGenerator(constantsManager));
    }
}
