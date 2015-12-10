package com.atlassian.jira.mock.servlet;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * A mock filter chain implementation
 *
 * @since v4.0
 */
public class MockFilterChain implements FilterChain
{
    public int filterCalls = 0;

    public ServletRequest lastRequest;
    public ServletResponse lastResponse;

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse) throws IOException, ServletException
    {
        filterCalls++;
        lastRequest = servletRequest;
        lastResponse = servletResponse;
    }
}
