package com.atlassian.jira.web.servlet;

import java.io.CharArrayWriter;
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.MockControl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestBufferingRequestDispatcher
{
    private MockControl requestControl;
    private HttpServletRequest request;
    private MockControl responseControl;
    private HttpServletResponse response;
    private BufferingRequestDispatcher bufferingRequestDispatcher;

    @Before
    public void setUp() throws Exception
    {
        requestControl = MockControl.createStrictControl(HttpServletRequest.class);
        request = (HttpServletRequest) requestControl.getMock();
        responseControl = MockControl.createStrictControl(HttpServletResponse.class);
        response = (HttpServletResponse) responseControl.getMock();
        bufferingRequestDispatcher = new BufferingRequestDispatcher(request, response);
    }

    private void replay()
    {
        requestControl.replay();
        responseControl.replay();
    }

    private void verify()
    {
        requestControl.verify();
        responseControl.verify();
    }

    @Test
    public void testInclude() throws Exception
    {
        request.getRequestDispatcher("path");
        requestControl.setReturnValue(new RequestDispatcher()
        {
            public void forward(ServletRequest request, ServletResponse response)
            {
                throw new UnsupportedOperationException();
            }

            public void include(ServletRequest request, ServletResponse response) throws IOException
            {
                response.getWriter().write("output");
            }
        });
        replay();
        CharArrayWriter result = bufferingRequestDispatcher.include("path");

        assertEquals("output", result.toString());

        verify();
    }

    @Test
    public void testNoOutputStream() throws Exception
    {
        request.getRequestDispatcher("path");
        requestControl.setReturnValue(new RequestDispatcher()
        {
            public void forward(ServletRequest request, ServletResponse response)
            {
                throw new UnsupportedOperationException();
            }

            public void include(ServletRequest request, ServletResponse response) throws IOException
            {
                try
                {
                    response.getOutputStream();
                    fail("Exception not thrown for get outputs tream");
                }
                catch (UnsupportedOperationException uoe)
                {
                    // Pass
                }
            }
        });
        replay();
        bufferingRequestDispatcher.include("path");
        verify();
    }
}
