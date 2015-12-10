/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.jira.jelly.tag.UserAwareActionTagSupport;
import org.apache.log4j.Logger;

public class RemovePermissionScheme extends UserAwareActionTagSupport
{
    private static final Logger log = Logger.getLogger(RemovePermissionScheme.class);
    private static final String SCHEME_ID = "schemeId";
    private static final String CONFIRMED_FLAG = "confirmed";

    public RemovePermissionScheme()
    {
        setActionName("DeletePermissionScheme");
    }

    public String[] getRequiredProperties()
    {
        return new String[] { SCHEME_ID, CONFIRMED_FLAG };
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[0];
    }
}
