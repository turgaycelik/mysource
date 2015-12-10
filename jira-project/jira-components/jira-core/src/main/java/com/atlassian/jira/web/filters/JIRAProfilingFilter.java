package com.atlassian.jira.web.filters;

import com.atlassian.util.profiling.filters.ProfilingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class JIRAProfilingFilter extends ProfilingFilter
{
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        ThreadLocalQueryProfiler.start();

        try
        {
            super.doFilter(servletRequest, servletResponse, filterChain);
        }
        finally
        {
            ThreadLocalQueryProfiler.end();
        }
    }
}
