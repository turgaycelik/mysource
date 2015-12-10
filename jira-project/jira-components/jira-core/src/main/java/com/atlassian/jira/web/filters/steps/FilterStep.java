package com.atlassian.jira.web.filters.steps;

/**
 * An interface defining steps that can be done as a compound filter step.
 * <p/>
 * The pattern is that you will beforeDoFilter() will be called, then the underlying filter chain will be invoked and
 * then finallyAfterDoFilter() will be run.
 * <p/>
 * You can keep state in yourself because a new step object will be instantiated for each request.
 */
public interface FilterStep
{
    /**
     * This is called to before the filterChain.doFilter() method is called
     *
     * @param callContext the context of the filter step call
     */
    FilterCallContext beforeDoFilter(FilterCallContext callContext);

    /**
     * This is called after the filterChain.doFilter() method is called in a finally {} block
     *
     * @param callContext the context of the filter step call
     */
    FilterCallContext finallyAfterDoFilter(FilterCallContext callContext);
}
