package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.PrioritySearcher;
import com.atlassian.jira.jql.context.SimpleClauseContextFactory;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.PriorityClauseQueryFactory;
import com.atlassian.jira.jql.validator.PriorityValidator;
import com.atlassian.jira.jql.values.PriorityClauseValuesGenerator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the {@link com.atlassian.jira.issue.fields.PrioritySystemField}.
 *
 * @since v4.0
 */
@InjectableComponent
public final class PrioritySearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public PrioritySearchHandlerFactory(final ComponentFactory componentFactory, PriorityClauseQueryFactory factory,
            PriorityValidator validator, final FieldClausePermissionChecker.Factory clausePermissionFactory,
            final ConstantsManager constantsManager)
    {
        super(componentFactory, SystemSearchConstants.forPriority(),
                PrioritySearcher.class, factory, validator, clausePermissionFactory,
                new SimpleClauseContextFactory(),
                new PriorityClauseValuesGenerator(constantsManager));
    }
}
