package com.atlassian.jira.functest.framework.admin;

/**
 * Enumeration of default issue events.
 *
 * @since 4.4
 */
public enum DefaultIssueEvents
{

    ISSUE_CREATED(1),
    ISSUE_UPDATED(2),
    ISSUE_ASSIGNED(3),
    ISSUE_RESOLVED(4),
    // TODO add missing
    ISSUE_DELETED(8);

    private final int id;

    DefaultIssueEvents(int id)
    {
        this.id = id;
    }

    public int eventId()
    {
        return id;
    }
}
