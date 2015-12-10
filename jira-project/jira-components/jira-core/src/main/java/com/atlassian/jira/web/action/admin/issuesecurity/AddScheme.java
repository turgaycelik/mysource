/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuesecurity;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.scheme.AbstractAddScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class AddScheme extends AbstractAddScheme
{
    public SchemeManager getSchemeManager()
    {
        return ComponentAccessor.getComponent(IssueSecuritySchemeManager.class);
    }

    public String getRedirectURL()
    {
        return "ViewIssueSecuritySchemes!default.jspa?schemeId=";
    }
}
