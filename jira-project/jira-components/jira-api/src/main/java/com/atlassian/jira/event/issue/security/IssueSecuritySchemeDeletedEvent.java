package com.atlassian.jira.event.issue.security;

/**
 * Event indicating an issue security scheme has been deleted.
 *
 * @since v5.0
 */
public class IssueSecuritySchemeDeletedEvent
{
    private Long id;

    public IssueSecuritySchemeDeletedEvent(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
}
