package com.atlassian.jira.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.template.mocks.VelocityTemplatingEngineMocks;

import com.mockobjects.servlet.MockHttpServletRequest;
import com.mockobjects.servlet.MockHttpServletResponse;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.RequestConstants;
import com.opensymphony.module.sitemesh.html.util.StringSitemeshBuffer;
import com.opensymphony.module.sitemesh.parser.AbstractHTMLPage;
import com.opensymphony.module.sitemesh.parser.AbstractPage;

import org.apache.velocity.exception.VelocityException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class TestVelocityDecoratorServlet
{
    private ApplicationProperties mockApplicationProperties;
    private JiraAuthenticationContext mockJiraAuthenticationContext;
    private VelocityDecoratorServlet velocityDecoratorServlet;
    private Map<String, Object> velocityContext;
    private VelocityTemplatingEngine mockVelocityTemplatingEngine;

    @Before
    public void setUp() throws Exception
    {
        mockApplicationProperties = mock(ApplicationProperties.class);
        mockJiraAuthenticationContext = mock(JiraAuthenticationContext.class);

        velocityContext = new HashMap<String, Object>();

        velocityDecoratorServlet = new VelocityDecoratorServlet()
        {
            ApplicationProperties getApplicationProperties()
            {
                return mockApplicationProperties;
            }

            JiraAuthenticationContext getJiraAuthenticationContext()
            {
                return mockJiraAuthenticationContext;
            }

            Map<String, Object> getDefaultVelocityParams()
            {
                return velocityContext;
            }

            @Override
            VelocityTemplatingEngine getTemplatingEngine()
            {
                return mockVelocityTemplatingEngine;
            }
        };
    }

    @Test
    public void shouldDecorateBodyGivenADispatchedRequest() throws Exception
    {
        MockHttpServletResponse response = createResponse();
        mockVelocityTemplatingEngine = VelocityTemplatingEngineMocks.alwaysOutput("rendered body").get();

        velocityDecoratorServlet.service(createRequest(createPage("title", "head", "body"), "path", true), response);

        assertEquals("rendered body", response.getOutputStreamContents());

        assertEquals("title", velocityContext.get("title"));
        assertEquals("head", velocityContext.get("head"));
        assertEquals("body", velocityContext.get("body").toString());
    }

    @Test
    public void shouldDecorateBodyGivenANonDispatchedRequest() throws Exception
    {
        MockHttpServletResponse response = createResponse();
        mockVelocityTemplatingEngine = VelocityTemplatingEngineMocks.alwaysOutput("rendered body").get();

        velocityDecoratorServlet.service(createRequest(createPage("title", "head", "body"), "path", false), response);

        assertEquals("rendered body", response.getOutputStreamContents());

        assertEquals("title", velocityContext.get("title"));
        assertEquals("head", velocityContext.get("head"));
        assertEquals("body", velocityContext.get("body").toString());
    }

    @Test
    public void shouldDecorateGivenWhenThereIsAPageAttributeInTheRequest() throws Exception
    {
        MockHttpServletResponse response = createResponse();
        mockVelocityTemplatingEngine = VelocityTemplatingEngineMocks.alwaysOutput("rendered body").get();

        velocityDecoratorServlet.service(createRequest(createPage("title", null, "body"), "path", true), response);

        assertEquals("rendered body", response.getOutputStreamContents());

        assertEquals("title", velocityContext.get("title"));
        assertEquals("body", velocityContext.get("body").toString());
    }

    @Test(expected = ServletException.class)
    public void shouldThrowAnExceptionWhenThereIsNoPageAttributeInTheRequest() throws Exception
    {
        HttpServletResponse response = mock(HttpServletResponse.class);
        velocityDecoratorServlet.service(createRequest(null, "path", true), response);
    }

    @Test
    public void shouldReturnAPageWithTheExceptionTextIfAVelocityExceptionIsThrown() throws Exception
    {
        MockHttpServletResponse response = createResponse();
        mockVelocityTemplatingEngine = VelocityTemplatingEngineMocks.alwaysThrow(new VelocityException("Hello")).get();

        velocityDecoratorServlet.service(createRequest(createPage("title", "head", "body"), "path", true), response);

        // Remember, the contents has new lines in it so we need dotall mode
        assertTrue(response.getOutputStreamContents().matches("(?s)Exception rendering velocity file path.*Hello.*"));
    }

    private Page createPage(final String title, final String head, final String body) throws Exception
    {
        AbstractPage page;
        if (head != null)
        {
            page = new AbstractHTMLPage(new StringSitemeshBuffer(""))
            {
                public void writeBody(Writer writer) throws IOException
                {
                    writer.write(body);
                }

                public void writeHead(Writer writer) throws IOException
                {
                    writer.write(head);
                }

                public String getHead()
                {
                    return head;
                }

                public String getTitle()
                {
                    return title;
                }
            };
        }
        else
        {
            page = new AbstractPage(new StringSitemeshBuffer(""))
            {
                public void writeBody(Writer writer) throws IOException
                {
                    writer.write(body);
                }

                public String getTitle()
                {
                    return title;
                }
            };
        }
        return page;
    }

    private MockHttpServletRequest createRequest(final Page page, final String path, final boolean dispatched)
    {
        // Don't really want to use the mockobjects version, but using it is easier than implementing the raw interface
        return new MockHttpServletRequest()
        {
            public Object getAttribute(String s)
            {
                if (s.equals(RequestConstants.PAGE))
                {
                    return page;
                }
                else if (dispatched && s.equals("javax.servlet.include.servlet_path"))
                {
                    return path;
                }
                return null;
            }

            public String getServletPath()
            {
                if (!dispatched)
                {
                    return path;
                }
                else
                {
                    return "irrelaventpath";
                }
            }

            public String getContextPath()
            {
                return "context";
            }
        };
    }

    private MockHttpServletResponse createResponse()
    {
        return new MockHttpServletResponse()
        {
            private PrintWriter writer = null;

            public PrintWriter getWriter() throws IOException
            {
                if (writer == null)
                {
                    writer = super.getWriter();
                }
                return writer;
            }

            public String getOutputStreamContents()
            {
                if (writer != null)
                {
                    writer.flush();
                }
                return super.getOutputStreamContents();
            }
        };
    }
}
