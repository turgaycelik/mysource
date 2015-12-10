package com.atlassian.jira.external.beans;

/**
 * Used to represent a single voter when importing data.
 *
 * @since v3.13
 */
public class ExternalVoter
{
    private String issueId;
    private String voter;

    public String getIssueId()
    {
        return issueId;
    }

    public void setIssueId(final String issueId)
    {
        this.issueId = issueId;
    }

    public String getVoter()
    {
        return voter;
    }

    public void setVoter(final String voter)
    {
        this.voter = voter;
    }

}
