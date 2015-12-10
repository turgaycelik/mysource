package com.atlassian.jira.web.filters.steps;

import java.util.List;

import com.atlassian.jira.mock.servlet.MockFilterChain;
import com.atlassian.jira.mock.servlet.MockFilterConfig;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.mock.servlet.MockHttpServletResponse;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 */
public class ChainedFilterStepRunnerTest
{
    private MockFilterStep step1;
    private MockFilterStep step2;
    private MockFilterStep step3;
    private MockFilterChain filterChain;
    private MockHttpServletResponse httpServletResponse;
    private MockHttpServletRequest httpServletRequest;

    @Before
    public void setUp() throws Exception
    {
        step1 = new MockFilterStep();
        step2 = new MockFilterStep();
        step3 = new MockFilterStep();
        filterChain = new MockFilterChain();
        httpServletResponse = new MockHttpServletResponse();
        httpServletRequest = new MockHttpServletRequest();
    }


    @Test
    public void testDoFilter() throws Exception
    {

        ChainedFilterStepRunner runner = instatiateRunner(step1, step2, step3);

        runner.init(new MockFilterConfig("filterName"));
        runner.doFilter(httpServletRequest, httpServletResponse, filterChain);

        assertEquals(1, step1.beforeCalled);
        assertEquals(1, step1.finallyCalled);

        assertEquals(1, step2.beforeCalled);
        assertEquals(1, step2.finallyCalled);

        assertEquals(1, step3.beforeCalled);
        assertEquals(1, step3.finallyCalled);

        assertEquals(1, filterChain.filterCalls);
    }

    @Test
    public void testDoFilterCalledTwice() throws Exception
    {

        ChainedFilterStepRunner runner = instatiateRunner(step1, step2, step3);

        runner.init(new MockFilterConfig("filterName"));

        runner.doFilter(httpServletRequest, httpServletResponse, filterChain);
        runner.doFilter(httpServletRequest, httpServletResponse, filterChain);

        assertEquals(1, step1.beforeCalled);
        assertEquals(1, step1.finallyCalled);

        assertEquals(1, step2.beforeCalled);
        assertEquals(1, step2.finallyCalled);

        assertEquals(1, step3.beforeCalled);
        assertEquals(1, step3.finallyCalled);

        // forwards to chain and runs only once
        assertEquals(2, filterChain.filterCalls);
    }

    @Test
    public void testDoFilterPassesOnContext() throws Exception
    {
        final FilterCallContext callContext1 = new FilterCallContextImpl(null,null,null,null);
        final FilterCallContext callContext2 = new FilterCallContextImpl(null,null,null,null);

        FilterStep step1 = new FilterStep()
        {
            @Override
            public FilterCallContext beforeDoFilter(FilterCallContext callContext)
            {
                return callContext1;
            }

            @Override
            public FilterCallContext finallyAfterDoFilter(FilterCallContext callContext)
            {
                assertEquals(callContext,callContext1);
                return callContext2;
            }
        };
        FilterStep step2 = new FilterStep()
        {
            @Override
            public FilterCallContext beforeDoFilter(FilterCallContext callContext)
            {
                assertEquals(callContext,callContext2);
                return callContext1;
            }

            @Override
            public FilterCallContext finallyAfterDoFilter(FilterCallContext callContext)
            {
                assertEquals(callContext,callContext1);
                return callContext2;
            }
        };

        ChainedFilterStepRunner runner = instatiateRunner(step1,step2);
    }

    private ChainedFilterStepRunner instatiateRunner(final FilterStep... steps)
    {
        return new ChainedFilterStepRunner()
        {
            @Override
            protected List<FilterStep> getFilterSteps()
            {
                return Lists.<FilterStep>newArrayList(steps);
            }
        };
    }

    private static class MockFilterStep implements FilterStep
    {
        int beforeCalled;
        int finallyCalled;

        @Override
        public FilterCallContext beforeDoFilter(FilterCallContext callContext)
        {
            beforeCalled++;
            return callContext;
        }

        @Override
        public FilterCallContext finallyAfterDoFilter(FilterCallContext callContext)
        {
            finallyCalled++;
            return callContext;
        }
    }
}
