package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.IssueConstantInfoResolver;
import com.atlassian.jira.jql.resolver.StatusResolver;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A context factory for project status clauses.
 *
 * @since v4.0
 */
public class StatusClauseContextFactory implements ClauseContextFactory
{
    private static final Logger log = Logger.getLogger(StatusClauseContextFactory.class);

    private final JqlOperandResolver jqlOperandResolver;
    private final WorkflowManager workflowManager;
    private final PermissionManager permissionManager;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final IssueConstantInfoResolver<?> issueConstantInfoResolver;

    public StatusClauseContextFactory(final JqlOperandResolver jqlOperandResolver, final StatusResolver statusResolver,
            final WorkflowManager workflowManager, final PermissionManager permissionManager,
            final IssueTypeSchemeManager issueTypeSchemeManager, final WorkflowSchemeManager workflowSchemeManager)
    {
        this.workflowManager = workflowManager;
        this.permissionManager = permissionManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.workflowSchemeManager = workflowSchemeManager;
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
        this.issueConstantInfoResolver = new IssueConstantInfoResolver<Status>(statusResolver);
    }

    public ClauseContext getClauseContext(final User searcher, final TerminalClause terminalClause)
    {
        final Operator operator = terminalClause.getOperator();
        boolean isEquality = isEqualityOperator(operator);

        if (!handlesOperator(operator))
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }

        final Set<ProjectIssueTypeContext> projectIssueTypeContexts = getContextFromStatusValues(searcher, terminalClause, isEquality);

        return projectIssueTypeContexts.isEmpty() ? ClauseContextImpl.createGlobalClauseContext() : new ClauseContextImpl(projectIssueTypeContexts);
    }

    Set<ProjectIssueTypeContext> getContextFromStatusValues(final User searcher, final TerminalClause clause, final boolean equality)
    {
        // if we don't have any ids, then the result will always be global: either by an empty set or the everything set
        Set<String> ids = getIds(searcher, clause);
        if (ids.isEmpty())
        {
            return Collections.emptySet();
        }

        boolean allProjects = true;
        boolean allIssueTypesAcrossAllProjects = true;

        final Map<String, Boolean> resultCache = new HashMap<String, Boolean>();
        final Set<ProjectIssueTypeContext> allContexts = new HashSet<ProjectIssueTypeContext>();
        final Collection<Project> projects = permissionManager.getProjectObjects(Permissions.BROWSE, searcher);

        for (final Project project : projects)
        {
            boolean projectAdded = false; // is this project added to the contxt
            boolean allIssueTypesAcrossProject = true; // are ALL issue types of this project added to the context
            Set<ProjectIssueTypeContext> projectIssueTypeContexts = new HashSet<ProjectIssueTypeContext>();

            //IssueType -> Workflowname
            final Map<String, String> workflowMap = workflowSchemeManager.getWorkflowMap(project);
            for (Map.Entry<String, String> entry : workflowMap.entrySet())
            {
                final boolean typeAdded;
                final String issueType = entry.getKey();
                final String workflowName = entry.getValue();

                final Boolean cachedResult = resultCache.get(workflowName);
                if (cachedResult != null)
                {
                    typeAdded = cachedResult;
                }
                else
                {
                    typeAdded = checkWorkflow(equality, ids, project, issueType, workflowName);
                    resultCache.put(workflowName, typeAdded);
                }

                if (typeAdded)
                {
                    projectIssueTypeContexts.addAll(getContextsForIssueType(project, workflowMap, issueType));
                    projectAdded = true;
                }
                else
                {
                    allIssueTypesAcrossAllProjects = false;
                    allIssueTypesAcrossProject = false;
                }
            }

            if (allIssueTypesAcrossProject) // if all issue types were added, its an ALL issue type context type
            {
                projectIssueTypeContexts = Collections.<ProjectIssueTypeContext>singleton(new ProjectIssueTypeContextImpl(
                        new ProjectContextImpl(project.getId()),
                        AllIssueTypesContext.INSTANCE));
            }
            if (!projectAdded)
            {
                allProjects = false;
            }
            allContexts.addAll(projectIssueTypeContexts);
        }

        if (allProjects && allIssueTypesAcrossAllProjects) // if all ALL projects and ALL issue types were added to the context, it is an ALL ALL context type
        {
            return Collections.singleton(ProjectIssueTypeContextImpl.createGlobalContext());
        }
        else
        {
            return allContexts;
        }
    }

    private Set<ProjectIssueTypeContext> getContextsForIssueType(final Project project, final Map<String, String> schemeMap, final String issueType)
    {
        //Null issue type means "default" issues types.
        if (issueType == null)
        {
            Set<ProjectIssueTypeContext> contexts = new HashSet<ProjectIssueTypeContext>();

            final Collection<IssueType> projectTypes = issueTypeSchemeManager.getIssueTypesForProject(project);
            for (IssueType projectType : projectTypes)
            {
                if (projectType != null && !schemeMap.containsKey(projectType.getId()))
                {
                    contexts.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(project.getId()),
                        new IssueTypeContextImpl(projectType.getId())));
                }
            }
            return contexts;
        }
        else
        {
            return Collections.<ProjectIssueTypeContext>singleton(new ProjectIssueTypeContextImpl(new ProjectContextImpl(project.getId()),
                new IssueTypeContextImpl(issueType)));
        }
    }

    private boolean checkWorkflow(final boolean equality, final Set<String> ids, final Project project, final String issueType,
            final String workflowName)
    {
        final JiraWorkflow workflow = workflowManager.getWorkflow(workflowName);
        if (workflow != null)
        {
            final List<Status> statusObjects = workflow.getLinkedStatusObjects();
            for (Status statusObject : statusObjects)
            {
                // JRA-19026 - when we encounter really messed up data the workflow can return null statuses
                if (statusObject != null)
                {
                    if (equality == ids.contains(statusObject.getId()))
                    {
                        return true;
                    }
                }
                else
                {
                    final String issueTypeStr = issueType == null ? "default" : issueType;
                    // Kinda strange, probably means you manually messed with your workflow, lets info log
                    log.debug("Workflow: '" + workflow.getName() + "' associated with project: '" + project.getId() + "' and issue type: '" + issueTypeStr + "' contains a null status, you might want to fix that.");
                }
            }
        }
        return false;
    }

    /**
     * @param searcher the user performing the search
     * @param clause the clause
     * @return a set of Strings representing the ids of the statuses; never null.
     */
    Set<String> getIds(final User searcher, final TerminalClause clause)
    {
        final List<QueryLiteral> literals = jqlOperandResolver.getValues(searcher, clause.getOperand(), clause);
        Set<String> ids = new HashSet<String>();
        if (literals != null)
        {
            for (QueryLiteral literal : literals)
            {
                if (literal.getLongValue() != null)
                {
                    ids.addAll(issueConstantInfoResolver.getIndexedValues(literal.getLongValue()));
                }
                else if (literal.getStringValue() != null)
                {
                    ids.addAll(issueConstantInfoResolver.getIndexedValues(literal.getStringValue()));
                }
                else if (literal.isEmpty())
                {
                    // empty literals would generate a Global context, but this does not impact on other contexts
                }
            }
        }
        return ids;

    }

    private boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY.contains(operator);
    }

    private boolean isEqualityOperator(final Operator operator)
    {
        return operator == Operator.EQUALS || operator == Operator.IS || operator == Operator.IN;
    }
}
