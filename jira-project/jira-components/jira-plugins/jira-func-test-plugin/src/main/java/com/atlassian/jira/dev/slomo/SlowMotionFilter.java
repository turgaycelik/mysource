package com.atlassian.jira.dev.slomo;

import com.atlassian.core.filters.AbstractHttpFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Slows down each request to expose race conditions in browser-based asynchronous integration tests.
 *
 * @since v5.0
 */
class SlowMotionFilter extends AbstractHttpFilter
{
    private static final String HEADER_SLOMO = "X-Atlassian-Slomo";
    private final SlowMotion slowMotion;

    SlowMotionFilter(SlowMotion slowMotion)
    {
        this.slowMotion = slowMotion;
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException
    {
        int slowDown = slowMotion.getSlowDown(request);
        if (slowDown > 0)
        {
            try
            {
                response.setHeader(HEADER_SLOMO, Long.toString(slowDown));
                Thread.sleep(slowDown);
            }
            catch (InterruptedException ignore)
            {
            }
        }

        filterChain.doFilter(request, response);
    }
}
