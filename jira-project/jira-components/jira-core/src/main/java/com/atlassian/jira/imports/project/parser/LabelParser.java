package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalLabel;
import com.atlassian.jira.imports.project.core.EntityRepresentation;

import java.util.Map;

/**
 * Converts label xml in a JIRA backup to an object representation and converts the object representation
 * into {@link com.atlassian.jira.imports.project.core.EntityRepresentation}.
 *
 * @since v4.2
 */
public interface LabelParser
{
    /**
     * Parses the label data from the backup XML.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an ExternalLabel. The following
     * attributes are required, otherwise a ParseException will be thrown:<br/>
     * <ul>
     * <li>id (required)</li>
     * <li>label (required)</li>
     * <li>issueId (required)</li>
     * </ul>
     *
     * @return an ExternalLabel if the attributes contain the required fields
     * @throws com.atlassian.jira.exception.ParseException if the required fields are not found in the attributes map
     */
    ExternalLabel parse(Map attributes) throws ParseException;

    /**
     * Gets an EntityRepresentation that contains the correct attributes based on the populated fields in the
     * provided label.
     *
     * @param label contains the populated fields that will end up in the EntityRepresentations map
     * @return an EntityRepresentation that can be persisted using OfBiz
     */
    EntityRepresentation getEntityRepresentation(ExternalLabel label);
}
