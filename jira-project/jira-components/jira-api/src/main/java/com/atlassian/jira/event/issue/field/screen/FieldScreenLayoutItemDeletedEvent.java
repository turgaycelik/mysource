package com.atlassian.jira.event.issue.field.screen;

import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;

import javax.annotation.Nonnull;

/**
 * Event indicating a field on a field screen has been updated.
 *
 * @since v5.1
 */
public class FieldScreenLayoutItemDeletedEvent extends AbstractFieldScreenLayoutItemEvent
{
    public FieldScreenLayoutItemDeletedEvent(@Nonnull FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        super(fieldScreenLayoutItem);
    }
}
