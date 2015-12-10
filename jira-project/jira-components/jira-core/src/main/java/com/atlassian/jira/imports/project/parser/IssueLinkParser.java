package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalLink;
import com.atlassian.jira.imports.project.core.EntityRepresentation;

import java.util.Map;

/**
 * Converts IssueLink xml in a JIRA backup to an object representation and converts the object representation
 * into {@link com.atlassian.jira.imports.project.core.EntityRepresentation}.
 *
 * @since v3.13
 */
public interface IssueLinkParser
{
    public static final String ISSUE_LINK_ENTITY_NAME = "IssueLink";

    /**
     * Parses the IssueLink data from the backup XML.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an IssueLink.
     * @return an ExternalLink if the attributes contain the required fields.
     * @throws com.atlassian.jira.exception.ParseException if the required fields are not found in the attributes map.
     */
    ExternalLink parse(Map attributes) throws ParseException;

    /**
     * Gets an EntityRepresentation that contains the correct attributes based on the populated fields in the
     * provided IssueLink.
     *
     * @param issueLink contains the populated fields that will end up in the EntityRepresentations map.
     * @return an EntityRepresentation that can be persisted using OfBiz.
     */
    EntityRepresentation getEntityRepresentation(ExternalLink issueLink);
}