package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalComponent;

import java.util.Map;

/**
 * Converts project component xml in a JIRA backup to an object representation.
 *
 * @since v3.13
 */
public interface ProjectComponentParser
{
    /** Name of the Component Entity */
    public static final String COMPONENT_ENTITY_NAME = "Component";

    /**
     * Transforms a set of attributes into an ExternalComponent.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an ExternalComponent. The following
     * attributes are required, otherwise a ParseException will be thrown:<br/>
     * <ul>
     * <li>id (required)</li>
     * <li>project (required)</li>
     * <li>name (required)</li>
     * </ul>
     * @return an ExternalComponent the attributes contain id and key.
     *
     * @throws ParseException If the attributes are invalid.
     */
    ExternalComponent parse(Map attributes) throws ParseException;
}
