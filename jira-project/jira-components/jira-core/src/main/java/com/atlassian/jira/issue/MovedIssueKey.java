package com.atlassian.jira.issue;

/**
 * @since v6.1
 */
public class MovedIssueKey
{
    public static final String ID = "id";
    public static final String OLD_ISSUE_KEY = "oldIssueKey";
    public static final String ISSUE_ID = "issueId";

    private final Long id;
    private final String oldIssueKey;
    private final Long issueId;

    public MovedIssueKey(Long id, String oldIssueKey, Long issueId)
    {
        this.id = id;
        this.oldIssueKey = oldIssueKey;
        this.issueId = issueId;
    }

    public Long getId()
    {
        return id;
    }

    public String getOldIssueKey()
    {
        return oldIssueKey;
    }

    public Long getIssueId()
    {
        return issueId;
    }
}
