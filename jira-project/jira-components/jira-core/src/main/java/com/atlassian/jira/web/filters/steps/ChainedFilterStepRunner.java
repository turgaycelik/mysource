package com.atlassian.jira.web.filters.steps;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A {@link Filter} that consists of a chain of filter steps to run.  A top level filter can become one of these guys
 * and name the steps he wants to run.
 * <p/>
 * This will only run the steps once per request.  Internal redirects will not have the filters re-run.  This is the
 * standard JIRA pattern.
 */
public abstract class ChainedFilterStepRunner implements Filter
{
    private final String filterName;
    private FilterConfig filterConfig;

    protected ChainedFilterStepRunner()
    {
        filterName = this.getClass().getCanonicalName() + "_alreadyfiltered";
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        this.filterConfig = filterConfig;
    }

    @Override
    public void destroy()
    {
        this.filterConfig = null;
    }

    /**
     * @return the list of {@link FilterStep}s to run
     */
    protected abstract List<FilterStep> getFilterSteps();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException
    {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        // Only apply this filter once per request. However, note that:
        // - the REQUEST dispatcher will call this method and iterate through the filter chain for all INCLUDEs,
        //   so we want avoid re-running the steps for each INCLUDE
        // - however the ERROR dispatcher runs after this method has returned from a REQUEST dispatch, and will be
        //   re-run on the ERROR dispatch.
        // For this reason, we clean up and remove the filterName request attribute. If a particular request
        // results in a REQUEST and ERROR dispatch, each of the steps in getFilterSteps() will be run twice.
        if (servletRequest.getAttribute(filterName) != null)
        {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        else
        {
            httpServletRequest.setAttribute(filterName, Boolean.TRUE);
        }


        List<FilterStep> filterSteps = notNull("filterSteps", getFilterSteps());
        FilterCallContext callContext = new FilterCallContextImpl(httpServletRequest, httpServletResponse, filterChain, filterConfig);
        try
        {
            for (final FilterStep filterStep : filterSteps)
            {
                callContext = notNull("callContext", filterStep.beforeDoFilter(callContext));
            }
            callContext.getFilterChain().doFilter(callContext.getHttpServletRequest(), callContext.getHttpServletResponse());
        }
        finally
        {
            // A,B,C becomes C,B,A on the way out, just like filters do
            for (final FilterStep filterStep : Iterables.reverse(filterSteps))
            {
                callContext = notNull("callContext", filterStep.finallyAfterDoFilter(callContext));
            }
            httpServletRequest.removeAttribute(filterName);
        }

    }

}
