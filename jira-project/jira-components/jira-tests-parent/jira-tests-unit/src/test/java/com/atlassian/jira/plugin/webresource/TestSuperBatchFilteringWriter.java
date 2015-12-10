package com.atlassian.jira.plugin.webresource;

import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestSuperBatchFilteringWriter
{
    @Test
    public void testFiltering() throws IOException
    {
        SuperBatchFilteringWriter writer = new SuperBatchFilteringWriter();
        writer.write("<script src=\"some/url/blah\">");
        assertEquals("<script src=\"some/url/blah\">", writer.toString());

        writer = new SuperBatchFilteringWriter();
        writer.write("<script src=\"some/url/download/superbatch/batch.css\">");
        assertEquals("", writer.toString());
        
        writer = new SuperBatchFilteringWriter();
        writer.write("<script src=\"some/url/download/superbatch/batch.css\">");
        writer.write("<script src=\"some/url/dude\">");
        assertEquals("<script src=\"some/url/dude\">", writer.toString());

        writer = new SuperBatchFilteringWriter();
        char[] chars = "<script src=\"some/url/download/superbatch/batch.css\">".toCharArray();
        writer.write(chars, 0, chars.length);
        assertEquals("", writer.toString());
    }
}
