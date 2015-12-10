package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.ProjectCategoryResolver;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Generates a {@link com.atlassian.jira.jql.context.ClauseContext} for a project category clause based on the specified
 * project values and the projects that the user has permission to see.
 *
 * The projects in the context that are generated here are {@link com.atlassian.jira.jql.context.QueryContextElementType#IMPLICIT}
 * and it will always return the {@link com.atlassian.jira.jql.context.AllIssueTypesContext} for issue types.
 *
 * @since v4.0
 */
@InjectableComponent
public class ProjectCategoryClauseContextFactory implements ClauseContextFactory
{
    private final PermissionManager permissionManager;
    private final ProjectCategoryResolver projectCategoryResolver;
    private final JqlOperandResolver jqlOperandResolver;

    public ProjectCategoryClauseContextFactory(final PermissionManager permissionManager, final ProjectCategoryResolver projectCategoryResolver,
            final JqlOperandResolver jqlOperandResolver)
    {
        this.permissionManager = notNull("permissionManager", permissionManager);
        this.projectCategoryResolver = notNull("projectCategoryResolver", projectCategoryResolver);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
    }

    public ClauseContext getClauseContext(final User searcher, final TerminalClause terminalClause)
    {
        final Operator operator = terminalClause.getOperator();

        if (!handlesOperator(operator))
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }

        final Set<Project> associatedProjects = getAssociatedProjectsFromClause(searcher, terminalClause);
        final Set<ProjectIssueTypeContext> contexts = getContextsForProjects(associatedProjects);
        return contexts.isEmpty() ? ClauseContextImpl.createGlobalClauseContext() : new ClauseContextImpl(contexts);
    }

    private Set<Project> getAssociatedProjectsFromClause(final User searcher, final TerminalClause clause)
    {
        final Set<Project> allVisibleProjects = new HashSet<Project>(permissionManager.getProjectObjects(Permissions.BROWSE, searcher));
        Set<Project> associatedProjects = new HashSet<Project>();

        final List<QueryLiteral> list = jqlOperandResolver.getValues(searcher, clause.getOperand(), clause);
        final Set<QueryLiteral> rawValues = list != null ? new LinkedHashSet<QueryLiteral>(list) : new LinkedHashSet<QueryLiteral>();
        // if we are negating, we need to exclude projects with no category
        // e.g. category NOT IN ("cat1") is equivalent to category NOT IN ("cat1", EMPTY)
        if (isNegationOperator(clause.getOperator()))
        {
            rawValues.add(new QueryLiteral());
        }

        for (QueryLiteral rawValue : rawValues)
        {
            final Collection<Project> projectsForCategory = projectCategoryResolver.getProjectsForCategory(rawValue);
            associatedProjects.addAll(CollectionUtils.intersection(allVisibleProjects, projectsForCategory));
        }

        if (!associatedProjects.isEmpty())
        {
            // if we have a negation operator we want every project context barr the associated ones.
            if (isNegationOperator(clause.getOperator()))
            {
                allVisibleProjects.removeAll(associatedProjects);
                associatedProjects = allVisibleProjects;
            }
        }

        return associatedProjects;
    }

    private Set<ProjectIssueTypeContext> getContextsForProjects(final Collection<Project> projects)
    {
        CollectionBuilder<ProjectIssueTypeContext> builder = CollectionBuilder.newBuilder();
        for (Project project : projects)
        {
            builder.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(project.getId()), AllIssueTypesContext.INSTANCE));
        }
        return builder.asMutableSet();
    }

    private boolean isNegationOperator(final Operator operator)
    {
        return OperatorClasses.NEGATIVE_EQUALITY_OPERATORS.contains(operator);
    }

    private boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY.contains(operator);
    }
}
