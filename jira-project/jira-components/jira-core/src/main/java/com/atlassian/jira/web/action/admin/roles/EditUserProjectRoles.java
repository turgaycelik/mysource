package com.atlassian.jira.web.action.admin.roles;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the bulk edit for project roles given a user.
 */
@WebSudoRequired
public class EditUserProjectRoles extends ViewUserProjectRoles
{

    public EditUserProjectRoles(ProjectManager projectManager, ProjectRoleService projectRoleService, ProjectFactory projectFactory,
                final CrowdService crowdService)
    {
        super(projectManager, projectRoleService, projectFactory, crowdService);
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        Map parameters = ActionContext.getParameters();
        Set<String> projectIds = getShownProjectIds();
        Collection<Project> projectsToUpdate = getProjectsFromIds(projectIds);

        // Iterate through the projects
        for (Project project : projectsToUpdate)
        {
            // Iterate through all project roles
            for (ProjectRole projectRole : getAllProjectRoles())
            {
                updateRoleActorsForProjectRole(project, projectRole, parameters);
            }
        }
        //JRA-12528: We need to return errors to the user.
        if(hasAnyErrors())
        {
            return ERROR;
        }

        return forceRedirect("ViewUserProjectRoles.jspa?name=" + JiraUrlCodec.encode(name));
    }

    public String doRefresh()
    {
        Set<String> projectIds = getShownProjectIds();

        String[] projectsToAdd = (String[]) ActionContext.getParameters().get("projects_to_add");
        if (projectsToAdd != null)
        {
            String projectIdsToAddValue = projectsToAdd[0];
            String[] projectIdsToAdd = projectIdsToAddValue.split(",");
            List addIds = Arrays.asList(projectIdsToAdd);
            if (addIds != null)
            {
                projectIds.addAll(addIds);
            }
        }
        // From this method we setup the visbile projects as an aggregation of what was visible on the previous page
        // and what the user is asking to have visible now. We do not need to go to the database for this information
        currentVisibleProjects = getProjectsFromIds(projectIds);

        return SUCCESS;
    }

    public boolean isAllProjectsInCategoryVisible(GenericValue projectCategory)
    {
        Collection<Object> projects = new ArrayList<Object>(getAllProjectsForCategory(projectCategory));
        Collection<Project> visibleProjects = getCurrentVisibleProjects();

        // Get the intersection of the two lists and if empty then true, else false
        projects.removeAll(visibleProjects);
        return projects.isEmpty();
    }

    public boolean isAllProjectsWithoutCategoryVisible()
    {
        final Collection<Project> projects = new ArrayList<Project>(getAllProjectsWithoutCategory());
        final Collection<Project> visibleProjects = getCurrentVisibleProjects();

        // Get the intersection of the two lists and if empty then true, else false
        projects.removeAll(visibleProjects);
        return projects.isEmpty();
    }

    public boolean isAllProjectsVisible()
    {
        if (!isAllProjectsWithoutCategoryVisible())
        {
            return false;
        }
        for (GenericValue category : getAllProjectCategories())
        {
            if (!isAllProjectsInCategoryVisible(category))
            {
                return false;
            }
        }
        return true;
    }

    public Collection<Project> getAllProjectsWithoutCategory()
    {
        return projectFactory.getProjects(projectManager.getProjectsWithNoCategory());
    }

    private Set<String> getShownProjectIds()
    {
        Set<String> projectIds = new HashSet<String>();

        // Get all the projects that were viewable
        String[] shownProjectIds = (String[]) ActionContext.getParameters().get("project_shown");

        List<String> shownIds = (shownProjectIds == null) ? new ArrayList<String>() : Arrays.asList(shownProjectIds);
        if (shownIds != null)
        {
            projectIds.addAll(shownIds);
        }
        return projectIds;
    }

    private Collection<Project> getProjectsFromIds(Set<String> shownProjects)
    {
        List<Long> projectIds = new ArrayList<Long>(shownProjects.size());
        for (final String projectIdString : shownProjects)
        {
            projectIds.add(new Long(projectIdString));
        }

        return projectManager.convertToProjectObjects(projectIds);
    }

    private void updateRoleActorsForProjectRole(Project project, ProjectRole projectRole, Map parameters)
    {
        // Look for the value of the checkbox and the original value
        String key = project.getId() + "_" + projectRole.getId();
        String[] origParam = (String[]) parameters.get(key + "_orig");
        boolean origValue = Boolean.valueOf(origParam[0]);
        String[] newValue = (String[]) parameters.get(key);

        // This means we are removing the user from the tang
        if (newValue == null && origValue)
        {
            projectRoleService.removeActorsFromProjectRole(EasyList.build(getUserKey()), projectRole, project, UserRoleActorFactory.TYPE, this);
        }
        // This means we are adding the user to the tang
        else if (newValue != null && !origValue)
        {
            projectRoleService.addActorsToProjectRole(EasyList.build(getUserKey()), projectRole, project, UserRoleActorFactory.TYPE, this);
        }
    }

    private String getUserKey()
    {
        ApplicationUser userObject = getUserManager().getUserByName(name);
        if (userObject == null)
        {
            return null;
        }
        return userObject.getKey();
    }

}
