package com.atlassian.jira.event.issue.field.screen;

import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;

import javax.annotation.Nonnull;

/**
 * Abstract event that captures the data relevant to the actual fields stored against a field screen.
 *
 * @since v5.1
 */
@SuppressWarnings ("UnusedDeclaration")
public class AbstractFieldScreenLayoutItemEvent
{
    private final Long id;
    private final Long fieldScreenTabId;
    private final Long fieldScreenId;

    public AbstractFieldScreenLayoutItemEvent(@Nonnull FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        this.id = fieldScreenLayoutItem.getId();

        FieldScreenTab fieldScreenTab = fieldScreenLayoutItem.getFieldScreenTab();
        fieldScreenTabId = fieldScreenTab != null ? fieldScreenTab.getId() : null;
        fieldScreenId = (fieldScreenTab != null && fieldScreenTab.getFieldScreen() != null) ? fieldScreenTab.getFieldScreen().getId() : null;
    }

    public Long getId()
    {
        return id;
    }

    public Long getFieldScreenTabId()
    {
        return fieldScreenTabId;
    }

    public Long getFieldScreenId()
    {
        return fieldScreenId;
    }
}
