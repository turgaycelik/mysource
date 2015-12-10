package com.atlassian.jira.web.action.admin.permission;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.scheme.AbstractViewSchemes;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class ViewSchemes extends AbstractViewSchemes
{
    public SchemeManager getSchemeManager()
    {
        return ComponentAccessor.getPermissionSchemeManager();
    }

    public String getRedirectURL()
    {
        return null;
    }
}
