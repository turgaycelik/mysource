package com.atlassian.jira.event.issue.field.screen;

import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;

import javax.annotation.Nonnull;

/**
 * Event indicating a field has been added to a field screen.
 *
 * @since v5.1
 */
public class FieldScreenLayoutItemCreatedEvent extends AbstractFieldScreenLayoutItemEvent
{
    public FieldScreenLayoutItemCreatedEvent(@Nonnull FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        super(fieldScreenLayoutItem);
    }
}
