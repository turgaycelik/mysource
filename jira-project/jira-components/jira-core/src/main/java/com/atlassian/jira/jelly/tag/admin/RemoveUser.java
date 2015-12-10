/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.jira.jelly.tag.UserAwareActionTagSupport;
import org.apache.log4j.Logger;

public class RemoveUser extends UserAwareActionTagSupport
{
    private static final transient Logger log = Logger.getLogger(RemoveUser.class);
    private static final String USERNAME = "name";

    public RemoveUser()
    {
        setActionName("DeleteUser");
    }

    public String[] getRequiredProperties()
    {
        return new String[] { USERNAME };
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[0];
    }

    protected void preContextValidation()
    {
        super.preContextValidation();
        setProperty("confirm", "true");
    }
}
