/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.jira.jelly.tag.UserAwareActionTagSupport;
import org.apache.log4j.Logger;

public class RemoveGroup extends UserAwareActionTagSupport
{
    private static final Logger log = Logger.getLogger(RemoveGroup.class);
    private static final String GROUP_NAME = "name";

    public RemoveGroup()
    {
        setActionName("DeleteGroup");
    }

    public String[] getRequiredProperties()
    {
        return new String[] { GROUP_NAME };
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[0];
    }
}
