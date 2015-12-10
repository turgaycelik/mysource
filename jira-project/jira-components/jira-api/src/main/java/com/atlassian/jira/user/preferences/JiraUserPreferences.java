package com.atlassian.jira.user.preferences;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.Internal;
import com.atlassian.core.AtlassianCoreException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;

import com.google.common.base.Objects;
import com.opensymphony.module.propertyset.PropertySet;

/**
 * The JIRA implementation {@code atlassian-core}'s {@code Preferences}.  Preferences that have not
 * been set will default to the corresponding value saved in the {@link ApplicationProperties}.
 * <p>
 * <strong>WARNING</strong>: This class should not be in the API and will be moved to its rightful
 * home in {@code jira-core}, possibly as early as JIRA 7.0.  As such, all of its public constructors
 * are {@code deprecated}.  Use the {@link UserPreferencesManager} to get a user's preferences
 * instead of referencing this class directly.
 * </p>
 */
@Internal  // TODO Move this class to jira-core in 7.0
public class JiraUserPreferences implements ExtendedPreferences
{
    private static final String ANONYMOUS_USER = "You cannot set properties on a null user.";

    private final String userKey;
    private final PropertySet propertySet;



    JiraUserPreferences(final String userKey, final PropertySet propertySet)
    {
        this.userKey = userKey;
        this.propertySet = propertySet;
    }



    /**
     * Returns a user preferences placeholder that is suitable for an anonymous user and
     * defaults to the global preferences defined by {@link ApplicationProperties} for
     * everything.  Attempts to modify the preferences throw {@link AtlassianCoreException}.
     *
     * @deprecated You should not construct user preferences directly.  Request them through the
     *      {@link UserPreferencesManager}, instead.  Since v6.2.
     */
    @Deprecated
    public JiraUserPreferences()
    {
        this(null, null);
    }

    /**
     * @deprecated You should not construct user preferences directly.  Request them through the
     *      {@link UserPreferencesManager}, instead.  Since v6.2.
     */
    @Deprecated
    public JiraUserPreferences(final ApplicationUser user)
    {
        this(ApplicationUsers.getKeyFor(user), getPropertySet(user));
    }

    /**
     * @deprecated You should not construct user preferences directly.  Request them through the
     *      {@link UserPreferencesManager}, instead.  Since v6.2.
     */
    @Deprecated
    public JiraUserPreferences(final User pUser)
    {
        this(ApplicationUsers.from(pUser));
    }

    /**
     * @deprecated You should not construct user preferences directly.  Further, preferences
     *      created with this particular constructor do not have a reference to the user and therefore
     *      may not behave correctly in all circumstances.  In particular, {@link #getUserKey()},
     *      {@link #equals(Object)} and {@link #hashCode()} will all behave as they would for an
     *      anonymous user, while the actual preferences returned will instead match those provided
     *      by the property set.  Request user preferences through the {@link UserPreferencesManager},
     *      instead.  Since v6.2.
     */
    @Deprecated
    public JiraUserPreferences(PropertySet userPs)
    {
        this(null, userPs);
    }



    @Override
    public long getLong(String key)
    {
        if (propertySet != null && propertySet.exists(key))
        {
            return propertySet.getLong(key);
        }
        return Long.parseLong(getApplicationProperties().getDefaultBackedString(key));
    }

    @Override
    public String getString(String key)
    {
        if (propertySet != null && propertySet.exists(key))
        {
            return propertySet.getString(key);
        }
        return getApplicationProperties().getDefaultBackedString(key);
    }

    @Override
    public String getText(String key)
    {
        if (propertySet != null && propertySet.exists(key))
        {
            return propertySet.getText(key);
        }
        return getApplicationProperties().getDefaultBackedText(key);
    }

    @Override
    public boolean getBoolean(String key)
    {
        if (propertySet != null && propertySet.exists(key))
        {
            return propertySet.getBoolean(key);
        }
        return getApplicationProperties().getOption(key);
    }

    public void setLong(String key, long value) throws AtlassianCoreException
    {
        if (propertySet == null)
        {
            throw new AtlassianCoreException(ANONYMOUS_USER);
        }
        propertySet.setLong(key, value);
    }

    public void setString(String key, String value) throws AtlassianCoreException
    {
        if (propertySet == null)
        {
            throw new AtlassianCoreException(ANONYMOUS_USER);
        }
        propertySet.setString(key, value);
    }

    @Override
    public void setText(String key, String value) throws AtlassianCoreException
    {
        if (propertySet == null)
        {
            throw new AtlassianCoreException(ANONYMOUS_USER);
        }
        propertySet.setText(key, value);
    }

    public void setBoolean(String key, boolean value) throws AtlassianCoreException
    {
        if (propertySet == null)
        {
            throw new AtlassianCoreException(ANONYMOUS_USER);
        }
        propertySet.setBoolean(key, value);
    }

    public void remove(String key) throws AtlassianCoreException
    {
        if (propertySet == null)
        {
            throw new AtlassianCoreException(ANONYMOUS_USER);
        }
        if (!propertySet.exists(key))
        {
            throw new AtlassianCoreException("The property with key '" + key + "' does not exist.");
        }
        propertySet.remove(key);
    }

    ApplicationProperties getApplicationProperties()
    {
        return ComponentAccessor.getApplicationProperties();
    }


    @Override
    public String getUserKey()
    {
        return userKey;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        return o instanceof ExtendedPreferences && equals((ExtendedPreferences)o);
    }

    private boolean equals(@Nonnull ExtendedPreferences other)
    {
        return Objects.equal(userKey, other.getUserKey());
    }

    public int hashCode()
    {
        return (userKey != null) ? userKey.hashCode() : 0;
    }

    /**
     * Returns true if the specified property is set.
     *
     * @param key - property to be tested.
     * @since 5.2.2
     */
    public boolean containsValue(final String key)
    {
        return propertySet.exists(key);
    }



    @Nullable
    private static PropertySet getPropertySet(final ApplicationUser user)
    {
        if (user == null)
        {
            return null;
        }
        return ComponentAccessor.getUserPropertyManager().getPropertySet(user);
    }
}
