package com.atlassian.jira.imports.project.mapper;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.dbc.Assertions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Allows you to map Users.
 * We keep the whole ExternalUser information so we can create Users that don't exist.
 *
 * @since v3.13
 */
@PublicApi
public class UserMapper extends AbstractMapper
{
    // User keys to ExternalUser
    private final Map<String, ExternalUser> users;
    // user keys
    private final Set<String> usersInUse;
    private final UserUtil userUtil;

    public UserMapper(final UserUtil userUtil)
    {
        this.userUtil = userUtil;
        users = Maps.newHashMap();
        usersInUse = Sets.newHashSet();
    }

    public void registerOldValue(final ExternalUser externalUser)
    {
        Assertions.notNull("externalUser", externalUser);
        // use the user key as the ID
        super.registerOldValue(externalUser.getKey(), externalUser.getName());
        // Remember the whole external user for later use.
        users.put(externalUser.getKey(), externalUser);
    }

    public ExternalUser getExternalUser(final String userKey)
    {
        return users.get(userKey);
    }

    public String getMappedId(final String oldId)
    {
        if (userExists(oldId))
        {
            return oldId;
        }
        else
        {
            return null;
        }
    }

    public void flagUserAsMandatory(final String oldUserKey)
    {
        super.flagValueAsRequired(oldUserKey);
        // The mandatory users supersede the ones that are in use, but optional
        usersInUse.remove(oldUserKey);
    }

    public void flagUserAsInUse(final String oldUserKey)
    {
        // We only want to store this if the user has not already been flagged as mandatory
        if (!getRequiredOldIds().contains(oldUserKey))
        {
            usersInUse.add(oldUserKey);
        }
    }

    public Collection<ExternalUser> getUnmappedMandatoryUsers()
    {
        final Collection<ExternalUser> unmappedUsers = new ArrayList<ExternalUser>();
        for (final String userKey : getRequiredOldIds())
        {
            if (!userExists(userKey))
            {
                ExternalUser user = users.get(userKey);
                if (user == null)
                {
                    user = externalUser(userKey);
                }
                unmappedUsers.add(user);
            }
        }
        return unmappedUsers;
    }

    public List<ExternalUser> getUnmappedMandatoryUsersWithNoRegisteredOldValue()
    {
        final List<ExternalUser> unregisteredUsers = new ArrayList<ExternalUser>();
        for (final String userKey : getRequiredOldIds())
        {
            if (!userExists(userKey))
            {
                final ExternalUser user = users.get(userKey);
                if (user == null)
                {
                    unregisteredUsers.add(externalUser(userKey));
                }
            }
        }
        return unregisteredUsers;
    }

    public List<ExternalUser> getUnmappedUsersInUseWithNoRegisteredOldValue()
    {
        final List<ExternalUser> unregisteredUsers = new ArrayList<ExternalUser>();
        for (final String userKey : usersInUse)
        {
            if (!userExists(userKey))
            {
                final ExternalUser user = users.get(userKey);
                if (user == null)
                {
                    unregisteredUsers.add(externalUser(userKey));
                }
            }
        }
        return unregisteredUsers;
    }

    public Collection<ExternalUser> getUnmappedUsersInUse()
    {
        final Collection<ExternalUser> unmappedUsers = new ArrayList<ExternalUser>();
        for (final String userKey : usersInUse)
        {
            if (!userExists(userKey))
            {
                ExternalUser user = users.get(userKey);
                if (user == null)
                {
                    user = externalUser(userKey);
                }
                unmappedUsers.add(user);
            }
        }
        return unmappedUsers;
    }

    private ExternalUser externalUser(final String userKey)
    {
        return new ExternalUser(userKey, getMappedUserName(userKey), "", "", "");
    }

    /**
     * Returns a List of users that can be automatically created by the import.
     * <p>This includes all optional and mandatory users that aren't in the current system, and the import file has the user details for.</p>
     * <p>Note that this method only makes sense if External User Management is off.</p>
     * @return a List of users that can be automatically created by the import.
     */
    public Collection<ExternalUser> getUsersToAutoCreate()
    {
        final List<ExternalUser> autoCreatable = new ArrayList<ExternalUser>();
        // Add in required users that aren't in the current system, and we have details for.
        for (final String userKey : getRequiredOldIds())
        {
            if (!userExists(userKey))
            {
                // User is not in current system - check if we have details so we can auto-create
                final ExternalUser user = users.get(userKey);
                if (user != null)
                {
                    autoCreatable.add(user);
                }
            }
        }
        // Add in optional users that aren't in the current system, and we have details for.
        for (final String userKey : usersInUse)
        {
            if (!userExists(userKey))
            {
                // User is not in current system - check if we have details so we can auto-create
                final ExternalUser user = users.get(userKey);
                if (user != null)
                {
                    autoCreatable.add(user);
                }
            }
        }
        return autoCreatable;
    }

    public Collection<String> getOptionalOldIds()
    {
        return usersInUse;
    }

    public boolean userExists(final String userKey)
    {
        String userName = getMappedUserName(userKey);
        return userName != null && userUtil.userExists(userName);
    }

    public String getMappedUserKey(String oldUserKey)
    {
        final String userName = getMappedUserName(oldUserKey);
        if (userName != null)
        {
            final ApplicationUser user = userUtil.getUserByName(userName);
            if (user != null)
            {
                return user.getKey();
            }
        }

        return oldUserKey;
    }

    private String getMappedUserName(String userKey)
    {
        String userName = getKey(userKey);
        // if we have a user key but no username mapped to it, this probably mean this is dodgy pre-6.0 data
        // that was upgraded to 6.0+, in which case we should really treat the key as a username
        return (userName == null) ? userKey : userName;
    }
}
