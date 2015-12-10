package com.atlassian.jira.security.roles;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.util.profiling.UtilTimerStack;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @see ProjectRoleManager
 */
public class DefaultProjectRoleManager implements ProjectRoleManager
{
    private ProjectRoleAndActorStore projectRoleAndActorStore;

    public DefaultProjectRoleManager(ProjectRoleAndActorStore projectRoleAndActorStore)
    {
        this.projectRoleAndActorStore = projectRoleAndActorStore;
    }

    @Override
    public Collection<ProjectRole> getProjectRoles()
    {
        return projectRoleAndActorStore.getAllProjectRoles();
    }

    @Override
    public Collection<ProjectRole> getProjectRoles(final ApplicationUser user, final Project project)
    {
        Collection<ProjectRole> associatedProjectRoles = new TreeSet<ProjectRole>(ProjectRoleComparator.COMPARATOR);
        Collection<ProjectRole> allProjectRoles = getProjectRoles();
        for (final ProjectRole projectRole : allProjectRoles)
        {
            final ProjectRoleActors projectRoleActors = getProjectRoleActors(projectRole, project);
            if (projectRoleActors.contains(user))
            {
                associatedProjectRoles.add(projectRole);
            }
        }
        return associatedProjectRoles;
    }

    @Override
    public Collection<ProjectRole> getProjectRoles(User user, Project project)
    {
        return getProjectRoles(ApplicationUsers.from(user), project);
    }

    @Override
    public ProjectRole getProjectRole(Long id)
    {
        return projectRoleAndActorStore.getProjectRole(id);
    }

    @Override
    public ProjectRole getProjectRole(String name)
    {
        if (StringUtils.isBlank(name))
        {
            throw new IllegalArgumentException("ProjectRole can not be found with a null name");
        }

        return projectRoleAndActorStore.getProjectRoleByName(name);
    }

    @Override
    public ProjectRole createRole(ProjectRole projectRole)
    {
        if (projectRole == null || projectRole.getName() == null)
        {
            throw new IllegalArgumentException("ProjectRole can not be created with a null name");
        }

        if (isRoleNameUnique(projectRole.getName()))
        {
            return projectRoleAndActorStore.addProjectRole(projectRole);
        }
        else
        {
            throw new IllegalArgumentException("A project role with the provided name: " + projectRole.getName() + ", already exists in the system.");
        }
    }

    @Override
    public boolean isRoleNameUnique(String name)
    {
        return projectRoleAndActorStore.getProjectRoleByName(name) == null;
    }

    @Override
    public void deleteRole(ProjectRole projectRole)
    {
        if (projectRole == null)
        {
            throw new IllegalArgumentException("ProjectRole can not be null");
        }
        projectRoleAndActorStore.deleteProjectRole(projectRole);
    }

    @Override
    public void updateRole(ProjectRole projectRole)
    {
        if (projectRole == null)
        {
            throw new IllegalArgumentException("ProjectRole can not be null");
        }
        if (projectRole.getName() == null)
        {
            throw new IllegalArgumentException("ProjectRole name can not be null");
        }
        projectRoleAndActorStore.updateProjectRole(projectRole);
    }

    @Override
    public ProjectRoleActors getProjectRoleActors(ProjectRole projectRole, Project project)
    {
        if (projectRole == null)
        {
            throw new IllegalArgumentException("ProjectRole can not be null");
        }
        if (project == null)
        {
            throw new IllegalArgumentException("Project can not be null");
        }
        return projectRoleAndActorStore.getProjectRoleActors(projectRole.getId(), project.getId());
    }

    @Override
    public void updateProjectRoleActors(ProjectRoleActors projectRoleActors)
    {
        if (projectRoleActors == null)
        {
            throw new IllegalArgumentException("ProjectRoleActors can not be null");
        }
        if (projectRoleActors.getProjectId() == null)
        {
            throw new IllegalArgumentException("ProjectRoleActors project can not be null");
        }
        if (projectRoleActors.getProjectRoleId() == null)
        {
            throw new IllegalArgumentException("ProjectRoleActors projectRole can not be null");
        }
        if (projectRoleActors.getRoleActors() == null)
        {
            throw new IllegalArgumentException("ProjectRoleActors roleActors set can not be null");
        }
        projectRoleAndActorStore.updateProjectRoleActors(projectRoleActors);
    }

    @Override
    public DefaultRoleActors getDefaultRoleActors(ProjectRole projectRole)
    {
        if (projectRole == null)
        {
            throw new IllegalArgumentException("ProjectRole can not be null");
        }
        return projectRoleAndActorStore.getDefaultRoleActors(projectRole.getId());
    }

    @Override
    public void updateDefaultRoleActors(DefaultRoleActors defaultRoleActors)
    {
        if (defaultRoleActors == null)
        {
            throw new IllegalArgumentException("DefaultRoleActors can not be null");
        }
        if (defaultRoleActors.getProjectRoleId() == null)
        {
            throw new IllegalArgumentException("DefaultRoleActors projectRole can not be null");
        }
        if (defaultRoleActors.getRoleActors() == null)
        {
            throw new IllegalArgumentException("DefaultRoleActors roleActors set can not be null");
        }
        projectRoleAndActorStore.updateDefaultRoleActors(defaultRoleActors);
    }

    @Override
    public void applyDefaultsRolesToProject(Project project)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project can not be null");
        }
        projectRoleAndActorStore.applyDefaultsRolesToProject(project);
    }

    @Override
    public void removeAllRoleActorsByNameAndType(String key, String type)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("The role actor name can not be null");
        }
        if (type == null)
        {
            throw new IllegalArgumentException("The role type can not be null");
        }
        projectRoleAndActorStore.removeAllRoleActorsByKeyAndType(key, type);
    }

    @Override
    public void removeAllRoleActorsByProject(Project project)
    {
        if (project == null || project.getId() == null)
        {
            throw new IllegalArgumentException("The project id can not be null");
        }
        projectRoleAndActorStore.removeAllRoleActorsByProject(project);
    }

    @Override
    public boolean isUserInProjectRole(ApplicationUser user, ProjectRole projectRole, Project project)
    {
        try
        {
            UtilTimerStack.push("DefaultProjectRoleManager.isUserInProjectRole");

            if (project == null || project.getId() == null)
            {
                throw new IllegalArgumentException("The project id can not be null");
            }
            if (projectRole == null)
            {
                throw new IllegalArgumentException("ProjectRole can not be null");
            }
            return getProjectRoleActors(projectRole, project).contains(user);
        }
        finally
        {
            UtilTimerStack.pop("DefaultProjectRoleManager.isUserInProjectRole");
        }
    }

    @Override
    public boolean isUserInProjectRole(User user, ProjectRole projectRole, Project project)
    {
        return isUserInProjectRole(ApplicationUsers.from(user), projectRole, project);
    }

    @Override
    public Collection<Long> getProjectIdsContainingRoleActorByNameAndType(String key, String type)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("The role actor name can not be null");
        }
        if (type == null)
        {
            throw new IllegalArgumentException("The role type can not be null");
        }
        return projectRoleAndActorStore.getProjectIdsContainingRoleActorByKeyAndType(key, type);
    }

    @Override
    public List<Long> roleActorOfTypeExistsForProjects(List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String projectRoleParameter)
    {
        return projectRoleAndActorStore.roleActorOfTypeExistsForProjects(projectsToLimitBy, projectRole, projectRoleType, projectRoleParameter);
    }

    @Override
    public Map<Long, List<String>> getProjectIdsForUserInGroupsBecauseOfRole(List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String userName)
    {
        return projectRoleAndActorStore.getProjectIdsForUserInGroupsBecauseOfRole(projectsToLimitBy, projectRole, projectRoleType, userName);
    }

    @Override
    public ProjectIdToProjectRoleIdsMap createProjectIdToProjectRolesMap(User user, Collection<Long> projectIds)
    {
        return createProjectIdToProjectRolesMap(ApplicationUsers.from(user),projectIds);
    }

    @Override
    public ProjectIdToProjectRoleIdsMap createProjectIdToProjectRolesMap(final ApplicationUser user, final Collection<Long> projectIds)
    {
        ProjectIdToProjectRoleIdsMap map = new ProjectIdToProjectRoleIdsMap();
        if (projectIds != null && !projectIds.isEmpty())
        {
            ProjectManager projectManager = ComponentAccessor.getProjectManager();
            for (final Long projectId : projectIds)
            {
                Collection<ProjectRole> projectRoles = getProjectRoles(user, projectManager.getProjectObj(projectId));
                for (final ProjectRole projectRole : projectRoles)
                {
                    map.add(projectId, projectRole.getId());
                }
            }
        }
        return map;
    }
}
