package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.jql.resolver.ProjectIndexInfoResolver;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generates a {@link com.atlassian.jira.jql.context.ClauseContext} for a project clause based on the specified
 * project values and the projects that the user has permission to see.
 *
 * The projects in the context that are generated here are {@link com.atlassian.jira.jql.context.QueryContextElementType#EXPLICIT}
 * and it will always return the {@link com.atlassian.jira.jql.context.AllIssueTypesContext} for issue types.
 *
 * @since v4.0
 */
public class ProjectClauseContextFactory implements ClauseContextFactory
{
    private final PermissionManager permissionManager;
    private final ProjectIndexInfoResolver projectIndexInfoResolver;
    private final JqlOperandResolver jqlOperandResolver;

    public ProjectClauseContextFactory(JqlOperandResolver jqlOperandResolver, NameResolver<Project> projectResolver,
            PermissionManager permissionManager)
    {
        this.jqlOperandResolver = jqlOperandResolver;
        this.permissionManager = permissionManager;
        this.projectIndexInfoResolver = new ProjectIndexInfoResolver(projectResolver);
    }

    public ClauseContext getClauseContext(final User searcher, final TerminalClause terminalClause)
    {
        final Operator operator = terminalClause.getOperator();
        if (!handlesOperator(operator))
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }

        final List<QueryLiteral> values = jqlOperandResolver.getValues(searcher, terminalClause.getOperand(), terminalClause);

        // Find all the projects that are in context for the provided project values and the projects that the user can see
        final Set<Project> projectsInContext = getProjectsInContext(values, searcher, isNegationOperator(operator));
        if (projectsInContext.isEmpty())
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }

        final Set<ProjectIssueTypeContext> contexts = new HashSet<ProjectIssueTypeContext>();

        // Now that we have all the projects in context we need to get all the issue types for each project and
        // create a ProjectIssueTypeContext for that project/issue type pair.
        for (Project project : projectsInContext)
        {
            contexts.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(project.getId()), AllIssueTypesContext.INSTANCE));
        }

        return contexts.isEmpty() ? ClauseContextImpl.createGlobalClauseContext() : new ClauseContextImpl(contexts);
    }

    /**
     * @param values the query literals representing project Ids
     * @param searcher the user performing the search
     * @param negationOperator whether the clause contained a negation operator
     * @return a set of projects which make up the context of these values; never null.
     */
    private Set<Project> getProjectsInContext(final List<QueryLiteral> values, final User searcher, final boolean negationOperator)
    {
        final Set<String> projectIds = new HashSet<String>();
        if (values != null)
        {
            for (QueryLiteral value : values)
            {
                if (value.getStringValue() != null)
                {
                    projectIds.addAll(projectIndexInfoResolver.getIndexedValues(value.getStringValue()));
                }
                else if (value.getLongValue() != null)
                {
                    projectIds.addAll(projectIndexInfoResolver.getIndexedValues(value.getLongValue()));
                }
                else if (value.isEmpty())
                {
                    // empty literal does not impact on the context - we can move on
                }
                else
                {
                    throw new IllegalStateException("Invalid query literal");
                }
            }
        }

        if (projectIds.isEmpty())
        {
            return Collections.emptySet();
        }

        // Lets get all the visible projects for the user, we are going to need them to compare against the specified projects
        final Collection<Project> visibleProjects = permissionManager.getProjectObjects(Permissions.BROWSE, searcher);
        final Set<Project> projectsInContext = new HashSet<Project>();
        for (Project visibleProject : visibleProjects)
        {
            // either we specified the project and we're a positive query,
            // or we didn't specify the project and we're a negative query
            if (projectIds.contains(visibleProject.getId().toString()) ^ negationOperator)
            {
                projectsInContext.add(visibleProject);
            }
        }

        return projectsInContext;
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
