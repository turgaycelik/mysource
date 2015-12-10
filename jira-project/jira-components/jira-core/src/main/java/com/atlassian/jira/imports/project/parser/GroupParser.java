package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalGroup;

import java.util.Map;

/**
 * Converts OSGroup xml in a JIRA backup to an object representation.
 *
 * @since v3.13
 */
public interface GroupParser
{
    public static final String GROUP_ENTITY_NAME = "Group";

    /**
     * Parses the OSGroup data from the backup XML.
     * The name attribute is required, otherwise a ParseException will be thrown.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an OSGroup.
     * @return an ExternalGroup if the attributes contain the required fields.
     * @throws com.atlassian.jira.exception.ParseException
     *          if the required fields are not found in the attributes map.
     */
    ExternalGroup parse(Map attributes) throws ParseException;
}
