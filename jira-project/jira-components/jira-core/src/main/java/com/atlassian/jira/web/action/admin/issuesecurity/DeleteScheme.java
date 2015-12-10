/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuesecurity;

import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.scheme.AbstractDeleteScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class DeleteScheme extends AbstractDeleteScheme
{
    private final IssueSecuritySchemeManager issueSecuritySchemeManager;

    public DeleteScheme(final IssueSecuritySchemeManager issueSecuritySchemeManager)
    {
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
    }

    @Override
    public SchemeManager getSchemeManager()
    {
        return issueSecuritySchemeManager;
    }

    @Override
    public String getRedirectURL()
    {
        return "ViewIssueSecuritySchemes.jspa";
    }
}
