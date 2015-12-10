package com.atlassian.jira.imports.project.parser;

import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalChangeGroup;
import com.atlassian.jira.imports.project.core.EntityRepresentation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestChangeGroupParserImpl
{
    ChangeGroupParserImpl changeGroupParser = new ChangeGroupParserImpl();

    @Test
    public void testParseNullAttributeMap() throws ParseException
    {
        try
        {
            changeGroupParser.parse(null);
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
            final Map attributes = EasyMap.build("issue", "10000", "author", "fred", "created", "2008-01-11 10:07:21.68");
            changeGroupParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingIssueId()
    {
        try
        {
            final Map attributes = EasyMap.build("id", "10000", "author", "fred", "created", "2008-01-11 10:07:21.68");
            changeGroupParser.parse(attributes);
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
        final Map attributes = EasyMap.build("id", "10001", "issue", "10000");
        ExternalChangeGroup changeGroup = changeGroupParser.parse(attributes);
        assertEquals("10001", changeGroup.getId());
        assertEquals("10000", changeGroup.getIssueId());
    }

    @Test
    public void testParseFull() throws ParseException
    {
        final Map attributes = EasyMap.build("id", "10001", "issue", "10002", "author", "fred", "created", "2008-01-11 10:07:21");

        ExternalChangeGroup changeGroup = changeGroupParser.parse(attributes);
        assertEquals("10001", changeGroup.getId());
        assertEquals("10002", changeGroup.getIssueId());
        assertEquals("fred", changeGroup.getAuthor());
        assertEquals("2008-01-11 10:07:21.0", changeGroup.getCreated().toString());
    }

    @Test
    public void testGetEntityRepresentation()
    {
        ExternalChangeGroup changeGroup = new ExternalChangeGroup();
        changeGroup.setId("10000");
        changeGroup.setIssueId("10001");
        changeGroup.setAuthor("fred");

        Date created = new Date(3600000);
        changeGroup.setCreated(created);

        // Set the Timezone so we know what we expect the Date representation to look like.
        TimeZone oldDefault = TimeZone.getDefault();
        try
        {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            final EntityRepresentation representation = changeGroupParser.getEntityRepresentation(changeGroup);
            assertNotNull(representation);
            assertEquals("10000", representation.getEntityValues().get("id"));
            assertEquals("10001", representation.getEntityValues().get("issue"));
            assertEquals("fred", representation.getEntityValues().get("author"));
            assertEquals("1970-01-01 01:00:00.0", representation.getEntityValues().get("created"));
        }
        finally
        {
            // reset the Timezone in case we upset other tests.
            TimeZone.setDefault(oldDefault);
        }
    }

    @Test
    public void testGetEntityRepresentationNullCreatedDate()
    {
        ExternalChangeGroup changeGroup = new ExternalChangeGroup();
        changeGroup.setId("10000");
        changeGroup.setIssueId("10001");
        changeGroup.setAuthor("fred");
        changeGroup.setCreated(null);

        final EntityRepresentation representation = changeGroupParser.getEntityRepresentation(changeGroup);
        assertNotNull(representation);
        assertEquals("10000", representation.getEntityValues().get("id"));
        assertEquals("10001", representation.getEntityValues().get("issue"));
        assertEquals("fred", representation.getEntityValues().get("author"));
        assertEquals(null, representation.getEntityValues().get("created"));
    }
}
