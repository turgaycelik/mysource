package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.StatusSearcher;
import com.atlassian.jira.jql.context.MultiClauseDecoratorContextFactory;
import com.atlassian.jira.jql.context.StatusClauseContextFactory;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.StatusClauseQueryFactory;
import com.atlassian.jira.jql.validator.StatusValidator;
import com.atlassian.jira.jql.values.StatusClauseValuesGenerator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the {@link com.atlassian.jira.issue.search.handlers.StatusSearchHandlerFactory}.
 *
 * @since v4.0
 */
@InjectableComponent
public final class StatusSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public StatusSearchHandlerFactory(final ComponentFactory componentFactory, StatusClauseQueryFactory queryFactory,
            StatusValidator queryValidator, final FieldClausePermissionChecker.Factory clausePermissionFactory,
            StatusClauseContextFactory contextFactory, final ConstantsManager constantsManager,
            MultiClauseDecoratorContextFactory.Factory multiFactory)
    {
        super(componentFactory, SystemSearchConstants.forStatus(), StatusSearcher.class,
                queryFactory, queryValidator, clausePermissionFactory,
                multiFactory.create(contextFactory), new StatusClauseValuesGenerator(constantsManager));
    }
}
