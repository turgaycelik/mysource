package com.atlassian.jira.web.action.admin.issuetypes;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.comparator.ProjectNameComparator;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class that returns all the projects that user an Issue Type Scheme (aka. FieldConfigScheme).
 *
 * @since v4.4
 */
public class ProjectIssueTypeSchemeHelper
{
    private final IssueTypeSchemeManager schemeManager;
    private final JiraAuthenticationContext authContext;
    private final ProjectFactory projectFactory;
    private final ProjectService projectService;
    private final PermissionManager permissionManager;

    public ProjectIssueTypeSchemeHelper(ProjectService projectService, IssueTypeSchemeManager schemeManager,
            JiraAuthenticationContext authContext, ProjectFactory projectFactory, PermissionManager permissionManager)
    {
        this.projectService = projectService;
        this.schemeManager = schemeManager;
        this.authContext = authContext;
        this.projectFactory = projectFactory;
        this.permissionManager = permissionManager;
    }

    List<Project> getProjectsUsingScheme(FieldConfigScheme configScheme)
    {
        final List<Project> associatedProjects;
        if (schemeManager.isDefaultIssueTypeScheme(configScheme))
        {
            //The default entries don't exist in the database so we have to loop across all the projects.
            final Long defaultId = configScheme.getId();

            associatedProjects = new ArrayList<Project>();
            for (Project project : getEditableProjects())
            {
                FieldConfigScheme projectScheme = schemeManager.getConfigScheme(project);
                if (projectScheme == null || defaultId.equals(projectScheme.getId()))
                {
                    associatedProjects.add(project);
                }
            }
        }
        else
        {
            User user = authContext.getLoggedInUser();
            List<Project> allAssociated = projectFactory.getProjects(configScheme.getAssociatedProjects());
            associatedProjects = new ArrayList<Project>(allAssociated.size());
            for (Project project : allAssociated)
            {
                if (hasEditPermission(user, project))
                {
                    associatedProjects.add(project);
                }
            }
        }

        Collections.sort(associatedProjects, ProjectNameComparator.COMPARATOR);
        return associatedProjects;
    }

    boolean hasEditPermission(User user, Project project)
    {
        return ProjectAction.EDIT_PROJECT_CONFIG.hasPermission(permissionManager, user, project);
    }

    private List<Project> getEditableProjects()
    {
        ServiceOutcome<List<Project>> allProjectsForAction = projectService.getAllProjectsForAction(authContext.getLoggedInUser(), ProjectAction.EDIT_PROJECT_CONFIG);
        if (allProjectsForAction.isValid())
        {
            return allProjectsForAction.getReturnedValue();
        }
        else
        {
            return Collections.emptyList();
        }
    }
}
