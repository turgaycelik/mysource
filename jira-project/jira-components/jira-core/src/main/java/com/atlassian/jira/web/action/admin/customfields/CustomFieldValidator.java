package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.util.ErrorCollection;

public interface CustomFieldValidator
{
    /**
     * Check that the given CustomFieldType is valid.
     *
     * @param fieldType the FieldType
     * @return An ErrorCollection containing any validation errors.
     */
    ErrorCollection validateType(String fieldType);

    /**
     * Check that the given CustomFieldType is valid.
     * This does the same logical checks as {@link #validateType(String)}, but returns a boolean instead of ErrorCollection.
     *
     * @param fieldType the FieldType
     * @return true if the field type is not-null and valid.
     */
    public boolean isValidType(String fieldType);

    ErrorCollection validateDetails(String fieldName, String fieldType, String searcher);
}
