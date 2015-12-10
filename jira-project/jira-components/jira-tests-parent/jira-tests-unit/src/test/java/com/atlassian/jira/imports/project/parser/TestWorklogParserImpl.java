package com.atlassian.jira.imports.project.parser;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalWorklog;
import com.atlassian.jira.imports.project.core.EntityRepresentation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestWorklogParserImpl
{
    WorklogParserImpl worklogParser = new WorklogParserImpl();

    @Test
    public void testParseNullAttributeMap() throws ParseException
    {
        try
        {
            worklogParser.parse(null);
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
            // author="admin" type="comment" rolelevel="10000" created="2008-01-11 10:07:21.68" updateauthor="fred" updated="2008-02-27 17:29:18.408"
            final Map attributes = EasyMap.build("issue", "10000", "body", "Comment body", "type", "comment", "author", "fred", "updateauthor", "barney", "rolelevel", "12345");
            attributes.put("updated", "2008-02-27 17:29:18.408");
            attributes.put("created", "2008-01-11 10:07:21.68");
            attributes.put("timeworked", "30");
            worklogParser.parse(attributes);
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
            final Map attributes = EasyMap.build("id", "10001", "body", "Comment body", "type", "comment", "author", "fred", "updateauthor", "barney", "rolelevel", "12345");
            attributes.put("updated", "2008-02-27 17:29:18.408");
            attributes.put("created", "2008-01-11 10:07:21.68");
            attributes.put("timeworked", "30");
            worklogParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseBadRoleId()
    {
        try
        {
            final Map attributes = EasyMap.build("id", "10001", "issue", "10000", "body", "Comment body", "type", "comment", "author", "fred", "updateauthor", "barney", "rolelevel", "abc");
            attributes.put("updated", "2008-02-27 17:29:18.408");
            attributes.put("created", "2008-01-11 10:07:21.68");
            attributes.put("timeworked", "30");
            worklogParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseBadTimeSpent()
    {
        try
        {
            final Map attributes = EasyMap.build("id", "10001", "issue", "10000", "body", "Comment body", "type", "comment", "author", "fred", "updateauthor", "barney", "rolelevel", "12345");
            attributes.put("updated", "2008-02-27 17:29:18.408");
            attributes.put("created", "2008-01-11 10:07:21.68");
            attributes.put("timeworked", "abc");
            worklogParser.parse(attributes);
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
        final Map attributes = EasyMap.build("id", "10001", "issue", "10000", "body", "Comment body", "type", "comment", "author", "fred");
        ExternalWorklog worklog = worklogParser.parse(attributes);
        assertEquals("10001", worklog.getId());
        assertEquals("10000", worklog.getIssueId());
        assertEquals("Comment body", worklog.getComment());
        assertEquals("fred", worklog.getAuthor());
    }

    @Test
    public void testParseFull() throws ParseException
    {
        final Map attributes = EasyMap.build("id", "10001", "issue", "10000", "body", "Comment body", "type", "comment", "author", "fred", "updateauthor", "barney", "rolelevel", "12345");
        attributes.put("updated", "2008-02-27 17:29:18");
        attributes.put("created", "2008-01-11 10:07:21");
        attributes.put("startdate", "2008-01-11 10:07:33");
        attributes.put("timeworked", "12");

        ExternalWorklog worklog = worklogParser.parse(attributes);
        assertEquals("10001", worklog.getId());
        assertEquals("10000", worklog.getIssueId());
        assertEquals("Comment body", worklog.getComment());
        assertEquals("fred", worklog.getAuthor());
        assertEquals("barney", worklog.getUpdateAuthor());
        assertEquals("12345", worklog.getRoleLevelId().toString());
        assertEquals("12", worklog.getTimeSpent().toString());
        assertDateEquals(2008, 1, 27, 17, 29, 18, worklog.getUpdated());
        assertDateEquals(2008, 0, 11, 10, 7, 21, worklog.getCreated());
        assertDateEquals(2008, 0, 11, 10, 7, 33, worklog.getStartDate());
    }

    private void assertDateEquals(int year, int month, int day, int hour, int minute, int second, Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(year, month, day, hour, minute, second);
        assertEquals(calendar.getTime(), date);

    }

    @Test
    public void testGetEntityRepresentation()
    {
        ExternalWorklog worklog = new ExternalWorklog();
        worklog.setComment("Test body");
        worklog.setId("10000");
        worklog.setIssueId("10001");
        worklog.setGroupLevel("agroup");
        worklog.setRoleLevelId(new Long(10002));
        worklog.setAuthor("fred");
        worklog.setUpdateAuthor("barney");
        Date now = new Date();
        Date then = new Date(System.currentTimeMillis() + 10000);
        Date start = new Date(System.currentTimeMillis() + 100000);
        worklog.setCreated(now);
        worklog.setUpdated(then);
        worklog.setStartDate(start);

        final EntityRepresentation representation = worklogParser.getEntityRepresentation(worklog);
        assertNotNull(representation);
        assertEquals("10000", representation.getEntityValues().get("id"));
        assertEquals("10001", representation.getEntityValues().get("issue"));
        assertEquals("agroup", representation.getEntityValues().get("grouplevel"));
        assertEquals("10002", representation.getEntityValues().get("rolelevel"));
        assertEquals("fred", representation.getEntityValues().get("author"));
        assertEquals("barney", representation.getEntityValues().get("updateauthor"));
        assertEquals(new Timestamp(now.getTime()).toString(), representation.getEntityValues().get("created"));
        assertEquals(new Timestamp(then.getTime()).toString(), representation.getEntityValues().get("updated"));
        assertEquals(new Timestamp(start.getTime()).toString(), representation.getEntityValues().get("startdate"));

    }

    @Test
    public void testGetEntityRepresentationNoRoleLevel()
    {
        ExternalWorklog worklog = new ExternalWorklog();
        worklog.setComment("Test body");
        worklog.setId("10000");
        worklog.setIssueId("10001");
        worklog.setGroupLevel("agroup");
        worklog.setAuthor("fred");
        worklog.setUpdateAuthor("barney");
        Date now = new Date();
        Date then = new Date(System.currentTimeMillis() + 10000);
        Date start = new Date(System.currentTimeMillis() + 100000);
        worklog.setCreated(now);
        worklog.setUpdated(then);
        worklog.setStartDate(start);

        final EntityRepresentation representation = worklogParser.getEntityRepresentation(worklog);
        assertNotNull(representation);
        assertEquals("10000", representation.getEntityValues().get("id"));
        assertEquals("10001", representation.getEntityValues().get("issue"));
        assertEquals("agroup", representation.getEntityValues().get("grouplevel"));
        assertNull(representation.getEntityValues().get("rolelevel"));
        assertEquals("fred", representation.getEntityValues().get("author"));
        assertEquals("barney", representation.getEntityValues().get("updateauthor"));
        assertEquals(new Timestamp(now.getTime()).toString(), representation.getEntityValues().get("created"));
        assertEquals(new Timestamp(then.getTime()).toString(), representation.getEntityValues().get("updated"));
        assertEquals(new Timestamp(start.getTime()).toString(), representation.getEntityValues().get("startdate"));

    }

    @Test
    public void testGetEntityRepresentationNoGroupLevel()
    {
        ExternalWorklog worklog = new ExternalWorklog();
        worklog.setComment("Test body");
        worklog.setId("10000");
        worklog.setIssueId("10001");
        worklog.setRoleLevelId(new Long(10002));
        worklog.setAuthor("fred");
        worklog.setUpdateAuthor("barney");
        worklog.setTimeSpent(new Long(30));
        Date now = new Date();
        Date then = new Date(System.currentTimeMillis() + 10000);
        Date start = new Date(System.currentTimeMillis() + 100000);
        worklog.setCreated(now);
        worklog.setUpdated(then);
        worklog.setStartDate(start);

        final EntityRepresentation representation = worklogParser.getEntityRepresentation(worklog);
        assertNotNull(representation);
        assertEquals("10000", representation.getEntityValues().get("id"));
        assertEquals("10001", representation.getEntityValues().get("issue"));
        assertNull(representation.getEntityValues().get("grouplevel"));
        assertEquals("10002", representation.getEntityValues().get("rolelevel"));
        assertEquals("fred", representation.getEntityValues().get("author"));
        assertEquals("barney", representation.getEntityValues().get("updateauthor"));
        assertEquals("30", representation.getEntityValues().get("timeworked"));
        assertEquals(new Timestamp(now.getTime()).toString(), representation.getEntityValues().get("created"));
        assertEquals(new Timestamp(then.getTime()).toString(), representation.getEntityValues().get("updated"));
        assertEquals(new Timestamp(start.getTime()).toString(), representation.getEntityValues().get("startdate"));

    }
    
    @Test
    public void testGetEntityRepresentationNoTimeSpent()
    {
        ExternalWorklog worklog = new ExternalWorklog();
        worklog.setComment("Test body");
        worklog.setId("10000");
        worklog.setIssueId("10001");
        worklog.setRoleLevelId(new Long(10002));
        worklog.setAuthor("fred");
        worklog.setUpdateAuthor("barney");
        Date now = new Date();
        Date then = new Date(System.currentTimeMillis() + 10000);
        Date start = new Date(System.currentTimeMillis() + 100000);
        worklog.setCreated(now);
        worklog.setUpdated(then);
        worklog.setStartDate(start);

        final EntityRepresentation representation = worklogParser.getEntityRepresentation(worklog);
        assertNotNull(representation);
        assertEquals("10000", representation.getEntityValues().get("id"));
        assertEquals("10001", representation.getEntityValues().get("issue"));
        assertNull(representation.getEntityValues().get("grouplevel"));
        assertEquals("10002", representation.getEntityValues().get("rolelevel"));
        assertNull(representation.getEntityValues().get("timeworked"));
        assertEquals("fred", representation.getEntityValues().get("author"));
        assertEquals("barney", representation.getEntityValues().get("updateauthor"));
        assertEquals(new Timestamp(now.getTime()).toString(), representation.getEntityValues().get("created"));
        assertEquals(new Timestamp(then.getTime()).toString(), representation.getEntityValues().get("updated"));
        assertEquals(new Timestamp(start.getTime()).toString(), representation.getEntityValues().get("startdate"));

    }

}
