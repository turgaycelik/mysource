package com.atlassian.jira.imports.project.parser;

import java.util.Map;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalEntityProperty;
import com.atlassian.jira.imports.project.core.EntityRepresentation;

/**
 * Converts entity property xml in a JIRA backup to an object representation and converts the object representation into
 * {@link com.atlassian.jira.imports.project.core.EntityRepresentation}.
 *
 * @since v6.2
 */
public interface EntityPropertyParser
{
    public static final String ENTITY_PROPERTY_ENTITY_NAME = "EntityProperty";

    /**
     * Parses the entity property data from the backup XML.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an ExternalEntityProperty. The
     * following attributes are required, otherwise a ParseException will be thrown:<br/>
     * <ul>
     *     <li>id (required)</li>
     *     <li>entityName (required)</li>
     *     <li>entityId (required)</li>
     *     <li>propertyKey (required)</li>
     *     <li>value(required)</li>
     *     <li>created (required)</li>
     *     <li>updated (required)</li>
     * </ul>
     * @return an ExternalEntityProperty if the attributes contain the required fields
     * @throws com.atlassian.jira.exception.ParseException if the required fields are not found in the attributes map
     */
    ExternalEntityProperty parse(Map<String, String> attributes) throws ParseException;

    /**
     * Gets an EntityRepresentation that contains the correct attributes based on the populated fields in the provided
     * entity property.
     *
     * @param entityProperty contains the populated fields that will end up in the EntityRepresentations map
     * @param newEntityId new id for external entity
     * @return an EntityRepresentation that can be persisted using OfBiz
     */
    EntityRepresentation getEntityRepresentation(ExternalEntityProperty entityProperty, Long newEntityId);
}
