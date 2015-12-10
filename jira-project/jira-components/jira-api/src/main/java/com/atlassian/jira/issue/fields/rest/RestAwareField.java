package com.atlassian.jira.issue.fields.rest;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
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
@PublicApi
public interface RestAwareField
{

    /**
     * Returns lower level Information about the field.
     * This information contains allowed values and/or the autocomplete url
     *
     * @param fieldTypeInfoContext the {@link FieldTypeInfoContext} contains context information that is relevant to generate the {@link FieldTypeInfo}
     */
    FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext);

    /**
     * Return a description of the shape of this field when represented as JSON.
     */
    JsonType getJsonSchema();

    /**
     * Return a FieldJsonDataPair containing a json representation of the raw data for this field, and if required, a json representation
     * of the rendered data for easy display.
     *
     * @param issue to get field data from
     * @param renderedVersionRequested whether the use requested the return of rendered/pretty data as well as raw data
     * @param fieldLayoutItem field layout for this field.  Will only be supplied if the field is also an ordereable field.
     * @return FieldJsonDataPair containing a json representation of the raw data for this field, and if required, a json representation
     * of the rendered data for easy display.
     */
    FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem);
}
