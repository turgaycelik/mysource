package com.atlassian.jira.security.roles.actor;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.security.roles.RoleActorDoesNotExistException;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import com.google.common.base.Predicate;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Iterables.all;

/**
 * Responsible for construction of UserRoleActor instances. Also optimises the
 * lookup where we have many users in a particular role for a project by doing
 * a map lookup based on the username.
 * <p>
 * Access to the actual User instance is via a UserFactory so we can unit-test.
 * The production dependency is set in the default ctor.
 */
@Internal
public class UserRoleActorFactory implements RoleActorFactory
{
    public static final String TYPE = "atlassian-user-role-actor";

    private UserFactory userFactory;
    private final UserManager userManager;

    public UserRoleActorFactory(final UserManager userManager)
    {
        // plugin the production factory here
        this(new UserFactory()
        {
            public ApplicationUser getUser(String userKey)
            {
                return userManager.getUserByKeyEvenWhenUnknown(userKey);
            }
        }, userManager);
    }

    UserRoleActorFactory(final UserFactory userFactory, final UserManager userManager)
    {
        if (userFactory == null)
        {
            throw new IllegalArgumentException(this.getClass().getName() + " cannot be constructed without a UserFactory instance");
        }
        this.userFactory = userFactory;
        this.userManager = userManager;
    }

    public ProjectRoleActor createRoleActor(Long id, Long projectRoleId, Long projectId, String type, String parameter)
            throws RoleActorDoesNotExistException
    {
        if (!TYPE.equals(type))
        {
            throw new IllegalArgumentException(this.getClass().getName() + " cannot create RoleActors of type: " + type);
        }
        Assertions.notNull("parameter", parameter);
        ApplicationUser user = userFactory.getUser(parameter);
        if (user == null)
        {
            throw new RoleActorDoesNotExistException("User '" + parameter + "' does not exist.");
        }
        return new UserRoleActor(id, projectRoleId, projectId, user);
    }

    public Set<RoleActor> optimizeRoleActorSet(Set<RoleActor> roleActors)
    {
        Set<RoleActor> originals = new HashSet<RoleActor>(roleActors);
        Set<UserRoleActor> userRoleActors = new HashSet<UserRoleActor>(roleActors.size());
        for (Iterator<RoleActor> it = originals.iterator(); it.hasNext();)
        {
            RoleActor roleActor = it.next();
            if (roleActor instanceof UserRoleActor)
            {
                userRoleActors.add((UserRoleActor) roleActor);
                it.remove();
            }
        }
        if (!userRoleActors.isEmpty())
        {
            // no point aggregating if there's only one
            if (userRoleActors.size() > 1)
            {
                UserRoleActor prototype = userRoleActors.iterator().next();
                originals.add(new AggregateRoleActor(prototype, userRoleActors));
            }
            else
            {
                // just one? throw it back...
                originals.addAll(userRoleActors);
            }
        }
        return Collections.unmodifiableSet(originals);
    }

    class UserRoleActor extends AbstractRoleActor
    {
        private UserRoleActor(Long id, Long projectRoleId, Long projectId, ApplicationUser user)
        {
            super(id, projectRoleId, projectId, ApplicationUsers.getKeyFor(user));
        }

        public String getType()
        {
            return TYPE;
        }

        @Override
        public boolean isActive()
        {
            return getUser().isActive();
        }

        public String getDescriptor()
        {
            return getAppUser().getDisplayName();
        }

        public Set<User> getUsers()
        {
            return CollectionBuilder.newBuilder(getUser()).asSet();
        }

        public boolean contains(ApplicationUser user)
        {
            return user != null && user.getKey().equals(getParameter());
        }

        @Override
        public boolean contains(User user)
        {
            return this.contains(ApplicationUsers.from(user));
        }

        private ApplicationUser getAppUser()
        {
            return userManager.getUserByKeyEvenWhenUnknown(getParameter());
        }

        private User getUser()
        {
            return ApplicationUsers.toDirectoryUser(userManager.getUserByKeyEvenWhenUnknown(getParameter()));
        }

    }

    /**
     * Aggregate UserRoleActors and look them up based on the hashcode
     */
    static class AggregateRoleActor extends AbstractRoleActor
    {
        private final Map <String, UserRoleActor> userRoleActorMap;

        private AggregateRoleActor(ProjectRoleActor prototype, Set<UserRoleActor> roleActors)
        {
            super(null, prototype.getProjectRoleId(), prototype.getProjectId(), null);

            final Map<String, UserRoleActor> map = new HashMap<String, UserRoleActor>(roleActors.size());

            for (final UserRoleActor userRoleActor : roleActors)
            {
                map.put(userRoleActor.getParameter(), userRoleActor);
            }
            this.userRoleActorMap = Collections.unmodifiableMap(map);
        }

        @Override
        public boolean isActive()
        {
            return all(userRoleActorMap.values(), new Predicate<UserRoleActor>()
            {
                @Override
                public boolean apply(@Nullable UserRoleActor user)
                {
                    return user != null && user.isActive();
                }
            });
        }

        @Override
        public boolean contains(ApplicationUser user)
        {
            return user != null && userRoleActorMap.containsKey(user.getKey())
                    && userRoleActorMap.get(user.getKey()).contains(user);
        }

        @Override
        public boolean contains(User user)
        {
            return contains(ApplicationUsers.from(user));
        }

        /*
         * not enormously efficient, could cache users maybe, we want contains to be fast...
         *
         * @see com.atlassian.jira.security.roles.RoleActor#getUsers()
         */
        public Set<User> getUsers()
        {
            final Set<User> result = new HashSet<User>(userRoleActorMap.size());
            for (UserRoleActor roleActor : userRoleActorMap.values())
            {
                // not the most efficient, but generally called in UI etc.
                result.addAll(roleActor.getUsers());
            }
            return Collections.unmodifiableSet(result);
        }

        public String getType()
        {
            return TYPE;
        }
    }

    /**
     * Used to mock out calls to stupid OSUser.
     */
    interface UserFactory
    {
        ApplicationUser getUser(String name);
    }
}
