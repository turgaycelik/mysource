package com.atlassian.jira.mail.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringBufferInputStream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13.3
 */
public class TestByteArrayDataSource
{
    private static final String CONTENT_TYPE = "rhino/elephants";
    private static final String DATA_STRING = "data";

    @Test
    public void testGetContentType() throws IOException
    {
        ByteArrayDataSource ds = new ByteArrayDataSource(getStream(DATA_STRING), CONTENT_TYPE);
        assertEquals(CONTENT_TYPE, ds.getContentType());
    }

    @Test
    public void testGetInputStream() throws IOException
    {
        ByteArrayDataSource ds = new ByteArrayDataSource(getStream(DATA_STRING), CONTENT_TYPE);
        assertNotNull(ds.getInputStream());
        String actualData = new BufferedReader(new InputStreamReader(ds.getInputStream())).readLine();
        assertEquals(DATA_STRING, actualData);
    }

    @Test
    public void testGetName() throws IOException
    {
        ByteArrayDataSource ds = new ByteArrayDataSource(getStream(DATA_STRING), CONTENT_TYPE);
        assertEquals("ByteArrayDataSource", ds.getName());
    }

    @Test
    public void testGetOutputStream() throws IOException
    {
        ByteArrayDataSource ds = new ByteArrayDataSource(getStream(DATA_STRING), CONTENT_TYPE);
        final OutputStream outputStream = ds.getOutputStream();
        assertNotNull(outputStream);
        assertTrue(outputStream instanceof ByteArrayOutputStream);

        ByteArrayOutputStream byteArrayOutputStream = (ByteArrayOutputStream) outputStream;

        assertNotNull(byteArrayOutputStream.toByteArray());
        assertEquals(0,byteArrayOutputStream.toByteArray().length);

    }

    private StringBufferInputStream getStream(final String data) throws IOException
    {
        return new StringBufferInputStream(data);
    }
}
