package com.atlassian.jira.security.roles;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.project.Project;

/**
 * This is an interface that defines the storage class for ProjectRoles and RoleActors.
 */
public interface ProjectRoleAndActorStore
{
    ProjectRole addProjectRole(ProjectRole projectRole);

    void updateProjectRole(ProjectRole projectRole);

    Collection<ProjectRole> getAllProjectRoles();

    ProjectRole getProjectRole(Long id);

    ProjectRole getProjectRoleByName(String name);

    void deleteProjectRole(ProjectRole projectRole);

    ProjectRoleActors getProjectRoleActors(Long projectRoleId, Long projectId);

    void updateProjectRoleActors(ProjectRoleActors projectRoleActors);

    void updateDefaultRoleActors(DefaultRoleActors defaultRoleActors);

    DefaultRoleActors getDefaultRoleActors(Long projectRoleId);

    void applyDefaultsRolesToProject(Project project);

    void removeAllRoleActorsByKeyAndType(String key, String type);

    void removeAllRoleActorsByProject(Project project);

    Collection<Long> getProjectIdsContainingRoleActorByKeyAndType(String key, String type);

    List<Long> roleActorOfTypeExistsForProjects(List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String projectRoleParameter);

    Map<Long, List<String>> getProjectIdsForUserInGroupsBecauseOfRole(List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String userKey);
}
