package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An ApplicationUser comprising of a String key and an embedded crowd User.
 *
 * @since v5.1.1
 */
public class DelegatingApplicationUser implements ApplicationUser
{
    private final String key;
    private final User user;
    private final User bridgedUser;

    public DelegatingApplicationUser(final String key, final User user)
    {
        this.key = notNull("key", key);
        this.user = notNull("user", user);
        // helps us convert efficiently between ApplicationUser and Directory user.
        this.bridgedUser = new BridgedDirectoryUser(user, this);
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public boolean isActive()
    {
        return user.isActive();
    }

    @Override
    public String getEmailAddress()
    {
        return user.getEmailAddress();
    }

    @Override
    public String getDisplayName()
    {
        return user.getDisplayName();
    }

    @Override
    public User getDirectoryUser()
    {
        return bridgedUser;
    }

    @Override
    public String getUsername()
    {
        return user.getName();
    }

    @Override
    public String getName()
    {
        return user.getName();
    }

    @Override
    public long getDirectoryId()
    {
        return user.getDirectoryId();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj instanceof ApplicationUser)
        {
            final ApplicationUser other = (ApplicationUser) obj;
            return key.equals(other.getKey());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return key.hashCode();
    }

    @Override
    public String toString()
    {
        return getUsername() + '(' + getKey() + ')';
    }
}
