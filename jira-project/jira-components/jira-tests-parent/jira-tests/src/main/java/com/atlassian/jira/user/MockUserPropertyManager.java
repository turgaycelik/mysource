package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.mock.propertyset.MockPropertySet;
import com.google.common.collect.Maps;
import com.opensymphony.module.propertyset.PropertySet;

import java.util.Map;

/**
 * @since v6.2.3
 */
public class MockUserPropertyManager implements UserPropertyManager
{
    private final Map<String, PropertySet> mappings = Maps.newHashMap();

    @Override
    public PropertySet getPropertySet(final ApplicationUser user)
    {
        return mappings.get(user.getKey());
    }

    @Override
    public PropertySet getPropertySet(final User user)
    {
        return mappings.get(user.getName());
    }

    @Override
    public PropertySet getPropertySetForUserKey(final String userkey)
    {
        return mappings.get(userkey);
    }

    public PropertySet createOrGetForUser(User user)
    {
        return createOrGetForKey(user.getName());
    }

    public PropertySet createOrGetForUser(ApplicationUser user)
    {
        return createOrGetForKey(user.getKey());
    }

    public PropertySet createOrGetForKey(final String key)
    {
        PropertySet userProperties = mappings.get(key);
        if (userProperties == null)
        {
            mappings.put(key, userProperties = new MockPropertySet());
        }
        return userProperties;
    }
}
