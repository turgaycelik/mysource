package com.atlassian.jira.web.filters;

import com.atlassian.core.filters.AbstractHttpFilter;
import com.atlassian.core.filters.legacy.WordCurlyQuotesRequestWrapper;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Applies the {@link com.atlassian.core.filters.legacy.WordCurlyQuotesRequestWrapper} to the request, in order to
 * escape high-bit punctuation characters with ASCII equivalents in request parameter values.
 *
 * @since v4.0
 */
public class CurlyQuotesFilter extends AbstractHttpFilter
{
    protected void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException
    {
        filterChain.doFilter(new WordCurlyQuotesRequestWrapper(request, getEncoding()), response);
    }

    protected String getEncoding()
    {
        try
        {
            return ComponentAccessor.getApplicationProperties().getEncoding();
        }
        catch (Exception e)
        {
            return "UTF-8";
        }
    }
}
