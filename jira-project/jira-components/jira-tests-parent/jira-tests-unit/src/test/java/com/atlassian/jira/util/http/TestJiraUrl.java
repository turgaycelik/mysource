package com.atlassian.jira.util.http;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

public class TestJiraUrl
{

    @Test
    public void testConstructBaseUrl()
    {
        final HttpServletRequest servletRequest = createMock(HttpServletRequest.class);
        expect(servletRequest.getScheme()).andReturn("http");
        expect(servletRequest.getServerName()).andReturn("jira.atlassian.com");
        expect(servletRequest.getServerPort()).andReturn(80);
        expect(servletRequest.getContextPath()).andReturn("/");

        replay(servletRequest);

        final String baseUrl = JiraUrl.constructBaseUrl(servletRequest);
        assertEquals("http://jira.atlassian.com/", baseUrl);

        verify(servletRequest);
    }

    @Test
    public void testConstructBaseUrlForHttps()
    {
        final HttpServletRequest servletRequest = createMock(HttpServletRequest.class);
        expect(servletRequest.getScheme()).andReturn("https");
        expect(servletRequest.getServerName()).andReturn("extranet.atlassian.com");
        expect(servletRequest.getServerPort()).andReturn(443);
        expect(servletRequest.getContextPath()).andReturn("/jira");

        replay(servletRequest);

        final String baseUrl = JiraUrl.constructBaseUrl(servletRequest);
        assertEquals("https://extranet.atlassian.com/jira", baseUrl);

        verify(servletRequest);
    }

    @Test
    public void testConstructBaseUrlForNonStandardPort()
    {
        final HttpServletRequest servletRequest = createMock(HttpServletRequest.class);
        expect(servletRequest.getScheme()).andReturn("http");
        expect(servletRequest.getServerName()).andReturn("localhost");
        expect(servletRequest.getServerPort()).andReturn(8080);
        expect(servletRequest.getContextPath()).andReturn("/atlassian-jira");

        replay(servletRequest);

        final String baseUrl = JiraUrl.constructBaseUrl(servletRequest);
        assertEquals("http://localhost:8080/atlassian-jira", baseUrl);

        verify(servletRequest);
    }

    @Test
    public void testConstructBaseUrlFor443onHttp()
    {
        final HttpServletRequest servletRequest = createMock(HttpServletRequest.class);
        expect(servletRequest.getScheme()).andReturn("http");
        expect(servletRequest.getServerName()).andReturn("stuffed.up.com");
        expect(servletRequest.getServerPort()).andReturn(443);
        expect(servletRequest.getContextPath()).andReturn("/atlassian-jira");

        replay(servletRequest);

        final String baseUrl = JiraUrl.constructBaseUrl(servletRequest);
        assertEquals("http://stuffed.up.com:443/atlassian-jira", baseUrl);

        verify(servletRequest);
    }

}
