package com.atlassian.jira.imports.project.parser;

import java.util.HashMap;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalAttachment;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestAttachmentParserImpl
{
    AttachmentParserImpl attachmentParser;

    @Before
    public void setUp()
    {
        attachmentParser = new AttachmentParserImpl(null, "/");
    }

    @Test
    public void testParseIllegalArgumentException() throws ParseException
    {
        try
        {
            attachmentParser.parse(null);
            fail("Should throw IllegalArgumentException.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected.
        }
    }

    @Test
    public void testParseParseException()
    {
        try
        {
            attachmentParser.parse(new HashMap());
            fail("Should throw ParseException.");
        }
        catch (ParseException e)
        {
            // Expected.
        }
    }

    @Test
    public void testParseParseExceptionMissingId()
    {
        try
        {
            attachmentParser.parse(EasyMap.build("issue", "2", "filename", "myFile.txt", "created", "2008-01-08 12:17:39.544", "author", "admin"));
            fail("Should throw ParseException.");
        }
        catch (ParseException e)
        {
            // Expected.
        }
    }

    @Test
    public void testParseParseExceptionMissingIssueId()
    {
        try
        {
            attachmentParser.parse(EasyMap.build("id", "1", "filename", "myFile.txt", "created", "2008-01-08 12:17:39.544", "author", "admin"));
            fail("Should throw ParseException.");
        }
        catch (ParseException e)
        {
            // Expected.
        }
    }
    @Test
    public void testParseParseExceptionMissingFileName()
    {
        try
        {
            attachmentParser.parse(EasyMap.build("id", "1", "issue", "2", "created", "2008-01-08 12:17:39.544", "author", "admin"));
            fail("Should throw ParseException.");
        }
        catch (ParseException e)
        {
            // Expected.
        }
    }
    @Test
    public void testParseParseExceptionMissingCreatedDate()
    {
        try
        {
            attachmentParser.parse(EasyMap.build("id", "1", "issue", "2", "filename", "myFile.txt", "author", "admin"));
            fail("Should throw ParseException.");
        }
        catch (ParseException e)
        {
            // Expected.
        }
    }

    @Test
    public void testParseHappyNoAuthor() throws ParseException
    {
        ExternalAttachment externalAttachment = attachmentParser.parse(EasyMap.build("id", "1", "issue", "2", "filename", "myFile.txt", "created", "2008-01-08 12:17:39.544"));
        assertEquals("1", externalAttachment.getId());
        assertEquals("2", externalAttachment.getIssueId());
        assertEquals("myFile.txt", externalAttachment.getFileName());
        assertEquals(java.sql.Timestamp.valueOf("2008-01-08 12:17:39.544"), externalAttachment.getAttachedDate());
        assertNull(externalAttachment.getAttacher());
    }
    
    @Test
    public void testParseHappy() throws ParseException
    {
        ExternalAttachment externalAttachment = attachmentParser.parse(EasyMap.build("id", "1", "issue", "2", "filename", "myFile.txt", "created", "2008-01-08 12:17:39.544", "author", "admin"));
        assertEquals("1", externalAttachment.getId());
        assertEquals("2", externalAttachment.getIssueId());
        assertEquals("myFile.txt", externalAttachment.getFileName());
        assertEquals(java.sql.Timestamp.valueOf("2008-01-08 12:17:39.544"), externalAttachment.getAttachedDate());
        assertEquals("admin", externalAttachment.getAttacher());
    }

}
