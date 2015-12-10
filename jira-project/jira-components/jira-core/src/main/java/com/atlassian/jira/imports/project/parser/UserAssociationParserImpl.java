package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalVoter;
import com.atlassian.jira.external.beans.ExternalWatcher;
import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * @since v3.13
 */
public class UserAssociationParserImpl implements UserAssociationParser
{
    private static final String ASSOCIATION_TYPE = "associationType";
    private static final String SINK_NODE_ENTITY = "sinkNodeEntity";
    private static final String SINK_NODE_ID = "sinkNodeId";
    private static final String SOURCE_NAME = "sourceName";

    public ExternalVoter parseVoter(final Map attributes) throws ParseException
    {
        Null.not("attributes", attributes);
        // <UserAssociation sourceName="admin" sinkNodeId="10000" sinkNodeEntity="Issue" associationType="VoteIssue"/>
        final String sinkNodeEntity = (String) attributes.get(SINK_NODE_ENTITY);
        final String associationType = (String) attributes.get(ASSOCIATION_TYPE);

        // We only handle voters if the type is correct and the association is for an issue
        if (!IssueParser.ISSUE_ENTITY_NAME.equals(sinkNodeEntity) || !ASSOCIATION_TYPE_VOTE_ISSUE.equals(associationType))
        {
            return null;
        }

        final String issueId = (String) attributes.get(SINK_NODE_ID);
        final String voter = (String) attributes.get(SOURCE_NAME);
        if (StringUtils.isBlank(issueId))
        {
            throw new ParseException("A voter must have an associated issue id.");
        }
        if (StringUtils.isBlank(voter))
        {
            throw new ParseException("Voter associated with issue id '" + issueId + "' has no user associated with the vote.");
        }
        final ExternalVoter externalVoter = new ExternalVoter();
        externalVoter.setIssueId(issueId);
        externalVoter.setVoter(voter);
        return externalVoter;
    }

    public ExternalWatcher parseWatcher(final Map attributes) throws ParseException
    {
        Null.not("attributes", attributes);
        // <UserAssociation sourceName="admin" sinkNodeId="10000" sinkNodeEntity="Issue" associationType="WatchIssue"/>
        final String sinkNodeEntity = (String) attributes.get(SINK_NODE_ENTITY);
        final String associationType = (String) attributes.get(ASSOCIATION_TYPE);

        // We only handle watchers if the type is correct and the association is for an issue
        if (!IssueParser.ISSUE_ENTITY_NAME.equals(sinkNodeEntity) || !ASSOCIATION_TYPE_WATCH_ISSUE.equals(associationType))
        {
            return null;
        }

        final String issueId = (String) attributes.get(SINK_NODE_ID);
        final String watcher = (String) attributes.get(SOURCE_NAME);
        if (StringUtils.isBlank(issueId))
        {
            throw new ParseException("A watcher must have an associated issue id.");
        }
        if (StringUtils.isBlank(watcher))
        {
            throw new ParseException("Watcher associated with issue id '" + issueId + "' has no user associated with the watch.");
        }
        final ExternalWatcher externalWatcher = new ExternalWatcher();
        externalWatcher.setIssueId(issueId);
        externalWatcher.setWatcher(watcher);
        return externalWatcher;
    }
}
