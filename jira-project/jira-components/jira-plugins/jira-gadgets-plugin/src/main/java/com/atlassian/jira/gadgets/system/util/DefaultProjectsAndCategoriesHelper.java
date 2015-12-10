package com.atlassian.jira.gadgets.system.util;

import com.atlassian.jira.gadgets.system.ProjectsAndProjectCategoriesResource;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility methods for handling a parameter which is a string of project and category ids
 */
public class DefaultProjectsAndCategoriesHelper implements ProjectsAndCategoriesHelper
{
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;

    public DefaultProjectsAndCategoriesHelper(ProjectManager projectManager, PermissionManager permissionManager, JiraAuthenticationContext authenticationContext)
    {
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
    }

    public void validate(String projectsOrCategories, Collection<ValidationError> errors, String fieldName)
    {
        List<String> projectAndCategoryIds = split(projectsOrCategories);
        if (projectAndCategoryIds.contains(ProjectsAndProjectCategoriesResource.ALL_PROJECTS))
        {
            return;
        }
        Set<Long> selectedProjectIds = ProjectsAndProjectCategoriesResource.filterProjectIds(projectAndCategoryIds);
        for (Long projectId : selectedProjectIds)
        {
            if (projectManager.getProjectObj(projectId) == null)
            {
                errors.add(new ValidationError(fieldName, "gadget.common.invalid.project"));
            }
        }
        Set<Long> selectedCategoryIds = ProjectsAndProjectCategoriesResource.filterProjectCategoryIds(projectAndCategoryIds);
        for (Long catId : selectedCategoryIds)
        {
            if (projectManager.getProjectCategory(catId) == null)
            {
                errors.add(new ValidationError(fieldName, "gadget.common.invalid.projectCategory"));
            }
        }
        if (selectedProjectIds.isEmpty() && selectedCategoryIds.isEmpty())
        {
            errors.add(new ValidationError(fieldName, "gadget.common.projects.and.categories.none.selected"));
        }
    }

    private List<String> split(final String projectsOrCategories)
    {
        if (projectsOrCategories == null)
        {
            return Collections.emptyList();
        }
        return Arrays.asList(projectsOrCategories.split("\\|"));
    }

    public Set<Long> getProjectIds(String projectAndCategoryIds)
    {
        return getProjectIds(split(projectAndCategoryIds));
    }

    private Set<Long> getProjectIds(List<String> projectAndCategoryIds)
    {
        Set<Long> projectIds = new HashSet<Long>();
        if (projectAndCategoryIds != null && !projectAndCategoryIds.isEmpty())
        {
            if (projectAndCategoryIds.contains(ProjectsAndProjectCategoriesResource.ALL_PROJECTS))
            {
                // add all projects this user can see
                projectIds.addAll(getAllBrowsableProjects());
            }
            else
            {
                // add projects
                projectIds.addAll(ProjectsAndProjectCategoriesResource.filterProjectIds(projectAndCategoryIds));

                // narrow down to project categories
                final Collection<String> possibleCategoryIds = new ArrayList<String>(projectAndCategoryIds);
                possibleCategoryIds.removeAll(projectIds);

                // get categories
                final Set<Long> categoryIds = ProjectsAndProjectCategoriesResource.filterProjectCategoryIds(possibleCategoryIds);
                for (Long categoryId : categoryIds)
                {
                    projectIds.addAll(getProjectIdsForCategory(categoryId));
                }

                projectIds = filterProjectsByPermission(projectIds);
            }
        }
        return projectIds;
    }

    private Set<Long> getAllBrowsableProjects()
    {
        return extractProjectIdsFromProjects(permissionManager.getProjects(Permissions.BROWSE, authenticationContext.getUser()));
    }

    private Set<Long> filterProjectsByPermission(Set<Long> projectIds)
    {
        final Set<Long> filtered = new HashSet<Long>();
        for (Long projectId : projectIds)
        {
            if (canBrowseProject(projectId))
            {
                filtered.add(projectId);
            }
        }
        return filtered;
    }

    private boolean canBrowseProject(Long projectId)
    {
        final Project project = projectManager.getProjectObj(projectId);
        return project != null && permissionManager.hasPermission(Permissions.BROWSE, project, authenticationContext.getUser());
    }

    /**
     * Collect and return ids of all projects that relate to this project category.
     *
     * @param categoryId category id
     * @return set of project ids, never null
     */
    private Set<Long> getProjectIdsForCategory(Long categoryId)
    {
        Collection<Project> projs = projectManager.getProjectObjectsFromProjectCategory(categoryId);
        return extractProjectIdsFromProjects(projs);
    }

    private Set<Long> extractProjectIdsFromProjects(Collection<Project> projs)
    {
        Set<Long> projectIds = new HashSet<Long>();
        for (Project proj : projs)
        {
            projectIds.add(proj.getId());
        }
        return projectIds;
    }
}
