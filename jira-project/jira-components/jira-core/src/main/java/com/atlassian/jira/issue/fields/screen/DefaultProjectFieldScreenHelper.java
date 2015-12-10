package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.issue.comparator.ProjectNameComparator;
import com.atlassian.jira.issue.fields.screen.issuetype.ProjectIssueTypeScreenSchemeHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.ProjectWorkflowSchemeHelper;
import com.atlassian.jira.workflow.WorkflowActionsBean;
import com.atlassian.jira.workflow.WorkflowManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opensymphony.workflow.loader.ActionDescriptor;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @since v4.4
 */
public class DefaultProjectFieldScreenHelper implements ProjectFieldScreenHelper
{

    private final ProjectWorkflowSchemeHelper projectWorkflowSchemeHelper;
    private final ProjectIssueTypeScreenSchemeHelper issueTypeScreenSchemeHelper;
    private final WorkflowManager workflowManager;
    private final FieldScreenSchemeManager fieldScreenSchemeManager;
    private final PermissionManager permissionManager;

    public DefaultProjectFieldScreenHelper(final ProjectWorkflowSchemeHelper projectWorkflowSchemeHelper,
            final ProjectIssueTypeScreenSchemeHelper issueTypeScreenSchemeHelper,
            final WorkflowManager workflowManager, final FieldScreenSchemeManager fieldScreenSchemeManager,
            final PermissionManager permissionManager)
    {

        this.projectWorkflowSchemeHelper = projectWorkflowSchemeHelper;
        this.issueTypeScreenSchemeHelper = issueTypeScreenSchemeHelper;
        this.workflowManager = workflowManager;
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
        this.permissionManager = permissionManager;
    }

    @Override
    public List<Project> getProjectsForFieldScreen(final FieldScreen fieldScreen)
    {
        final WorkflowActionsBean actionsBean = getActionsBean();
        final Set<Project> projects = Sets.newTreeSet(ProjectNameComparator.COMPARATOR);

        final Set<String> matchingWorkflows = Sets.newHashSet();

        final Collection<JiraWorkflow> activeWorkflows = workflowManager.getActiveWorkflows();
        for (final JiraWorkflow activeWorkflow : activeWorkflows)
        {
            final Collection<ActionDescriptor> allActions = activeWorkflow.getAllActions();
            for (final ActionDescriptor actionDescriptor : allActions)
            {
                final FieldScreen fieldScreenForView = actionsBean.getFieldScreenForView(actionDescriptor);
                if (fieldScreenForView != null && fieldScreenForView.equals(fieldScreen))
                {
                    matchingWorkflows.add(activeWorkflow.getName());
                    break;
                }
            }
        }

        final Set<FieldScreenScheme> fieldScreenSchemes = Sets.newHashSet(fieldScreenSchemeManager.getFieldScreenSchemes(fieldScreen));

        projects.addAll(projectWorkflowSchemeHelper.getProjectsForWorkflow(matchingWorkflows).values());
        projects.addAll(issueTypeScreenSchemeHelper.getProjectsForFieldScreenSchemes(fieldScreenSchemes).values());

        return Lists.newArrayList(projects);
    }

    @Override
    public boolean canUserViewFieldScreenForProject(final ApplicationUser user, final FieldScreen fieldScreen, final Project project)
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, user)
                || permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user)
                && isProjectUsingFieldScreen(fieldScreen, project);
    }

    private boolean isProjectUsingFieldScreen(final FieldScreen fieldScreen, final Project project)
    {
        final Set<FieldScreenScheme> fieldScreenSchemes =
                Sets.newHashSet(fieldScreenSchemeManager.getFieldScreenSchemes(fieldScreen));
        final Collection<Project> projects =
                issueTypeScreenSchemeHelper.getProjectsForFieldScreenSchemes(fieldScreenSchemes).values();

        return projects.contains(project);
    }

    @VisibleForTesting
    WorkflowActionsBean getActionsBean()
    {
        return new WorkflowActionsBean();
    }
}
