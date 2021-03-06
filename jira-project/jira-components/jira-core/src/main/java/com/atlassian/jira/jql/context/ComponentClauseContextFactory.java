package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.ComponentIndexInfoResolver;
import com.atlassian.jira.jql.resolver.ComponentResolver;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A context factory for project component clauses. The contexts that are generated by this factory will all be
 * {@link com.atlassian.jira.jql.context.QueryContextElementType#IMPLICIT}. The context will contain any projects
 * that the component is relevant for and an {@link com.atlassian.jira.jql.context.AllIssueTypesContext}.
 *
 * @since v4.0
 */
public class ComponentClauseContextFactory extends AbstractProjectAttributeClauseContextFactory<ProjectComponent>
{
    private final JqlOperandResolver jqlOperandResolver;
    private final ComponentResolver componentResolver;
    private final ProjectManager projectManager;

    public ComponentClauseContextFactory(final JqlOperandResolver jqlOperandResolver, final ComponentResolver componentResolver,
            final ProjectManager projectManager, final PermissionManager permissionManager)
    {
        super(new ComponentIndexInfoResolver(componentResolver), jqlOperandResolver, permissionManager);
        this.projectManager = notNull("projectManager", projectManager);
        this.componentResolver = notNull("componentResolver", componentResolver);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
    }

    ClauseContext getContextFromClause(final User searcher, final TerminalClause terminalClause)
    {
        final Operator operator = terminalClause.getOperator();
        if (!handlesOperator(operator))
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }

        final List<QueryLiteral> literals = jqlOperandResolver.getValues(searcher, terminalClause.getOperand(), terminalClause);
        // Run through and figure out all the components that were specified by the user
        List<ProjectComponent> specifiedComponents = new ArrayList<ProjectComponent>();

        if (literals != null)
        {
            for (QueryLiteral literal : literals)
            {
                if (!literal.isEmpty())
                {
                    final List<Long> ids = getIds(literal);
                    for (Long id : ids)
                    {
                        specifiedComponents.add(componentResolver.get(id));
                    }
                }
            }
        }

        // Note: empty literals do not impact on the context as they would generate All-All contexts
        // which are superceded by ProjectContexts generated from components
        if (!specifiedComponents.isEmpty())
        {
            Collection<ProjectComponent> contextComponents = new ArrayList<ProjectComponent>();

            // If the operator is a negation operator than we need to get the set of components that are NOT the
            // specified components.
            if (isNegationOperator(operator))
            {
                final List<ProjectComponent> allComponents = new ArrayList<ProjectComponent>(componentResolver.getAll());
                allComponents.removeAll(specifiedComponents);
                contextComponents.addAll(allComponents);
            }
            else
            {
                contextComponents = specifiedComponents;
            }

            final Set<ProjectIssueTypeContext> contexts = new HashSet<ProjectIssueTypeContext>();
            // Lets add the contexts for all the components we have found
            for (ProjectComponent contextComponent : contextComponents)
            {
                contexts.addAll(getContextsForProject(searcher, getProject(contextComponent.getProjectId())));
            }

            return new ClauseContextImpl(contexts);
        }
        else
        {
            // if we somehow got no components or only empties, just return the global context
            return ClauseContextImpl.createGlobalClauseContext();
        }
    }

    private Project getProject(Long projectId)
    {
        return projectManager.getProjectObj(projectId);
    }

    private boolean handlesOperator(Operator operator)
    {
        return OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY.contains(operator);
    }
}
