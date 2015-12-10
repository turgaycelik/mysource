package com.atlassian.jira.web.filters.steps;

import com.atlassian.jira.mock.servlet.MockFilterChain;
import com.atlassian.jira.mock.servlet.MockFilterConfig;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.mock.servlet.MockHttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 */
public class FilterStepContextImplTest
{
    private MockHttpServletRequest httpServletRequest;
    private MockHttpServletResponse httpServletResponse;
    private MockFilterChain filterChain;
    private MockFilterConfig filterConfig;

    @Before
    public void setUp() throws Exception
    {
        httpServletRequest = new MockHttpServletRequest();
        httpServletResponse = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        filterConfig = new MockFilterConfig();
    }

    @Test
    public void testGetters()
    {
        FilterCallContextImpl context = new FilterCallContextImpl(httpServletRequest, httpServletResponse, filterChain, filterConfig);

        assertEquals(httpServletRequest, context.getHttpServletRequest());
        assertEquals(httpServletResponse, context.getHttpServletResponse());
        assertEquals(filterChain, context.getFilterChain());
        assertEquals(filterConfig, context.getFilterConfig());
    }

}
