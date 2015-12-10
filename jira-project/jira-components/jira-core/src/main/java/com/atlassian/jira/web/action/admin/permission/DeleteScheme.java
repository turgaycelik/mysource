/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.permission;

import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.scheme.AbstractDeleteScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class DeleteScheme extends AbstractDeleteScheme
{
    private final PermissionSchemeManager permissionSchemeManager;

    public DeleteScheme(final PermissionSchemeManager permissionSchemeManager)
    {
        this.permissionSchemeManager = permissionSchemeManager;
    }

    @Override
    public SchemeManager getSchemeManager()
    {
        return permissionSchemeManager;
    }

    @Override
    public String getRedirectURL()
    {
        return "ViewPermissionSchemes.jspa";
    }
}
