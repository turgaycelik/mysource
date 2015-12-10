package com.atlassian.jira.external.beans;

/**
 * Used to represent a single watcher when importing data.
 *
 * @since v3.13
 */
public class ExternalWatcher
{
    private String issueId;
    private String watcher;

    public String getIssueId()
    {
        return issueId;
    }

    public void setIssueId(final String issueId)
    {
        this.issueId = issueId;
    }

    public String getWatcher()
    {
        return watcher;
    }

    public void setWatcher(final String watcher)
    {
        this.watcher = watcher;
    }

}
