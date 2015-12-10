package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserComparator;
import com.atlassian.crowd.embedded.api.UserWithAttributes;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A convenient mock for {@link User} that supplies reasonable behaviour for unit tests.
 * Note that creating a {@link MockUser} does not automatically ensure that it can be
 * resolved by the {@code UserManager} or {@code UserKeyService}, so unit tests may need
 * to provide mocks for one or both of those services as well.
 *
 * @since v4.3
 */
public class MockUser implements UserWithAttributes, Serializable
{
    private String name;
    private String fullName;
    private String email;
    private Map<String, Set<String>> values;
    private boolean active = true;

    /**
     * Convenience constructor that is equivalent to
     * {@link #MockUser(String, String, String, Map) MockUser(username, "", null, null)}.
     */
    public MockUser(final String username)
    {
        this(username, "", null, null);
    }

    /**
     * Convenience constructor that is equivalent to
     * {@link #MockUser(String, String, String, Map) MockUser(username, fullName, email, null)}.
     */
    public MockUser(final String username, final String fullName, final String email)
    {
        this(username, fullName, email, null);
    }

    /**
     * Creates a new mock user with the specified information.
     *
     * @param username the value to be returned for {@link #getName()}
     * @param fullName the value to be returned for {@link #getDisplayName()}
     * @param email the value to be returned for {@link #getEmailAddress()}
     * @param values a map to provide user attributes, such as are returned
     *      by {@link #getKeys()} and {@link #getValues(String)}.  May be
     *      {@code null}, in which case an empty map is used.
     */
    public MockUser(final String username, final String fullName, final String email, Map<String, Set<String>> values)
    {
        this.name = username;
        this.fullName = fullName;
        this.email = email;
        if (values == null)
        {
            this.values = Collections.emptyMap();
        }
        else
        {
            this.values = values;
        }
    }

    /**
     * A {@code MockUser} is always active by default.
     */
    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public String getEmailAddress()
    {
        return email;
    }

    public String getDisplayName()
    {
        return fullName;
    }

    public long getDirectoryId()
    {
        return 1;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return "<User " + name + ">";
    }

    public Set<String> getValues(final String key)
    {
        return values.get(key);
    }

    public String getValue(final String key)
    {
        Set<String> allValues = values.get(key);
        if (allValues != null && allValues.size() > 0)
        {
            return allValues.iterator().next();
        }
        return null;
    }

    public Set<String> getKeys()
    {
        return values.keySet();
    }

    public boolean isEmpty()
    {
        return values.size() == 0;
    }

    @Override
    public boolean equals(Object o)
    {
        return (o instanceof User) && UserComparator.equal(this, (User) o);
    }

    @Override
    public int hashCode()
    {
        return UserComparator.hashCode(this);
    }

    public int compareTo(final com.atlassian.crowd.embedded.api.User other)
    {
        return UserComparator.compareTo(this, other);
    }
}
