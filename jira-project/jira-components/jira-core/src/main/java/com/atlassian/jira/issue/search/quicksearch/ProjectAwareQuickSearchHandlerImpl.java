package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Looks up the search request for projects already searched for in the context. If there are no projects
 * in the context than returns all projects the current user can browse.
 *
 * @since v3.13
 */
public class ProjectAwareQuickSearchHandlerImpl implements ProjectAwareQuickSearchHandler
{
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;

    public ProjectAwareQuickSearchHandlerImpl(final ProjectManager projectManager, final PermissionManager permissionManager, final JiraAuthenticationContext authenticationContext)
    {
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
    }

    /**
     * Retrieves project from the search request or returns all projects the current user can browse
     *
     * @param searchResult search result to look up
     * @return list of project {@link org.ofbiz.core.entity.GenericValue}'s
     */
    public List/*<GenericValue>*/getProjects(final QuickSearchResult searchResult)
    {
        final Collection projectIds = searchResult.getSearchParameters("pid");
        final List possibleProjects = new ArrayList();
        if ((projectIds != null) && (projectIds.size() == 1))
        {
            final String projectId = (String) projectIds.iterator().next();
            final GenericValue project = projectManager.getProject(new Long(projectId));
            possibleProjects.add(project);
        }
        else
        {
            possibleProjects.addAll(permissionManager.getProjects(Permissions.BROWSE, authenticationContext.getLoggedInUser()));
        }
        return possibleProjects;
    }

    @Override
    public String getSingleProjectIdFromSearch(QuickSearchResult searchResult)
    {
        Collection projectIds =  searchResult.getSearchParameters("pid");

        if ((projectIds != null) && (projectIds.size() == 1))
        {
            return (String) projectIds.iterator().next();
        }

        return null;
    }

    @Override
    public void addProject(String projectId, QuickSearchResult searchResult)
    {
        searchResult.addSearchParameter("pid", projectId);
    }
}
