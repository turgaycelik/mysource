/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.util;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestHTMLCompressWriter
{
    @Test
    public void testWriteNormalText() throws IOException
    {
        _testWriter("abc", "abc");
    }

    @Test
    public void testCompressBlankLines() throws IOException
    {
        _testWriter("abc\ndef", "abc\n\ndef");
        _testWriter("abc\ndef", "abc\ndef");
        _testWriter("abc\ndef", "abc\n    \ndef");
        _testWriter("abc \ndef", "abc \ndef");
        _testWriter("abc \ndef", "abc \n\ndef");
        _testWriter("abc \ndef", "abc \n  \ndef");
    }

    @Test
    public void testCompressConsecutiveSpaces() throws IOException
    {
        _testWriter("abc def", "abc  def");
        _testWriter("abc def", "abc\tdef");
    }

    public void _testWriter(String expected, String input) throws IOException
    {
        StringWriter target = new StringWriter();
        HTMLCompressWriter compressWriter = new HTMLCompressWriter(target);
        compressWriter.write(input);
        assertEquals(expected, target.getBuffer().toString());
    }
}
