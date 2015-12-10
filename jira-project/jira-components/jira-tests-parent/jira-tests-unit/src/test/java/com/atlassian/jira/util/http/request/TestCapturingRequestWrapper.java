package com.atlassian.jira.util.http.request;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.servlet.ServletInputStream;

import com.atlassian.jira.mock.servlet.MockHttpServletRequest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 */
public class TestCapturingRequestWrapper
{
    private static final String HOW_COOL_IS_BRAD = "How cool is brad?";

    @Test
    public void testGeneralCaptureViaReader() throws IOException
    {
        BufferedReader reader = new BufferedReader(new StringReader(HOW_COOL_IS_BRAD));

        MockHttpServletRequest request = new MockHttpServletRequest(reader);
        CapturingRequestWrapper  wrapper = new CapturingRequestWrapper(request, 100);

        String line = wrapper.getReader().readLine();
        assertEquals(HOW_COOL_IS_BRAD, line);

        line = wrapper.getReader().readLine();
        assertNull(line);

        // assert we capture it
        byte[] bytes = wrapper.getBytes();
        assertNotNull(bytes);
        assertEquals(HOW_COOL_IS_BRAD, new String(bytes));
    }

    @Test
    public void testGeneralCaptureViaInputStream() throws IOException
    {
        final ByteArrayInputStream bis = new ByteArrayInputStream(HOW_COOL_IS_BRAD.getBytes());
        
        ServletInputStream servletInputStream = new ServletInputStream()
        {
            public int read() throws IOException
            {
                return bis.read();
            }
        };

        MockHttpServletRequest request = new MockHttpServletRequest(servletInputStream);
        CapturingRequestWrapper  wrapper = new CapturingRequestWrapper(request, 100);

        byte[] buf = new byte[100];
        int read = wrapper.getInputStream().readLine(buf,0,buf.length);
        assertEquals(HOW_COOL_IS_BRAD.length(), read);
        assertEquals(HOW_COOL_IS_BRAD, new String(buf,0,read));

        read = wrapper.getInputStream().readLine(buf,0,buf.length);
        assertEquals(-1,read);

        // assert we capture it
        byte[] bytes = wrapper.getBytes();
        assertNotNull(bytes);
        assertEquals(HOW_COOL_IS_BRAD, new String(bytes));
    }
}
