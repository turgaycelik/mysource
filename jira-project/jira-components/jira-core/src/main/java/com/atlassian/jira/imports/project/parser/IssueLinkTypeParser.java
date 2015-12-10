package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalIssueLinkType;

import java.util.Map;

/**
 * Converts IssueLinkType xml in a JIRA backup to an object representation.
 *
 * @since v3.13
 */
public interface IssueLinkTypeParser
{
    public static final String ISSUE_LINK_TYPE_ENTITY_NAME = "IssueLinkType";

    /**
     * Parses the IssueLinkType data from the backup XML.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an IssueLinkType.
     * @return an ExternalIssueLinkType if the attributes contain the required fields
     * @throws com.atlassian.jira.exception.ParseException
     *          if the required fields are not found in the attributes map
     */
    ExternalIssueLinkType parse(Map attributes) throws ParseException;
}