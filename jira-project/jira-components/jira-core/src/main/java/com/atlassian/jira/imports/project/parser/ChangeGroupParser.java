package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalChangeGroup;
import com.atlassian.jira.imports.project.core.EntityRepresentation;

import java.util.Map;

/**
 * Converts change group xml in a JIRA backup to an object representation and converts the object representation
 * into {@link com.atlassian.jira.imports.project.core.EntityRepresentation}.
 *
 * @since v3.13
 */
public interface ChangeGroupParser
{
    public static final String CHANGE_GROUP_ENTITY_NAME = "ChangeGroup";

    /**
     * Parses the change group data from the backup XML.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an ExternalChangeGroup. The following
     * attributes are required, otherwise a ParseException will be thrown:<br/>
     * <ul>
     * <li>id (required)</li>
     * <li>issue (required)</li>
     * </ul>
     *
     * @return an ExternalChangeGroup if the attributes contain the required fields
     * @throws com.atlassian.jira.exception.ParseException if the required fields are not found in the attributes map
     */
    ExternalChangeGroup parse(Map attributes) throws ParseException;

    /**
     * Gets an EntityRepresentation that contains the correct attributes based on the populated fields in the
     * provided change group.
     *
     * @param changeGroup contains the populated fields that will end up in the EntityRepresentations map
     * @return an EntityRepresentation that can be persisted using OfBiz
     */
    EntityRepresentation getEntityRepresentation(ExternalChangeGroup changeGroup);
}
