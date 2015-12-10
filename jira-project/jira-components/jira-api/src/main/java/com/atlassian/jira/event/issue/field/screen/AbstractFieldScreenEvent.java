package com.atlassian.jira.event.issue.field.screen;

import com.atlassian.jira.issue.fields.screen.FieldScreen;

/**
 * Abstract event that captures the data relevant to field screen events.
 *
 * @since v5.1
 */
public class AbstractFieldScreenEvent
{
    private Long id;

    public AbstractFieldScreenEvent(FieldScreen fieldScreen)
    {
        if (null != fieldScreen)
        {
            this.id = fieldScreen.getId();
        }
    }

    public Long getId()
    {
        return id;
    }
}
