package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalChangeItem;
import com.atlassian.jira.imports.project.core.EntityRepresentation;

import java.util.Map;

/**
 * Converts change item xml in a JIRA backup to an object representation and converts the object representation
 * into {@link com.atlassian.jira.imports.project.core.EntityRepresentation}.
 *
 * @since v3.13
 */
public interface ChangeItemParser
{
    static final String CHANGE_ITEM_ENTITY_NAME = "ChangeItem";

    /**
     * Parses the ChangeItem data from the backup XML.
     *
     * @param attributes is a map of key value pairs that represent the attributes of a ChangeItem. The following
     *                   attributes are required, otherwise a ParseException will be thrown:
     *                   <ul>
     *                   <li>id (required)</li>
     *                   <li>group (required)</li>
     *                   <li>fieldtype (required)</li>
     *                   <li>field (required)</li>
     *                   </ul>
     * @return an ExternalChangeItem if the attributes contain the required fields
     * @throws com.atlassian.jira.exception.ParseException
     *          if the required fields are not found in the attributes map
     */
    ExternalChangeItem parse(Map attributes) throws ParseException;

    /**
     * Gets an EntityRepresentation that contains the correct attributes based on the populated fields in the
     * provided ExternalChangeItem.
     *
     * @param changeItem contains the populated fields that will end up in the EntityRepresentations map
     * @return an EntityRepresentation that can be persisted using OfBiz
     */
    EntityRepresentation getEntityRepresentation(ExternalChangeItem changeItem);
}
