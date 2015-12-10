/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.user.usermapper;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.util.UserManager;

import java.util.Properties;

public class PropertyFileUserMapper extends AbstractUserMapper
{
    private final Properties properties;

    public PropertyFileUserMapper(UserManager userManager, Properties properties)
    {
        super(userManager);
        this.properties = properties;
    }

    public User getUserFromEmailAddress(String emailAddress)
    {
        String username = properties.getProperty(emailAddress);
        return getUser(username);
    }
}
