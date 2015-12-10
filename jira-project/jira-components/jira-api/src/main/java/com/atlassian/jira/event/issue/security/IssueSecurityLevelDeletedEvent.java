package com.atlassian.jira.event.issue.security;

/**
 * Event indicating a security level has been removed from an issue security scheme.
 *
 * @since v5.0
 */
public class IssueSecurityLevelDeletedEvent
{
    private Long id;

    public IssueSecurityLevelDeletedEvent(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
}
