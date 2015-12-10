package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalIssue;
import com.atlassian.jira.imports.project.core.EntityRepresentation;

import java.util.Map;

/**
 * Converts issue xml in a JIRA backup to an object representation and converts the object representation
 * into {@link EntityRepresentation}.
 *
 * @since v3.13
 */
public interface IssueParser
{
    public static final String ISSUE_ENTITY_NAME = "Issue";

    /**
     * Parses the issue data from the backup XML.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an ExternalIssue. The following
     * attributes are required, otherwise a ParseException will be thrown:<br/>
     * <ul>
     * <li>id (required)</li>
     * <li>key (required)</li>
     * <li>type (required)</li>
     * <li>status (required)</li>
     * <li>summary (required)</li>
     * <li>project (required)</li>
     * </ul>
     * @return an ExternalIssue if the attributes contain the required fields
     * @throws ParseException if the required fields are not found in the attributes map
     */
    ExternalIssue parse(Map attributes) throws ParseException;

}