/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.user.usermapper;

import com.atlassian.crowd.embedded.api.User;

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A caching chain of userMappers
 */
public class UserMapperChain implements UserMapper
{
    private final Collection userMappers;
    private final Map userMapCache = new WeakHashMap();

    public UserMapperChain(Collection userMappers)
    {
        this.userMappers = userMappers;
    }

    public User getUserFromEmailAddress(String emailAddress)
    {
        User user = (User) userMapCache.get(emailAddress);
        if (user != null)
            return user;

        for (final Object userMapper1 : userMappers)
        {
            UserMapper userMapper = (UserMapper) userMapper1;
            user = userMapper.getUserFromEmailAddress(emailAddress);
            if (user != null)
            {
                userMapCache.put(emailAddress, user);
                return user;
            }
        }
        return null;
    }
}
