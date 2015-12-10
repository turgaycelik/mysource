package com.atlassian.jira.event.issue.field.screen;

/**
 * Event indicating a screen has been deleted.
 *
 * @since v5.1
 */
public class FieldScreenDeletedEvent
{
    private final Long id;

    public FieldScreenDeletedEvent(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
}
