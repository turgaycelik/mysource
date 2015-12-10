package com.atlassian.jira.event.issue.field.screen;

import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;

import javax.annotation.Nonnull;

/**
 * Event indicating a field has been removed from a field screen.
 *
 * @since v5.1
 */
public class FieldScreenLayoutItemUpdatedEvent extends AbstractFieldScreenLayoutItemEvent
{
    public FieldScreenLayoutItemUpdatedEvent(@Nonnull FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        super(fieldScreenLayoutItem);
    }
}
