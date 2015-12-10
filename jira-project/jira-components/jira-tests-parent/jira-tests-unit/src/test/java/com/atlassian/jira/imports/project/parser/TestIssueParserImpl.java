package com.atlassian.jira.imports.project.parser;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalIssue;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestIssueParserImpl
{
    IssueParserImpl issueParser = new IssueParserImpl();

    @Test
    public void testParseNullAttributeMap() throws ParseException
    {
        try
        {
            issueParser.parse(null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // Expected!
        }
    }

    @Test
    public void testParseMissingId()
    {
        try
        {
            issueParser.parse(EasyMap.build("key", "HSP-2", "type", "1", "status", "2", "summary", "I am a summary", "project", "10001"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseBadId() throws ParseException
    {
        try
        {
            issueParser.parse(EasyMap.build("id", "10000-string", "key", "HSP-2", "type", "1", "status", "2", "summary", "I am a summary", "project", "10001"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingKey()
    {
        try
        {
            issueParser.parse(EasyMap.build("id", "10000", "type", "1", "status", "2", "summary", "I am a summary", "project", "10001"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingIssueType()
    {
        try
        {
            issueParser.parse(EasyMap.build("id", "10000", "key", "HSP-2", "status", "2", "summary", "I am a summary", "project", "10001"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingStatus()
    {
        try
        {
            issueParser.parse(EasyMap.build("id", "10000", "key", "HSP-2", "type", "1", "summary", "I am a summary", "project", "10001"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingSummary()
    {
        try
        {
            issueParser.parse(EasyMap.build("id", "10000", "key", "HSP-2", "type", "1", "status", "2", "project", "10001"));
        }
        catch (ParseException e)
        {
            fail("UnExpected ParseException");
        }
    }

    @Test
    public void testParseMissingProject()
    {
        try
        {
            issueParser.parse(EasyMap.build("id", "10000", "key", "HSP-2", "type", "1", "status", "2", "summary", "I am a summary"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMinimal() throws ParseException
    {
        ExternalIssue issue = issueParser.parse(EasyMap.build("id", "10000", "key", "HSP-2", "type", "1", "status", "2", "summary", "I am a summary", "project", "10001"));
        assertEquals("10000", issue.getId());
        assertEquals("HSP-2", issue.getKey());
        assertEquals("1", issue.getIssueType());
        assertEquals("2", issue.getStatus());
        assertEquals("I am a summary", issue.getSummary());
        assertEquals("10001", issue.getProject());
    }

    @Test
    public void testParseFull() throws ParseException
    {
        final Map attributes = EasyMap.build("id", "10000", "key", "HSP-2", "type", "1", "status", "2", "summary", "I am a summary", "project", "10001");
        attributes.put("resolution", "3");
        attributes.put("reporter", "admin");
        attributes.put("assignee", "dylan");
        attributes.put("description", "desc");
        attributes.put("environment", "env");
        attributes.put("priority", "3");
        attributes.put("created", "2008-01-07 16:24:54");
        attributes.put("updated", "2008-01-07 16:24:55");
        attributes.put("duedate", "2008-01-07 16:24:56");
        attributes.put("resolutiondate", "2008-01-07 16:24:57");
        attributes.put("votes", "2");
        attributes.put("timespent", "100");
        attributes.put("timeoriginalestimate", "50");
        attributes.put("timeestimate", "75");
        attributes.put("security", "12345");

        ExternalIssue issue = issueParser.parse(attributes);

        assertEquals("10000", issue.getId());
        assertEquals("HSP-2", issue.getKey());
        assertEquals("1", issue.getIssueType());
        assertEquals("2", issue.getStatus());
        assertEquals("I am a summary", issue.getSummary());
        assertEquals("10001", issue.getProject());
        assertEquals("3", issue.getResolution());
        assertEquals("admin", issue.getReporter());
        assertEquals("dylan", issue.getAssignee());
        assertEquals("desc", issue.getDescription());
        assertEquals("env", issue.getEnvironment());
        assertEquals("3", issue.getPriority());
        assertEquals("2008-01-07 16:24:54.0", issue.getCreated().toString());
        assertEquals("2008-01-07 16:24:55.0", issue.getUpdated().toString());
        assertEquals("2008-01-07 16:24:56.0", issue.getDuedate().toString());
        assertEquals("2008-01-07 16:24:57.0", issue.getResolutionDate().toString());
        assertEquals(new Long("2"), issue.getVotes());
        assertEquals(new Long("100"), issue.getTimeSpent());
        assertEquals(new Long("50"), issue.getOriginalEstimate());
        assertEquals(new Long("75"), issue.getEstimate());
        assertEquals("12345", issue.getSecurityLevel());
    }
}
