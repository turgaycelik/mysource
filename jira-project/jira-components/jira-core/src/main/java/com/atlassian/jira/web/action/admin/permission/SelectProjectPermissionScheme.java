package com.atlassian.jira.web.action.admin.permission;

import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.scheme.AbstractSelectProjectScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class SelectProjectPermissionScheme extends AbstractSelectProjectScheme
{
    private final PermissionSchemeManager permissionSchemeManager;

    public SelectProjectPermissionScheme(PermissionSchemeManager permissionSchemeManager)
    {
        this.permissionSchemeManager = permissionSchemeManager;
    }

    @Override
    public SchemeManager getSchemeManager()
    {
        return permissionSchemeManager;
    }

    @Override
    protected String getProjectReturnUrl()
    {
        return "/plugins/servlet/project-config/" + getProjectObject().getKey() + "/permissions";
    }
}
