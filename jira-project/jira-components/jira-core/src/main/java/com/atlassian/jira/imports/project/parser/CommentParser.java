package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalComment;
import com.atlassian.jira.imports.project.core.EntityRepresentation;

import java.util.Map;

/**
 * Converts comment xml in a JIRA backup to an object representation and converts the object representation
 * into {@link com.atlassian.jira.imports.project.core.EntityRepresentation}.
 *
 * @since v3.13
 */
public interface CommentParser
{
    public static final String COMMENT_ENTITY_NAME = "Action";

    /**
     * Parses the comment data from the backup XML.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an ExternalComment. The following
     * attributes are required, otherwise a ParseException will be thrown:<br/>
     * <ul>
     * <li>id (required)</li>
     * <li>issue (required)</li>
     * </ul>
     *
     * @return an ExternalComment if the attributes contain the required fields
     * @throws com.atlassian.jira.exception.ParseException if the required fields are not found in the attributes map
     */
    ExternalComment parse(Map attributes) throws ParseException;

    /**
     * Gets an EntityRepresentation that contains the correct attributes based on the populated fields in the
     * provided comment.
     *
     * @param comment contains the populated fields that will end up in the EntityRepresentations map
     * @return an EntityRepresentation that can be persisted using OfBiz
     */
    EntityRepresentation getEntityRepresentation(ExternalComment comment);
}
