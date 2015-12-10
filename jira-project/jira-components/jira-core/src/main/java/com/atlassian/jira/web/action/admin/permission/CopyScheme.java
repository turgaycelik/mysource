/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.permission;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.scheme.AbstractCopyScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class CopyScheme extends AbstractCopyScheme
{
    public SchemeManager getSchemeManager()
    {
        return ComponentAccessor.getPermissionSchemeManager();
    }

    public String getRedirectURL()
    {
        return "ViewPermissionSchemes.jspa";
    }
}
