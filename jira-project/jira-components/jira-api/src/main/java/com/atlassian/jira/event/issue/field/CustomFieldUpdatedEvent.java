package com.atlassian.jira.event.issue.field;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.fields.CustomField;

import javax.annotation.Nonnull;

/**
 * Event indicating a custom field has been updated.
 *
 * @since v5.1
 */
public class CustomFieldUpdatedEvent extends AbstractCustomFieldEvent
{
    private final CustomFieldDetails originalCustomField;

    @Internal
    public CustomFieldUpdatedEvent(@Nonnull CustomField customField, @Nonnull final CustomField originalCustomField)
    {
        super(customField);

        this.originalCustomField = new CustomFieldDetailsImpl(originalCustomField);
    }

    public CustomFieldDetails getOriginalCustomField()
    {
        return originalCustomField;
    }
}
