package com.atlassian.jira.event.issue.field.screen;

import com.atlassian.jira.issue.fields.screen.FieldScreen;

/**
 * Event indicating a screen has been created.
 *
 * @since v5.1
 */
public class FieldScreenCreatedEvent extends AbstractFieldScreenEvent
{
    public FieldScreenCreatedEvent(FieldScreen fieldScreen)
    {
        super(fieldScreen);
    }
}
