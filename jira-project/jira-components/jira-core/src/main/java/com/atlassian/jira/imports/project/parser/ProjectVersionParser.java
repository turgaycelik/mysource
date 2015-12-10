package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalVersion;

import java.util.Map;

/**
 * Converts project version xml in a JIRA backup to an object representation.
 *
 * @since v3.13
 */
public interface ProjectVersionParser
{
    /** Entity name for Version */
    public static final String VERSION_ENTITY_NAME = "Version";

    /**
     * Transforms a set of attributes into an ExternalVersion.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an ExternalVersion. The following
     * attributes are required, otherwise a ParseException will be thrown:<br/>
     * <ul>
     * <li>id (required)</li>
     * <li>project (required)</li>
     * <li>name (required)</li>
     * <li>sequence (optional)</li>
     * </ul>
     * @return an ExternalVersion the attributes contain id and key.
     *
     * @throws ParseException If the attributes are invalid.
     */
    ExternalVersion parse(Map attributes) throws ParseException;
}
