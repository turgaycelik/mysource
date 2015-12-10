package com.atlassian.jira.web.filters.steps;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A place to stuff away context if you need them back during filter step execution
 */
public interface FilterCallContext
{
    /**
     * @return the {@link HttpServletRequest} in play
     */
    HttpServletRequest getHttpServletRequest();

    /**
     * @return the {@link HttpServletResponse} in play
     */
    HttpServletResponse getHttpServletResponse();

    /**
     * @return the {@link FilterChain} in play
     */
    FilterChain getFilterChain();

    /**
     * @return the {@link FilterConfig} in play
     */
    FilterConfig getFilterConfig();

}
