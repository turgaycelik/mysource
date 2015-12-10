package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.web.action.JiraWebActionSupport;

public class CantBrowseCreatedIssue extends JiraWebActionSupport
{
    private String issueKey;
    private boolean converted;

    private final IssueManager issueManager;
    private final UserManager userManager;
    private static final String CONVERTED = "converted";

    public CantBrowseCreatedIssue(final IssueManager issueManager, final UserManager userManager)
    {
        this.issueManager = issueManager;
        this.userManager = userManager;
    }

    protected String doExecute() throws Exception
    {
        Issue issue = issueManager.getIssueObject(issueKey);
        if (issue != null && ComponentAccessor.getPermissionManager().hasPermission(Permissions.BROWSE, issue, getLoggedInUser()))
        {
            return getRedirect("/browse/" + issueKey);
        }

        if(!converted)
        {
            return super.doExecute();
        }
        else
        {
            return CONVERTED;
        }
    }

    public boolean isConverted()
    {
        return converted;
    }

    public void setConverted(boolean converted)
    {
        this.converted = converted;
    }

    public String getIssueKey()
    {
        return issueKey;
    }

    public void setIssueKey(String issueKey)
    {
        this.issueKey = issueKey;
    }

    public boolean isAllowSignUp()
    {
        return userManager.hasPasswordWritableDirectory() && JiraUtils.isPublicMode();
    }
}
