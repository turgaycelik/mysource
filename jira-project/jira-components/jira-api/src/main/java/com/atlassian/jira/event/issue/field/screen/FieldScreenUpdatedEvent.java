package com.atlassian.jira.event.issue.field.screen;

import com.atlassian.jira.issue.fields.screen.FieldScreen;

/**
 * Event indicating a screen has been updated.
 *
 * @since v5.1
 */
public class FieldScreenUpdatedEvent extends AbstractFieldScreenEvent
{
    public FieldScreenUpdatedEvent(FieldScreen fieldScreen)
    {
        super(fieldScreen);
    }
}
