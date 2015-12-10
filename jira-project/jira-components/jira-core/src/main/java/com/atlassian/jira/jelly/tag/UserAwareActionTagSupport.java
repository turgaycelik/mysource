/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.jelly.ActionTagSupport;
import com.atlassian.jira.jelly.UserAware;

public abstract class UserAwareActionTagSupport extends ActionTagSupport implements UserAware
{
    private final String[] requiredContextVariables = new String[] { JellyTagConstants.USERNAME };

    public String[] getRequiredContextVariables()
    {
        return requiredContextVariables;
    }

    public String getUsername()
    {
        return (String) getContext().getVariable(JellyTagConstants.USERNAME);
    }

    public User getUser()
    {
        return ManagerFactory.getUserManager().getUser(getUsername());
    }
}
