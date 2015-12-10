package com.atlassian.jira.event.issue.field;

import com.atlassian.jira.issue.fields.CustomField;

import javax.annotation.Nonnull;

/**
 * Event indicating a custom field has been created.
 *
 * @since v5.1
 */
public class CustomFieldCreatedEvent extends AbstractCustomFieldEvent
{
    public CustomFieldCreatedEvent(@Nonnull CustomField customField)
    {
        super(customField);
    }
}
