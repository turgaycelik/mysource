package com.atlassian.jira.imports.project.customfield;

import com.atlassian.annotations.PublicApi;

/**
 * Represents a custom field value that has been taken from backup data.
 *
 * @since v3.13
 */
@PublicApi
public interface ExternalCustomFieldValue
{
    /**
     * Returns the ID of the CustomFieldValue.
     *
     * @return the ID of the CustomFieldValue.
     */
    String getId();

    /**
     * Returns the ID of the CustomField that this value is stored for.
     *
     * @return the ID of the CustomField that this value is stored for.
     */
    String getCustomFieldId();

    /**
     * Returns the Issue ID that this value was stored against.
     *
     * @return the Issue ID that this value was stored against.
     */
    String getIssueId();

    /**
     * Returns the Parent Key for this Custom Field Value.
     * This is normally null, but the "Cascading Select" Custom Field will store the parent Option ID in this field.
     *
     * @return the Parent Key for this Custom Field Value.
     */
    String getParentKey();

    /**
     * Returns the String representation of the value that is stored.
     *
     * @return the String representation of the value that is stored.
     */
    String getValue();

    String getDateValue();

    String getNumberValue();

    String getStringValue();

    String getTextValue();
}
