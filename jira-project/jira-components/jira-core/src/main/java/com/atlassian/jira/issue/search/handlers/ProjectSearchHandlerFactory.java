package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.ProjectSearcher;
import com.atlassian.jira.jql.context.MultiClauseDecoratorContextFactory;
import com.atlassian.jira.jql.context.ProjectClauseContextFactory;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.permission.ProjectClauseValueSanitiser;
import com.atlassian.jira.jql.query.ProjectClauseQueryFactory;
import com.atlassian.jira.jql.resolver.ProjectResolver;
import com.atlassian.jira.jql.validator.ProjectValidator;
import com.atlassian.jira.jql.values.ProjectClauseValuesGenerator;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the {@link com.atlassian.jira.issue.fields.ProjectSystemField}.
 *
 * @since v4.0
 */
@InjectableComponent
public final class ProjectSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public ProjectSearchHandlerFactory(final ComponentFactory componentFactory, final ProjectClauseQueryFactory clauseFactory,
            final ProjectValidator caluseValidator, final FieldClausePermissionChecker.Factory clausePermissionFactory,
            final PermissionManager permissionManager, final JqlOperandResolver jqlOperandResolver,
            final ProjectResolver projectResolver, final MultiClauseDecoratorContextFactory.Factory multiFactory)
    {
        super(componentFactory, SystemSearchConstants.forProject(), ProjectSearcher.class,
                clauseFactory, caluseValidator, clausePermissionFactory,
                multiFactory.create(new ProjectClauseContextFactory(jqlOperandResolver, projectResolver, permissionManager)),
                new ProjectClauseValuesGenerator(permissionManager),
                new ProjectClauseValueSanitiser(permissionManager, jqlOperandResolver, projectResolver));
    }
}
