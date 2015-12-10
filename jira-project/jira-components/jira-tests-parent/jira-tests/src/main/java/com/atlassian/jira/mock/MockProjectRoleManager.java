package com.atlassian.jira.mock;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.DefaultRoleActors;
import com.atlassian.jira.security.roles.DefaultRoleActorsImpl;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleActorsImpl;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.security.roles.RoleActorDoesNotExistException;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.MockUser;

import com.google.common.collect.ImmutableList;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

public class MockProjectRoleManager implements ProjectRoleManager
{
    public static final ProjectRole PROJECT_ROLE_TYPE_1 = new ProjectRoleImpl(1L, "Project Administrator", "Can change settings about this project");
    public static final ProjectRole PROJECT_ROLE_TYPE_2 = new ProjectRoleImpl(2L, "Developer", "Works on this project");
    public static final ProjectRole PROJECT_ROLE_TYPE_3 = new ProjectRoleImpl(3L, "User", "Acts as a participant on this project");
    public static final ProjectRole PROJECT_ROLE_TYPE_NULL = new ProjectRoleImpl(null, null, null);
    public static final List<ProjectRole> DEFAULT_ROLE_TYPES = ImmutableList.of(PROJECT_ROLE_TYPE_1, PROJECT_ROLE_TYPE_2, PROJECT_ROLE_TYPE_3);

    private Collection<ProjectRole> projectRoles = newArrayList(DEFAULT_ROLE_TYPES);
    private long idCounter = projectRoles.size();

    public MockProjectRoleManager()
    {
    }

    public Collection<ProjectRole> getProjectRoles()
    {
        return projectRoles;
    }

    public Collection<ProjectRole> getProjectRoles(ApplicationUser user, Project project)
    {
        return null;
    }

    @Override
    public Collection<ProjectRole> getProjectRoles(User user, Project project)
    {
        return getProjectRoles(ApplicationUsers.from(user), project);
    }

    public ProjectRole getProjectRole(Long id)
    {
        for (final Object o : getProjectRoles())
        {
            ProjectRole role = (ProjectRole) o;
            if (role.getId().equals(id))
            {
                return role;
            }
        }
        return null;
    }

    public ProjectRole getProjectRole(String name)
    {
        for (final Object o : getProjectRoles())
        {
            ProjectRole role = (ProjectRole) o;
            if (role.getName().equals(name))
            {
                return role;
            }
        }
        return null;
    }

    public void addRole(ProjectRole projectRole)
    {
        checkRoleNameUnique(projectRole.getName());
        if (getProjectRole(projectRole.getId()) != null)
        {
            throw new IllegalArgumentException("Role with id '" + projectRole.getId() + "' already exists.");
        }

        projectRoles.add(projectRole);
    }

    public ProjectRole createRole(ProjectRole projectRole)
    {
        MockProjectRole role = new MockProjectRole(++idCounter, projectRole.getName(), projectRole.getDescription());
        addRole(projectRole);
        return role;
    }

    public boolean isRoleNameUnique(String name)
    {
        try
        {
            checkRoleNameUnique(name);
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    private void checkRoleNameUnique(String name)
    {
        for (final Object o : getProjectRoles())
        {
            ProjectRole role = (ProjectRole) o;
            if (name != null && name.equals(role.getName()))
            {
                throw new IllegalArgumentException("Cannot have two roles with the same name");
            }
        }
    }

    public void deleteRole(ProjectRole projectRole)
    {
        projectRoles.remove(projectRole);
    }

    public void updateRole(ProjectRole projectRole)
    {
        projectRoles.remove(projectRole);
        projectRoles.add(projectRole);
    }

    public ProjectRoleActors getProjectRoleActors(ProjectRole projectRole, Project project)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Mock bad argument");
        }
        Set<RoleActor> actors = newHashSet();
        Set<User> users = newHashSet();
        users.add(new MockUser("tester", "tester", "tester@test.com"));
        users.add(new MockUser("fred", "fred", "fred@test.com"));

        final Long roleId = projectRole.getId();
        final Long projectId = project.getId();
        try
        {
            actors.add(new MockRoleActor(1L, roleId, projectId, users, MockRoleActor.TYPE, "tester"));
            actors.add(new MockRoleActor(2L, roleId, projectId, users, MockRoleActor.TYPE, "fred"));
        }
        catch (RoleActorDoesNotExistException e)
        {
            throw new RuntimeException(e);
        }
        return new ProjectRoleActorsImpl(projectId, roleId, actors);
    }

    public void updateProjectRoleActors(ProjectRoleActors projectRoleActors)
    {
    }

    public DefaultRoleActors getDefaultRoleActors(ProjectRole projectRole)
    {
        Set<RoleActor> actors = newHashSet();
        Set<User> users = newHashSet();
        users.add(new MockUser("tester", "tester", "tester@test.com"));
        try
        {
            actors.add(new MockRoleActor(1L, projectRole.getId(), null, users, MockRoleActor.TYPE, "tester"));
        }
        catch (RoleActorDoesNotExistException e)
        {
            throw new RuntimeException(e);
        }
        return new DefaultRoleActorsImpl(projectRole.getId(), actors);
    }

    public void updateDefaultRoleActors(DefaultRoleActors defaultRoleActors)
    {
    }

    public void applyDefaultsRolesToProject(Project project)
    {
    }

    public void removeAllRoleActorsByNameAndType(String name, String type)
    {
    }

    public void removeAllRoleActorsByProject(Project project)
    {
    }

    public boolean isUserInProjectRole(ApplicationUser user, ProjectRole projectRole, Project project)
    {
        return false;
    }

    @Override
    public boolean isUserInProjectRole(User user, ProjectRole projectRole, Project project)
    {
        return isUserInProjectRole(ApplicationUsers.from(user), projectRole, project);
    }

    public Collection<Long> getProjectIdsContainingRoleActorByNameAndType(String name, String type)
    {
        return Collections.emptyList();
    }

    @Override
    public ProjectIdToProjectRoleIdsMap createProjectIdToProjectRolesMap(User user, Collection<Long> projectIds)
    {
        return createProjectIdToProjectRolesMap(ApplicationUsers.from(user), projectIds);
    }

    public List<Long> roleActorOfTypeExistsForProjects(List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String projectRoleParameter)
    {
        return null;
    }

    public Map<Long,List<String>> getProjectIdsForUserInGroupsBecauseOfRole(List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String userName)
    {
        return null;
    }

    public static class MockProjectRole implements ProjectRole
    {
        public MockProjectRole(long id, String name, String description)
        {
            this.name = name;
            this.description = description;
            this.id = id;
        }

        private String name;
        private String description;
        private Long id;

        public Long getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public String toString()
        {
            return "Project Role: " + name + '(' + id + ')';
        }
    }

    public static class MockRoleActor implements ProjectRoleActor
    {
        private Long projectRoleId;
        private Long projectId;
        private Set<User> users;
        private String type;
        private Long id;
        private String parameter;
        public static final String INVALID_PARAMETER = "Invalid Parameter";
        public static final String TYPE = "mock type";

        public MockRoleActor(Long id, Long projectRoleId, Long projectId, Set<User> users, String type, String parameter)
                throws RoleActorDoesNotExistException
        {
            this.id = id;
            this.projectId = projectId;
            this.projectRoleId = projectRoleId;
            this.users = users;
            this.type = type;
            setParameter(parameter);
        }

        public Long getId()
        {
            return id;
        }

        public void setId(Long id)
        {
            this.id = id;
        }

        public Long getProjectRoleId()
        {
            return projectRoleId;
        }

        public void setProjectRoleId(Long projectRoleId)
        {
            this.projectRoleId = projectRoleId;
        }

        public Long getProjectId()
        {
            return projectId;
        }

        public void setProjectId(Long projectId)
        {
            this.projectId = projectId;
        }

        public String getPrettyName()
        {
            return "Mock Role Actor";
        }

        public String getDescriptor()
        {
            return type + ':' + parameter;
        }

        public String getType()
        {
            return type;
        }

        public String getParameter()
        {
            return parameter;
        }

        public Set<User> getUsers()
        {
            return users;
        }

        public void setParameter(String parameter) throws RoleActorDoesNotExistException
        {
            if (INVALID_PARAMETER.equals(parameter))
            {
                throw new RoleActorDoesNotExistException("Invalid Param does not exist");
            }
            this.parameter = parameter;
        }

        public boolean contains(ApplicationUser user)
        {
            return users.contains(ApplicationUsers.toDirectoryUser(user));
        }

        @Override
        public boolean contains(User user)
        {
            return users.contains(user);
        }

        @Override
        public boolean isActive()
        {
            return true;
        }

        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final MockRoleActor that = (MockRoleActor) o;
            return parameter.equals(that.parameter) && type.equals(that.type);
        }

        public int hashCode()
        {
            int result;
            result = type.hashCode();
            result = 29 * result + parameter.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return "MockRoleActor[descriptor=" + getDescriptor() + ']';
        }
    }

    public static class MockRoleActorFactory implements RoleActorFactory
    {
        public ProjectRoleActor createRoleActor(Long id, Long projectRoleId, Long projectId, String type, String parameter)
                throws RoleActorDoesNotExistException
        {
            return new MockProjectRoleManager.MockRoleActor(id, projectRoleId, projectId, Collections.<User>emptySet(), type, parameter);
        }

        public Set<RoleActor> optimizeRoleActorSet(Set<RoleActor> roleActors)
        {
            return roleActors;
        }
    }

    public ProjectIdToProjectRoleIdsMap createProjectIdToProjectRolesMap(ApplicationUser user, Collection<Long> projectIds)
    {
        ProjectIdToProjectRoleIdsMap map = new ProjectIdToProjectRoleIdsMap();
        if (projectIds != null && !projectIds.isEmpty())
        {
            ProjectManager projectManager = ComponentAccessor.getProjectManager();
            for (final Long projectId : projectIds)
            {
                Collection<ProjectRole> projectRoles = getProjectRoles(user, projectManager.getProjectObj(projectId));
                for (final Object projectRole1 : projectRoles)
                {
                    ProjectRole projectRole = (ProjectRole) projectRole1;
                    map.add(projectId, projectRole.getId());
                }
            }
        }
        return map;
    }

}
