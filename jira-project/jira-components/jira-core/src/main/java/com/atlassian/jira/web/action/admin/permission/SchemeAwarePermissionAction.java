/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.permission;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.scheme.AbstractSchemeAwareAction;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.scheme.SchemeType;

/**
 * This class is used as a super class for any classes that perform actions on permission schemes.
 * It contains the schemeId and the actual (GenericValue) scheme object
 */
public class SchemeAwarePermissionAction extends AbstractSchemeAwareAction
{
    public SchemeType getType(String id)
    {
        return ManagerFactory.getPermissionTypeManager().getSchemeType(id);
    }

    public SchemeManager getSchemeManager()
    {
        return ManagerFactory.getPermissionSchemeManager();
    }

    public String getRedirectURL()
    {
        return null;
    }
}
