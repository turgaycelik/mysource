package com.atlassian.jira.plugin.searchrequestview;

import com.atlassian.annotations.Internal;

import javax.servlet.http.HttpServletResponse;

@Internal
public class HttpRequestHeaders implements RequestHeaders
{
    private final HttpServletResponse servletResponse;

    public HttpRequestHeaders(HttpServletResponse servletResponse)
    {
        this.servletResponse = servletResponse;
    }

    public void setDateHeader(String name, long date)
    {
        servletResponse.setDateHeader(name, date);
    }

    public void addDateHeader(String name, long date)
    {
        servletResponse.addDateHeader(name, date);
    }

    public void setHeader(String name, String value)
    {
        servletResponse.setHeader(name, value);
    }

    public void addHeader(String name, String value)
    {
        servletResponse.addHeader(name, value);
    }

    public void setIntHeader(String name, int value)
    {
        servletResponse.setIntHeader(name, value);
    }

    public void addIntHeader(String name, int value)
    {
        servletResponse.addIntHeader(name, value);
    }
}
