package com.atlassian.jira.user;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.user.util.UserKeyStore;

import com.opensymphony.module.propertyset.PropertySet;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultUserPropertyManager implements UserPropertyManager
{
    private static final String ENTITY_TYPE = "ApplicationUser";

    private final UserKeyStore userKeyStore;
    private final JiraPropertySetFactory propertySetFactory;

    public DefaultUserPropertyManager(UserKeyStore userKeyStore, JiraPropertySetFactory propertySetFactory)
    {
        this.userKeyStore = userKeyStore;
        this.propertySetFactory = propertySetFactory;
    }

    @Nonnull
    public PropertySet getPropertySet(@Nonnull ApplicationUser user)
    {
        notNull("user", user);
        return getPropertySetForUserKey(user.getKey());
    }

    @Nonnull
    public PropertySet getPropertySet(@Nonnull User user)
    {
        notNull("user", user);
        return getPropertySetForUserKey(userKeyStore.getKeyForUsername(user.getName()));
    }

    @Nonnull
    public PropertySet getPropertySetForUserKey(@Nonnull String userKey)
    {
        final Long id = userKeyStore.getIdForUserKey(notNull("userKey", userKey));
        if (id == null)
        {
            throw new IllegalStateException("There is no ID mapped for the user key '" + userKey + '\'');
        }
        return propertySetFactory.buildCachingPropertySet(ENTITY_TYPE, id);
    }
}
