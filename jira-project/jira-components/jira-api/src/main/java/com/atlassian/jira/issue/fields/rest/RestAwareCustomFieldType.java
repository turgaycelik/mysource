package com.atlassian.jira.issue.fields.rest;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.json.JsonType;

import javax.annotation.Nullable;

/**
 * Fields that implement this interface can:
 * <ul>
 *  <li>Supply meta data for the REST APIs to use.</li>
 *  <li>Optionally Process command verbs from rest resoucres.</li>
 *  <li>Format their data into beans that can be used to generate meaningful REST values.</li>
 * </ul>
 *
 * @since v5.0
 */
@PublicSpi
public interface RestAwareCustomFieldType
{

    /**
     * Returns lower level Information about the field.
     * This information contains allowed values and/or the autocomplete url
     *
     * @param fieldTypeInfoContext context information for generating the {@link FieldTypeInfo}.
     *
     * @return Low level information about the field.
     */
    FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext);

    /**
     * Return a description of the shape of this field when represented as JSON.
     * @param customField
     */
    JsonType getJsonSchema(CustomField customField);

    /**
     * Return a JsonData representation of the field value
     * @param field configuration of the current field
     * @param issue to get field data from
     * @param renderedVersionRequested whether the use requested the return of rendered/pretty data as well as raw data
     * @param fieldLayoutItem field layout for this field.
     *
     * @return FieldJsonDataPair containing a json representation of the raw data for this field, and if required, a json representation
     * of the rendered data for easy display.
     */
    FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem);
}
