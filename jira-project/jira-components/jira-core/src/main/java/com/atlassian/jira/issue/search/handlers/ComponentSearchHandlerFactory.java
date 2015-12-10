package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.ComponentsSearcher;
import com.atlassian.jira.jql.context.ComponentClauseContextFactory;
import com.atlassian.jira.jql.context.MultiClauseDecoratorContextFactory;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.ComponentClauseQueryFactory;
import com.atlassian.jira.jql.validator.ComponentValidator;
import com.atlassian.jira.jql.values.ComponentClauseValuesGenerator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the {@link com.atlassian.jira.issue.fields.ComponentsSystemField}.
 *
 * @since v4.0
 */
@InjectableComponent
public final class ComponentSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public ComponentSearchHandlerFactory(final ComponentFactory componentFactory, ComponentClauseQueryFactory clauseFactory,
            ComponentValidator caluseValidator, final FieldClausePermissionChecker.Factory clausePermissionFactory,
            final ComponentClauseContextFactory componentClauseContextFactory, final PermissionManager permissionManager,
            final ProjectComponentManager projectComponentManager, final ProjectManager projectManager,
            final MultiClauseDecoratorContextFactory.Factory multiFactory)
    {
        super(componentFactory, SystemSearchConstants.forComponent(),
                ComponentsSearcher.class, clauseFactory, caluseValidator, clausePermissionFactory,
                multiFactory.create(componentClauseContextFactory),
                new ComponentClauseValuesGenerator(projectComponentManager, projectManager, permissionManager));
    }
}