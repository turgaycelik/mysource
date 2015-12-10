package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

import java.util.Map;

public class ProjectQuickSearchHandler extends SingleWordQuickSearchHandler
{
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;

    public ProjectQuickSearchHandler(ProjectManager projectManager, PermissionManager permissionManager, JiraAuthenticationContext authenticationContext)
    {
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
    }

    protected Map handleWord(String word, QuickSearchResult searchResult)
    {
        if (word == null)
            return null;

        Project projectByKey = projectManager.getProjectObjByKey(word.toUpperCase());
        if (projectByKey != null && hasPermissionToViewProject(projectByKey))
        {
            // TODO: Do we have to put a String in the Map?
            return EasyMap.build("pid", projectByKey.getGenericValue().getString("id"));
        }

        Project projectByName = projectManager.getProjectObjByName(word);
        if (projectByName != null && hasPermissionToViewProject(projectByName))
        {
            // TODO: Do we have to put a String in the Map?
            return EasyMap.build("pid", projectByName.getGenericValue().getString("id"));
        }

        return null;
    }

    private boolean hasPermissionToViewProject(Project project)
    {
        return permissionManager.hasPermission(Permissions.BROWSE, project, authenticationContext.getUser());
    }

}
