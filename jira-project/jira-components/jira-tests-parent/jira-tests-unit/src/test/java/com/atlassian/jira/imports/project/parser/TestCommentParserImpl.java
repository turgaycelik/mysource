package com.atlassian.jira.imports.project.parser;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalComment;
import com.atlassian.jira.imports.project.core.EntityRepresentation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestCommentParserImpl
{
    CommentParserImpl commentParser = new CommentParserImpl();

    @Test
    public void testParseNullAttributeMap() throws ParseException
    {
        try
        {
            commentParser.parse(null);
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
            commentParser.parse(attributes);
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
            commentParser.parse(attributes);
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
            commentParser.parse(attributes);
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
        ExternalComment comment = commentParser.parse(attributes);
        assertEquals("10001", comment.getId());
        assertEquals("10000", comment.getIssueId());
        assertEquals("Comment body", comment.getBody());
        assertEquals("fred", comment.getUsername());
    }

    @Test
    public void testParseFull() throws ParseException
    {
        final Map attributes = EasyMap.build("id", "10001", "issue", "10000", "body", "Comment body", "type", "comment", "author", "fred", "updateauthor", "barney", "rolelevel", "12345");
        attributes.put("updated", "2008-02-27 17:29:18");
        attributes.put("created", "2008-01-11 10:07:21");

        ExternalComment comment = commentParser.parse(attributes);
        assertEquals("10001", comment.getId());
        assertEquals("10000", comment.getIssueId());
        assertEquals("Comment body", comment.getBody());
        assertEquals("fred", comment.getUsername());
        assertEquals("barney", comment.getUpdateAuthor());
        assertEquals("12345", comment.getRoleLevelId().toString());
        assertEquals(new Date(Timestamp.valueOf("2008-02-27 17:29:18.0").getTime()), comment.getUpdated());
        assertEquals(new Date(Timestamp.valueOf("2008-01-11 10:07:21.0").getTime()), comment.getTimePerformed());
    }

    @Test
    public void testParseNonCommentActionEntity() throws ParseException
    {
        final Map attributes = EasyMap.build("id", "10001", "issue", "10000", "body", "Comment body", "type", "notcomment", "author", "fred", "updateauthor", "barney", "rolelevel", "12345");
        attributes.put("updated", "2008-02-27 17:29:18");
        attributes.put("created", "2008-01-11 10:07:21");

        assertNull(commentParser.parse(attributes));
    }

    @Test
    public void testGetEntityRepresentation()
    {
        ExternalComment comment = new ExternalComment("Test body");
        comment.setId("10000");
        comment.setIssueId("10001");
        comment.setGroupLevel("agroup");
        comment.setRoleLevelId(new Long(10002));
        comment.setUsername("fred");
        comment.setUpdateAuthor("barney");
        Date now = new Date();
        Date then = new Date(System.currentTimeMillis() + 10000);
        comment.setTimePerformed(now);
        comment.setUpdated(then);

        final EntityRepresentation representation = commentParser.getEntityRepresentation(comment);
        assertNotNull(representation);
        assertEquals("10000", representation.getEntityValues().get("id"));
        assertEquals("10001", representation.getEntityValues().get("issue"));
        assertEquals("agroup", representation.getEntityValues().get("level"));
        assertEquals("10002", representation.getEntityValues().get("rolelevel"));
        assertEquals("fred", representation.getEntityValues().get("author"));
        assertEquals("barney", representation.getEntityValues().get("updateauthor"));
        assertEquals(new Timestamp(now.getTime()).toString(), representation.getEntityValues().get("created"));
        assertEquals(new Timestamp(then.getTime()).toString(), representation.getEntityValues().get("updated"));

    }

    @Test
    public void testGetEntityRepresentationNoRoleLevel()
    {
        ExternalComment comment = new ExternalComment("Test body");
        comment.setId("10000");
        comment.setIssueId("10001");
        comment.setGroupLevel("agroup");
        comment.setUsername("fred");
        comment.setUpdateAuthor("barney");
        Date now = new Date();
        Date then = new Date(System.currentTimeMillis() + 10000);
        comment.setTimePerformed(now);
        comment.setUpdated(then);

        final EntityRepresentation representation = commentParser.getEntityRepresentation(comment);
        assertNotNull(representation);
        assertEquals("10000", representation.getEntityValues().get("id"));
        assertEquals("10001", representation.getEntityValues().get("issue"));
        assertEquals("agroup", representation.getEntityValues().get("level"));
        assertNull(representation.getEntityValues().get("rolelevel"));
        assertEquals("fred", representation.getEntityValues().get("author"));
        assertEquals("barney", representation.getEntityValues().get("updateauthor"));
        assertEquals(new Timestamp(now.getTime()).toString(), representation.getEntityValues().get("created"));
        assertEquals(new Timestamp(then.getTime()).toString(), representation.getEntityValues().get("updated"));

    }
    
    @Test
    public void testGetEntityRepresentationNoGroupLevel()
    {
        ExternalComment comment = new ExternalComment("Test body");
        comment.setId("10000");
        comment.setIssueId("10001");
        comment.setRoleLevelId(new Long(10002));
        comment.setUsername("fred");
        comment.setUpdateAuthor("barney");
        Date now = new Date();
        Date then = new Date(System.currentTimeMillis() + 10000);
        comment.setTimePerformed(now);
        comment.setUpdated(then);

        final EntityRepresentation representation = commentParser.getEntityRepresentation(comment);
        assertNotNull(representation);
        assertEquals("10000", representation.getEntityValues().get("id"));
        assertEquals("10001", representation.getEntityValues().get("issue"));
        assertNull(representation.getEntityValues().get("level"));
        assertEquals("10002", representation.getEntityValues().get("rolelevel"));
        assertEquals("fred", representation.getEntityValues().get("author"));
        assertEquals("barney", representation.getEntityValues().get("updateauthor"));
        assertEquals(new Timestamp(now.getTime()).toString(), representation.getEntityValues().get("created"));
        assertEquals(new Timestamp(then.getTime()).toString(), representation.getEntityValues().get("updated"));

    }
}
