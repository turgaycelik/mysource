package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalVoter;
import com.atlassian.jira.external.beans.ExternalWatcher;

import java.util.Map;

/**
 * Converts voter and watcher xml in a JIRA backup to an object representation.
 *
 * @since v3.13
 */
public interface UserAssociationParser
{
    public static final String USER_ASSOCIATION_ENTITY_NAME = "UserAssociation";
    public static final String ASSOCIATION_TYPE_VOTE_ISSUE = "VoteIssue";
    public static final String ASSOCIATION_TYPE_WATCH_ISSUE = "WatchIssue";

    /**
     * Parses the voter data from the backup XML.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an ExternalVoter.
     * The parser will only create an ExternalVoter if the sinkNodeEntity attribute is provided an is 'Issue' AND
     * the associationType attribute is provided and is 'VoteIssue'.
     * The following attributes are required, otherwise a ParseException will be thrown:<br/>
     * <ul>
     * <li>sinkNodeId (represents issue id, required)</li>
     * <li>sourceName (represents voter, required)</li>
     * </ul>
     * @return an ExternalVoter if the attributes contain the required fields, null otherwise
     * @throws com.atlassian.jira.exception.ParseException if the required fields are not found in the attributes map
     */
    ExternalVoter parseVoter(Map attributes) throws ParseException;

    /**
     * Parses the watcher data from the backup XML.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an ExternalWatcher.
     * The parser will only create an ExternalWatcher if the sinkNodeEntity attribute is provided an is 'Issue' AND
     * the associationType attribute is provided and is 'WatchIssue'.
     * The following attributes are required, otherwise a ParseException will be thrown:<br/>
     * <ul>
     * <li>sinkNodeId (represents issue id, required)</li>
     * <li>sourceName (represents watcher, required)</li>
     * </ul>
     * @return an ExternalWatcher if the attributes contain the required fields, null otherwise
     * @throws com.atlassian.jira.exception.ParseException if the required fields are not found in the attributes map
     */
    ExternalWatcher parseWatcher(Map attributes) throws ParseException;
}
