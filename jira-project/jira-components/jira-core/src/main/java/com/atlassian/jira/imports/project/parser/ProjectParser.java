package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalProject;

import java.util.Map;

/**
 * Converts project xml in a JIRA backup to an object representation.
 *
 * @since v3.13
 */
public interface ProjectParser
{
    /**
     * Defines the element name that the parser will handle.
     */
    public static final String PROJECT_ENTITY_NAME = "Project";

    /**
     * Transforms a set of attributes into an {@link com.atlassian.jira.external.beans.ExternalProject}.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an ExternalProject. The following
     * attributes are required, otherwise a ParseException will be thrown:<br/>
     * <ul>
     * <li>id (required)</li>
     * <li>key (required)</li>
     * </ul>
     * @return an {@link com.atlassian.jira.external.beans.ExternalProject} the attributes contain id and key, never null.
     *
     * @throws ParseException If the attributes are invalid.
     */
    ExternalProject parseProject(Map attributes) throws ParseException;

    /**
     * This method parses the Project "Email Sender" properties out of OSProperty.
     * The values are remembered and added to the appropriate ExternalProject during the parseProject() phase.
     *
     * @param elementName Element Name for this element.
     * @param attributes Map of key-value pairs for this element.
     */
    void parseOther(final String elementName, final Map attributes);
}
