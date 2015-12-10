package com.atlassian.jira.util.http.response;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.atlassian.jira.mock.servlet.MockHttpServletResponse;
import com.atlassian.jira.mock.servlet.MockServletOutputStream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 */
public class TestObservantResponseWrapper
{
    private static final String HOW_COOL_IS_BRAD = "How cool is brad?";

    @Test
    public void testStatus() throws IOException
    {
        MockHttpServletResponse underlyingResponse = new MockHttpServletResponse()
        {
            public void setStatus(final int i)
            {
                assertEquals(400, i);
            }

            public void setStatus(final int i, final String s)
            {
                assertEquals(500, i);
                assertEquals("withStr", s);
            }

            public void sendError(final int i) throws IOException
            {
                assertEquals(600, i);
            }

            public void sendError(final int i, final String s) throws IOException
            {
                assertEquals(700, i);
                assertEquals("withStr", s);
            }
        };
        ObservantResponseWrapper wrapper = new ObservantResponseWrapper(underlyingResponse);

        // default
        assertEquals(200, wrapper.getStatus());


        wrapper.setStatus(400);
        assertEquals(400, wrapper.getStatus());

        wrapper.setStatus(500, "withStr");
        assertEquals(500, wrapper.getStatus());

        wrapper.sendError(600);
        assertEquals(600, wrapper.getStatus());

        wrapper.sendError(700, "withStr");
        assertEquals(700, wrapper.getStatus());
    }

    @Test
    public void testContentLenViaWriter() throws IOException
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        MockHttpServletResponse underlyingResponse = new MockHttpServletResponse(pw);

        ObservantResponseWrapper wrapper = new ObservantResponseWrapper(underlyingResponse);

        wrapper.getWriter().print(HOW_COOL_IS_BRAD);

        assertEquals(HOW_COOL_IS_BRAD.length(), wrapper.getContentLen());

        // did it make back into the underlying writers
        assertEquals(HOW_COOL_IS_BRAD, sw.toString());
    }

    @Test
    public void testContentLenViaServletOutputStream() throws IOException
    {
        StringWriter sw = new StringWriter();
        MockServletOutputStream outputStream = new MockServletOutputStream(sw);

        MockHttpServletResponse underlyingResponse = new MockHttpServletResponse(outputStream);

        ObservantResponseWrapper wrapper = new ObservantResponseWrapper(underlyingResponse);

        wrapper.getOutputStream().print(HOW_COOL_IS_BRAD);

        assertEquals(HOW_COOL_IS_BRAD.length(), wrapper.getContentLen());

        // did it make back into the underlying writers
        assertEquals(HOW_COOL_IS_BRAD, sw.toString());
    }
}
