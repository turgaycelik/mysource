package com.atlassian.jira.event.issue.field;

import com.atlassian.jira.issue.fields.CustomField;

import javax.annotation.Nonnull;

/**
 * Abstract event that captures the data relevant to custom field events.
 *
 * @since v5.1
 */
public class AbstractCustomFieldEvent
{
    private final CustomFieldDetails customField;

    public AbstractCustomFieldEvent(@Nonnull CustomField customField)
    {
        this.customField = new CustomFieldDetailsImpl(customField);
    }

    /**
     * Returns the ID of the custom field that this event relates to, as a number. Note that the custom field's full ID
     * is returned by {@link #getCustomFieldId()}.
     *
     * @return a Long containing the numeric id of the custom field
     */
    public Long getId()
    {
        return customField.getIdAsLong();
    }

    /**
     * Returns the ID of the custom field that this event relates to. The custom field's string ID will have the form
     * "customfield_XXXXX", where XXXXX is the value of {@link #getId()}.
     *
     * @return a String containing the ID of the custom field
     */
    public String getCustomFieldId()
    {
        return customField.getId();
    }

    public String getFieldType()
    {
        return customField.getFieldTypeName();
    }

    public CustomFieldDetails getCustomField()
    {
        return customField;
    }
}
