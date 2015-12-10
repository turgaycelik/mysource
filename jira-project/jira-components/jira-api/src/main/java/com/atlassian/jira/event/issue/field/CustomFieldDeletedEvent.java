package com.atlassian.jira.event.issue.field;

import com.atlassian.jira.issue.fields.CustomField;

import javax.annotation.Nonnull;

/**
 * Event indicating a custom field has been deleted.
 *
 * @since v5.1
 */
public class CustomFieldDeletedEvent extends AbstractCustomFieldEvent
{
    public CustomFieldDeletedEvent(@Nonnull CustomField customField)
    {
        super(customField);
    }
}
