package com.atlassian.jira.imports.project.parser;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalVersion;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestProjectVersionParserImpl
{
    ProjectVersionParserImpl projectVersionParser = new ProjectVersionParserImpl();

    @Test
    public void testParseNullAttributeMap() throws ParseException
    {
        try
        {
            projectVersionParser.parse(null);
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
            projectVersionParser.parse(EasyMap.build("project", "10000", "name", "version1", "sequence", "1"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingProject()
    {
        try
        {
            projectVersionParser.parse(EasyMap.build("id", "10000", "name", "version1", "sequence", "1"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingName()
    {
        try
        {
            projectVersionParser.parse(EasyMap.build("id", "10000", "project", "10000", "sequence", "1"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingSequence()
    {
        try
        {
            projectVersionParser.parse(EasyMap.build("id", "10000", "project", "10000", "name", "version1"));
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
        final ExternalVersion externalVersion = projectVersionParser.parse(EasyMap.build("id", "10000", "project", "10002", "name", "version1", "sequence", "10004"));
        assertEquals("10000", externalVersion.getId());
        assertEquals("10002", externalVersion.getProjectId());
        assertEquals("version1", externalVersion.getName());
        assertEquals(new Long(10004), externalVersion.getSequence());
    }

    @Test
    public void testParseFullMap() throws ParseException
    {
        final ExternalVersion externalVersion = projectVersionParser.parse(EasyMap.build("id", "10000", "project", "10002", "name", "version1", "sequence", "10004", "description", "desc", "archived", "true", "released", "true"));
        assertEquals("10000", externalVersion.getId());
        assertEquals("10002", externalVersion.getProjectId());
        assertEquals("version1", externalVersion.getName());
        assertEquals(new Long(10004), externalVersion.getSequence());
        assertEquals("desc", externalVersion.getDescription());
        assertTrue(externalVersion.isReleased());
        assertTrue(externalVersion.isArchived());
    }

}
