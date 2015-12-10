package com.atlassian.jira.mock.plugin.issuetabpanel;

import java.util.Date;

import com.atlassian.jira.plugin.issuetabpanel.IssueAction;

public class MockIssueAction implements IssueAction
{
    private Date date;

    public MockIssueAction(Date timePerformed)
    {
        date = timePerformed;
    }

    public String getHtml()
    {
        return null;
    }

    public Date getTimePerformed()
    {
        return date;
    }

    public boolean isDisplayActionAllTab()
    {
        return false;
    }
}
