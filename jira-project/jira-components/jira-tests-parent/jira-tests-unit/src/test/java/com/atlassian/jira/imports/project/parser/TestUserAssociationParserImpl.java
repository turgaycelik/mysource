package com.atlassian.jira.imports.project.parser;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalVoter;
import com.atlassian.jira.external.beans.ExternalWatcher;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestUserAssociationParserImpl
{
    UserAssociationParser userAssociationParser = new UserAssociationParserImpl();

    @Test
    public void testParseNullAttributeMap() throws ParseException
    {
        try
        {
            userAssociationParser.parseVoter(null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // Expected!
        }
        try
        {
            userAssociationParser.parseWatcher(null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // Expected!
        }
    }

    @Test
    public void testParseVoterMissingIssueId()
    {
        try
        {
            final Map attributes = EasyMap.build("sourceName", "admin", "sinkNodeEntity", "Issue", "associationType", "VoteIssue");
            userAssociationParser.parseVoter(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseWatcherMissingIssueId()
    {
        try
        {
            final Map attributes = EasyMap.build("sourceName", "admin", "sinkNodeEntity", "Issue", "associationType", "WatchIssue");
            userAssociationParser.parseWatcher(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingVoter()
    {
        try
        {
            final Map attributes = EasyMap.build("sinkNodeId", "10000", "sinkNodeEntity", "Issue", "associationType", "VoteIssue");
            userAssociationParser.parseVoter(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingWatcher()
    {
        try
        {
            final Map attributes = EasyMap.build("sinkNodeId", "10000", "sinkNodeEntity", "Issue", "associationType", "WatchIssue");
            userAssociationParser.parseWatcher(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseVoter() throws ParseException
    {
        final Map attributes = EasyMap.build("sourceName", "admin", "sinkNodeId", "10000", "sinkNodeEntity", "Issue", "associationType", "VoteIssue");
        ExternalVoter externalVoter = userAssociationParser.parseVoter(attributes);
        assertEquals("10000", externalVoter.getIssueId());
        assertEquals("admin", externalVoter.getVoter());
    }

    @Test
    public void testParseWatcher() throws ParseException
    {
        final Map attributes = EasyMap.build("sourceName", "admin", "sinkNodeId", "10000", "sinkNodeEntity", "Issue", "associationType", "WatchIssue");
        ExternalWatcher externalWatcher = userAssociationParser.parseWatcher(attributes);
        assertEquals("10000", externalWatcher.getIssueId());
        assertEquals("admin", externalWatcher.getWatcher());
    }

    @Test
    public void testParseVoterWithWatcher() throws ParseException
    {
        final Map attributes = EasyMap.build("sourceName", "admin", "sinkNodeId", "10000", "sinkNodeEntity", "Issue", "associationType", "WatchIssue");
        assertNull(userAssociationParser.parseVoter(attributes));
    }

    @Test
    public void testParseWatcherrWithVoter() throws ParseException
    {
        final Map attributes = EasyMap.build("sourceName", "admin", "sinkNodeId", "10000", "sinkNodeEntity", "Issue", "associationType", "VoteIssue");
        assertNull(userAssociationParser.parseWatcher(attributes));
    }
}
