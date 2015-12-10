/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.permission;

import com.atlassian.jira.security.AbstractSecurityTypeManager;
import com.atlassian.jira.security.SecurityTypeManager;

/**
 * This class reads the permission-types.xml file for the different types of permission that are used.
 * These can be GroupDropdown, CurrentReporter etc
 */
public class PermissionTypeManager extends AbstractSecurityTypeManager implements SecurityTypeManager
{
    /**
     * THERE IS NOTHING IN HERE BECAUSE PERMISSION TYPES AND ISSUE LEVEL SECURITIES TYPE ARE THE SAME
     *
     * IF THEY NEED TO BE DIFFERENT WE NEED TO LOOK AT WHERE THEY ARE USED
     */
}
