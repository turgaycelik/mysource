package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.DefaultRoleActors;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.util.ErrorCollection;
import org.apache.commons.collections.MultiMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mock implmentation for testing that contains only do-nothing-return-null methods.
 */
public class MockProjectRoleService implements ProjectRoleService
{

    @Override
    public Collection<ProjectRole> getProjectRoles(ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public Collection getProjectRoles(User currentUser, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public ProjectRole getProjectRole(Long id, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public ProjectRole getProjectRole(User currentUser, Long id, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public ProjectRole getProjectRoleByName(String name, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public ProjectRole getProjectRoleByName(User currentUser, String name, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public ProjectRole createProjectRole(ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public ProjectRole createProjectRole(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public boolean isProjectRoleNameUnique(String name, ErrorCollection errorCollection)
    {
        return false;
    }

    @Override
    public boolean isProjectRoleNameUnique(User currentUser, String name, ErrorCollection errorCollection)
    {
        return false;
    }

    @Override
    public void deleteProjectRole(ProjectRole projectRole, ErrorCollection errorCollection)
    {
    }

    @Override
    public void deleteProjectRole(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
    }

    @Override
    public void addActorsToProjectRole(Collection<String> actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection)
    {
    }

    @Override
    public void removeActorsFromProjectRole(Collection<String> actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection)
    {
    }

    @Override
    public void setActorsForProjectRole(Map<String, Set<String>> newRoleActors, ProjectRole projectRole, Project project, ErrorCollection errorCollection)
    {
    }

    @Override
    public void setActorsForProjectRole(User currentUser, Map<String, Set<String>> newRoleActors, ProjectRole projectRole, Project project, ErrorCollection errorCollection)
    {
    }

    @Override
    public void updateProjectRole(ProjectRole projectRole, ErrorCollection errorCollection)
    {
    }

    @Override
    public void addActorsToProjectRole(User currentUser, Collection actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection)
    {
    }

    @Override
    public void removeActorsFromProjectRole(User currentUser, Collection actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection)
    {
    }

    @Override
    public void updateProjectRole(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
    }

    @Override
    public ProjectRoleActors getProjectRoleActors(ProjectRole projectRole, Project project, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public ProjectRoleActors getProjectRoleActors(User currentUser, ProjectRole projectRole, Project project, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public DefaultRoleActors getDefaultRoleActors(ProjectRole projectRole, ErrorCollection collection)
    {
        return null;
    }

    @Override
    public DefaultRoleActors getDefaultRoleActors(User currentUser, ProjectRole projectRole, ErrorCollection collection)
    {
        return null;
    }

    @Override
    public void addDefaultActorsToProjectRole(Collection<String> actors, ProjectRole projectRole, String type, ErrorCollection errorCollection)
    {
    }

    @Override
    public void removeDefaultActorsFromProjectRole(Collection<String> actors, ProjectRole projectRole, String actorType, ErrorCollection errorCollection)
    {
    }

    @Override
    public void removeAllRoleActorsByNameAndType(String name, String type, ErrorCollection errorCollection)
    {
    }

    @Override
    public void addDefaultActorsToProjectRole(User currentUser, Collection actorNames, ProjectRole projectRole, String type, ErrorCollection errorCollection)
    {
    }

    @Override
    public void removeDefaultActorsFromProjectRole(User currentUser, Collection actors, ProjectRole projectRole, String actorType, ErrorCollection errorCollection)
    {
    }

    @Override
    public void removeAllRoleActorsByNameAndType(User currentUser, String name, String type, ErrorCollection errorCollection)
    {
    }

    @Override
    public ErrorCollection validateRemoveAllRoleActorsByNameAndType(String name, String type)
    {
        return null;
    }

    @Override
    public ErrorCollection validateRemoveAllRoleActorsByNameAndType(final User currentUser, final String name, final String type)
    {
        return null;
    }

    @Override
    public void removeAllRoleActorsByNameAndType(final String name, final String type)
    {
    }

    @Override
    public void removeAllRoleActorsByProject(Project project, ErrorCollection errorCollection)
    {
    }

    @Override
    public void removeAllRoleActorsByProject(User currentUser, Project project, ErrorCollection errorCollection)
    {
    }

    @Override
    public Collection getAssociatedNotificationSchemes(ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public Collection getAssociatedNotificationSchemes(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public Collection getAssociatedPermissionSchemes(ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public Collection getAssociatedPermissionSchemes(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public Collection getAssociatedIssueSecuritySchemes(ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public MultiMap getAssociatedWorkflows(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public Collection<Project> getProjectsContainingRoleActorByNameAndType(String name, String type, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public Collection getProjectsContainingRoleActorByNameAndType(User currentUser, String name, String type, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public List<Long> roleActorOfTypeExistsForProjects(List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String projectRoleParameter, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public Map<Long, List<String>> getProjectIdsForUserInGroupsBecauseOfRole(List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String userName, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public boolean hasProjectRolePermission(Project project)
    {
        return false;
    }

    @Override
    public List roleActorOfTypeExistsForProjects(User currentUser, List projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String projectRoleParameter, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public Map getProjectIdsForUserInGroupsBecauseOfRole(User currentUser, List projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String userName, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public boolean hasProjectRolePermission(final User currentUser, final Project project)
    {
        return false;
    }

    @Override
    public Collection getAssociatedIssueSecuritySchemes(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public MultiMap getAssociatedWorkflows(ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }
}