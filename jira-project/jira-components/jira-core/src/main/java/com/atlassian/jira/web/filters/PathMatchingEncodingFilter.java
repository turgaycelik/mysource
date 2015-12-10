package com.atlassian.jira.web.filters;

import com.atlassian.core.filters.AbstractHttpFilter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Filter that applies appropriate encoding to request/response depending on the
 * servlet path.
 *
 * <p>
 * For some paths (currently only REST API) we want to always enforce standard (UTF-8) encoding, for others
 * the traditional, JIRA application property-based filter is used. 
 *
 * @since v4.2
 */
public class PathMatchingEncodingFilter extends AbstractHttpFilter implements Filter
{
    // TODO we might want to make it more generic and configurable if this changes often
    private static final String REST_PATH = "/rest/";


    private final Filter jiraEncodingFilter = new JiraEncodingFilter();
    private final Filter unicodeFilter = new FixedEncodingFilter("UTF-8");


    public void init(final FilterConfig filterConfig) throws ServletException
    {
        jiraEncodingFilter.init(filterConfig);
        unicodeFilter.init(filterConfig);
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException
    {
        if (isRestRequest(request))
        {
            unicodeFilter.doFilter(request, response, filterChain);
        }
        else
        {
            jiraEncodingFilter.doFilter(request, response, filterChain);
        }
    }

    private boolean isRestRequest(final HttpServletRequest request)
    {
        return request.getServletPath() != null && request.getServletPath().startsWith(REST_PATH);
    }

    public void destroy()
    {
        jiraEncodingFilter.destroy();
        unicodeFilter.destroy();
    }
}
