package com.atlassian.jira.web.action.admin.issuesecurity;

import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.scheme.AbstractSelectProjectScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class SelectProjectIssueSecurityScheme extends AbstractSelectProjectScheme
{
    private final IssueSecuritySchemeManager issueSecuritySchemeManager;

    public SelectProjectIssueSecurityScheme(IssueSecuritySchemeManager issueSecuritySchemeManager)
    {
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
    }

    @Override
    public SchemeManager getSchemeManager()
    {
        return issueSecuritySchemeManager;
    }
}
