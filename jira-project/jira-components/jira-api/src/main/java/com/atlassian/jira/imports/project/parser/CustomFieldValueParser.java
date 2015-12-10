package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValue;

import java.util.Map;

/**
 * Converts custom field value xml in a JIRA backup to an object representation and converts the object into the
 * EntityRepresentation.
 *
 * @since v3.13
 */
public interface CustomFieldValueParser
{
    public static final String CUSTOM_FIELD_VALUE_ENTITY_NAME = "CustomFieldValue";

    /**
     * Parses the custom field value data from the backup XML.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an ExternalCustomFieldValue. The following
     * attributes are required, otherwise a ParseException will be thrown:<br/>
     * <ul>
     * <li>id (required)</li>
     * <li>customfield (required)</li>
     * <li>issue (required)</li>
     * </ul>
     * @return an ExternalCustomFieldValue if the attributes contain the required fields
     * @throws com.atlassian.jira.exception.ParseException if the required fields are not found in the attributes map
     */
    ExternalCustomFieldValue parse(Map attributes) throws ParseException;

    /**
     * Gets an EntityRepresentation that contains the correct attributes based on the populated fields in the
     * provided custom field value.
     *
     * @param customFieldValue contains the populated fields that will end up in the EntityRepresentations map
     * @return an EntityRepresentation that can be persisted using OfBiz
     */
    EntityRepresentation getEntityRepresentation(ExternalCustomFieldValue customFieldValue);
}