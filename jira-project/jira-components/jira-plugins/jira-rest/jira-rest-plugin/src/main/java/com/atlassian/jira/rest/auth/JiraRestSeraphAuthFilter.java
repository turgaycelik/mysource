package com.atlassian.jira.rest.auth;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * JRA-25405: Users need to be able to hit '/rest/auth' even when their session has expired.
 *
 * The 'RestSeraphFilter' adds a magic seraph attribute to rest requests (i.e. /rest) that make seraph return a 401
 * when a request is made from a person whose session timed out. It does this under the assumption
 * that the person did not mean to logout and will want to know that their session expired.
 *
 * Unfortunately, to log back in this user needs to hit a rest resource (i.e. /rest/auth). However, they
 * will be unable to do so as they get a 401. To get around this we set this magic attribute to "none" for
 * rest calls under '/rest/auth' under the assumption that these calls will handle their own authentication.
 *
 * @since v4.4.5
 */
public class JiraRestSeraphAuthFilter implements Filter
{
    // This *must* be the same thing as in com.atlassian.seraph.auth.AuthType
    private static final String DEFAULT_ATTRIBUTE = "os_authTypeDefault";
    private static final String AUTH_TYPE_NONE = "none";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (httpRequest.getAttribute(DEFAULT_ATTRIBUTE) == null)
        {
            httpRequest.setAttribute(DEFAULT_ATTRIBUTE, AUTH_TYPE_NONE);
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy()
    {
    }
}
