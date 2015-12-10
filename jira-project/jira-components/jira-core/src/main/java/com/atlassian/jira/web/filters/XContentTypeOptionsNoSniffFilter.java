package com.atlassian.jira.web.filters;

import com.atlassian.core.filters.AbstractHttpFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This exists solely to deal with a security vulnerability in Internet Explorer: JRA-28879
 * IE can be tricked into parsing a text/html page as a stylesheet if it contains certain characters. Hence, a JIRA page
 * can be loaded as a stylesheet on an external, malicious site and voila, XSS.
 */
public class XContentTypeOptionsNoSniffFilter extends AbstractHttpFilter
{
    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException
    {
        resp.setHeader("X-Content-Type-Options", "nosniff");
        chain.doFilter(req, resp);
    }
}
