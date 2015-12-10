package com.atlassian.jira.mock.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Mock servlet filter implementation tracking number of calls. Use it only in single-threaded test environment.
 *
 * @since v4.2
 */
public class MockFilter implements Filter
{
    // not in a mood to make all those getters 
    public int initCalls = 0;
    public int filterCalls = 0;
    public int destroyCalls = 0;

    public FilterConfig lastConfig;
    public ServletRequest lastRequest;
    public ServletResponse lastResponse;
    public FilterChain lastChain;


    
    public void init(final FilterConfig filterConfig) throws ServletException
    {
        initCalls++;
        lastConfig = filterConfig;
    }

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException
    {
        filterCalls++;
        lastRequest = servletRequest;
        lastResponse = servletResponse;
        lastChain = filterChain;
    }

    public void destroy()
    {
        destroyCalls++;
    }
}
