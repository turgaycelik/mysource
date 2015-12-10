package com.atlassian.jira.security.roles.actor;

import java.util.HashSet;
import java.util.Set;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.security.roles.RoleActorDoesNotExistException;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.atlassian.jira.user.ApplicationUser;

@Internal
public class GroupRoleActorFactory implements RoleActorFactory
{
    public static final String TYPE = ProjectRoleActor.GROUP_ROLE_ACTOR_TYPE;

    private final GroupManager groupManager;

    public GroupRoleActorFactory(final GroupManager groupManager)
    {
        this.groupManager = groupManager;
    }

    public ProjectRoleActor createRoleActor(Long id, Long projectRoleId, Long projectId, String type, String groupName)
            throws RoleActorDoesNotExistException
    {
        if (!TYPE.equals(type))
        {
            throw new IllegalArgumentException(this.getClass().getName() + " cannot create RoleActors of type: " + type);
        }

        final Group group = groupManager.getGroupEvenWhenUnknown(groupName);

        return new GroupRoleActor(id, projectRoleId, projectId, group, groupManager.groupExists(groupName));
    }

    public Set<RoleActor> optimizeRoleActorSet(Set<RoleActor> roleActors)
    {
        // no optimise for groups
        return roleActors;
    }

    public class GroupRoleActor extends AbstractRoleActor
    {
        private final Group group;
        private final boolean active;

        GroupRoleActor(Long id, Long projectRoleId, Long projectId, Group group, final boolean active)
        {
            super(id, projectRoleId, projectId, group.getName());
            this.group = group;
            this.active = active;
        }

        @Override
        public boolean isActive()
        {
            return active;
        }

        public String getType()
        {
            return GROUP_ROLE_ACTOR_TYPE;
        }

        public String getDescriptor()
        {
            return getParameter();
        }

        public Set<User> getUsers()
        {
            final Set<User> users = new HashSet<User>();

            for (User user : groupManager.getUsersInGroup(group.getName()))
            {
                 users.add(user);
            }
            return users;
        }

        public boolean contains(ApplicationUser user)
        {
            return user != null && groupManager.isUserInGroup(user.getDirectoryUser(), group);
        }

        @Override
        public boolean contains(User user)
        {
            return user != null && groupManager.isUserInGroup(user, group);
        }

        /**
         * Returns a Group object that represents a valid (existing) group or throws an IllegalArgumentException
         * if the group does not exist
         *
         * @return group
         * @throws IllegalArgumentException if group does not exist
         */
        public Group getGroup() throws IllegalArgumentException
        {
            return group;
        }

    }
}
