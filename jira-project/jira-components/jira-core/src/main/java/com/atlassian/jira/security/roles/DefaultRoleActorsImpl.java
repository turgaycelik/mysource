package com.atlassian.jira.security.roles;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;

import com.google.common.collect.ImmutableSet;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @see DefaultRoleActors
 */
public class DefaultRoleActorsImpl implements DefaultRoleActors {

    private final Long projectRoleId;
    private final Set<RoleActor> roleActors;

    public DefaultRoleActorsImpl(Long projectRoleId, Set<? extends RoleActor> roleActors)
    {
        this.projectRoleId = projectRoleId;
        this.roleActors = ImmutableSet.copyOf(roleActors);
    }

    /**
     * This will allow you to add a single role actor to the internal Set used
     * for the role actors. This is a convience constructor to allow us to easily add
     * a single RoleActor
     *
     * @param projectRoleId the Project Role Id we are modelling
     * @param roleActor the Project Role we are modelling
     */
    public DefaultRoleActorsImpl(Long projectRoleId, RoleActor roleActor)
    {
        this(projectRoleId, ImmutableSet.of(roleActor));
    }

    public Set<User> getUsers()
    {
        Set<User> allUsers = new HashSet<User>();
        if (roleActors != null)
        {
            for (final Object roleActor : roleActors)
            {
                RoleActor actor = (RoleActor) roleActor;
                for (User user : actor.getUsers())
                {
                    allUsers.add(user);
                }
            }
        }
        return allUsers;
    }

    public Set<ApplicationUser> getApplicationUsers()
    {
        Set<ApplicationUser> allUsers = new HashSet<ApplicationUser>();
        if (roleActors != null)
        {
            for (final Object roleActor : roleActors)
            {
                RoleActor actor = (RoleActor) roleActor;
                for (User user : actor.getUsers())
                {
                    allUsers.add(ApplicationUsers.from(user));
                }
            }

        }
        return allUsers;
    }

    public Set<RoleActor> getRoleActors()
    {
        return roleActors;
    }

    public Long getProjectRoleId()
    {
        return projectRoleId;
    }

    public Set<RoleActor> getRoleActorsByType(String type)
    {
        // catagorize the roleActors by type and return all the users
        Set<RoleActor> roleActorsForType = new TreeSet<RoleActor>(RoleActorComparator.COMPARATOR);
        for (final Object roleActor1 : roleActors)
        {
            RoleActor roleActor = (RoleActor) roleActor1;
            if (roleActor.getType().equals(type))
            {
                roleActorsForType.add(roleActor);
            }
        }
        return roleActorsForType;
    }

    public boolean contains(ApplicationUser user)
    {
        for (final Object roleActor1 : roleActors)
        {
            RoleActor roleActor = (RoleActor) roleActor1;
            if (roleActor.contains(user))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(User user)
    {
        return contains(ApplicationUsers.from(user));
    }

    public DefaultRoleActors addRoleActor(RoleActor roleActor)
    {
        final Set<RoleActor> set = newHashSet(this.roleActors);
        set.add(roleActor);
        return new DefaultRoleActorsImpl(projectRoleId, set);
    }

    public DefaultRoleActors addRoleActors(Collection<? extends RoleActor> roleActors)
    {
        final Set<RoleActor> set = newHashSet(this.roleActors);
        set.addAll(roleActors);
        return new DefaultRoleActorsImpl(projectRoleId, set);
    }

    public DefaultRoleActors removeRoleActor(RoleActor roleActor)
    {
        if (!roleActors.contains(roleActor))
        {
            return this;
        }
        Set<RoleActor> set = newHashSet(this.roleActors);
        set.remove(roleActor);
        return new DefaultRoleActorsImpl(projectRoleId, set);
    }

    public DefaultRoleActors removeRoleActors(Collection<? extends RoleActor> roleActors)
    {
        Set<RoleActor> set = newHashSet(this.roleActors);
        set.removeAll(roleActors);
        return new DefaultRoleActorsImpl(projectRoleId, set);
    }

    @Override
    public String toString()
    {
        return "DefaultRoleActorsImpl[projectRoleId=" + projectRoleId + ",roleActors=" + roleActors + ']';
    }
}
