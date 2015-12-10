package com.atlassian.jira.web.filters;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.atlassian.jira.mock.servlet.MockFilterChain;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * The test here are prove that it delegates to the right underlying Filter.  It doesnt
 * check what those underlying filters do!
 */
public class TestJiraLoginFilter
{
    @Test
    public void testConstruction()
    {
        try
        {
            new JiraLoginFilter(null, new CountingFilter());
            fail("Bad input argument");
        }
        catch (IllegalArgumentException expected)
        {
        }
        try
        {
            new JiraLoginFilter(new CountingFilter(), null);
            fail("Bad input argument");
        }
        catch (IllegalArgumentException expected)
        {
        }
        new JiraLoginFilter(new CountingFilter(), new CountingFilter());
    }

    @Test
    public void testDoFilter_with_os_username() throws IOException, ServletException
    {
        CountingFilter cfLoginFilter = new CountingFilter();
        CountingFilter cfHttpAuthFilter = new CountingFilter();

        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("os_username", "admin");

        JiraLoginFilter filter = new JiraLoginFilter(cfLoginFilter, cfHttpAuthFilter);
        filter.doFilter(request, null, chain);

        assertEquals(1, cfLoginFilter.getCallCount("doFilter"));
        assertEquals(0, cfHttpAuthFilter.getCallCount("doFilter"));
    }

    @Test
    public void testDoFilter_with_authType() throws IOException, ServletException
    {
        CountingFilter cfLoginFilter = new CountingFilter();
        CountingFilter cfHttpAuthFilter = new CountingFilter();

        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("os_authType", "basic");

        JiraLoginFilter filter = new JiraLoginFilter(cfLoginFilter, cfHttpAuthFilter);
        filter.doFilter(request, null, chain);

        assertEquals(1, cfLoginFilter.getCallCount("doFilter"));
        assertEquals(0, cfHttpAuthFilter.getCallCount("doFilter"));
    }

    @Test
    public void testDoFilter_without_os_username() throws IOException, ServletException
    {
        CountingFilter cfLoginFilter = new CountingFilter();
        CountingFilter cfHttpAuthFilter = new CountingFilter();

        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest request = new MockHttpServletRequest();

        JiraLoginFilter filter = new JiraLoginFilter(cfLoginFilter, cfHttpAuthFilter);
        filter.doFilter(request, null, chain);

        assertEquals(0, cfLoginFilter.getCallCount("doFilter"));
        assertEquals(1, cfHttpAuthFilter.getCallCount("doFilter"));
    }

    @Test
    public void testInit() throws ServletException
    {
        CountingFilter cfLoginFilter = new CountingFilter();
        CountingFilter cfHttpAuthFilter = new CountingFilter();
        JiraLoginFilter filter = new JiraLoginFilter(cfLoginFilter, cfHttpAuthFilter);
        filter.init(null);

        assertEquals(1, cfLoginFilter.getCallCount("init"));
        assertEquals(1, cfHttpAuthFilter.getCallCount("init"));

        assertEquals(0, cfHttpAuthFilter.getCallCount("destroy"));
        assertEquals(0, cfHttpAuthFilter.getCallCount("destroy"));
    }

    @Test
    public void testDestroy()
    {
        CountingFilter cfLoginFilter = new CountingFilter();
        CountingFilter cfHttpAuthFilter = new CountingFilter();
        JiraLoginFilter filter = new JiraLoginFilter(cfLoginFilter, cfHttpAuthFilter);
        filter.destroy();

        assertEquals(0, cfLoginFilter.getCallCount("init"));
        assertEquals(0, cfHttpAuthFilter.getCallCount("init"));

        assertEquals(1, cfHttpAuthFilter.getCallCount("destroy"));
        assertEquals(1, cfHttpAuthFilter.getCallCount("destroy"));
    }

    private static class CountingFilter implements Filter
    {
        private ConcurrentMap<String, AtomicInteger> callCount = new ConcurrentHashMap<String, AtomicInteger>();

        public int getCallCount(String key)
        {
            AtomicInteger count = callCount.get(key);
            return count == null ? 0 : count.get();
        }

        public void init(FilterConfig filterConfig) throws ServletException
        {
            countMethod("init");
        }

        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
        {
            countMethod("doFilter");
        }

        public void destroy()
        {
            countMethod("destroy");
        }

        private void countMethod(String methodName)
        {
            AtomicInteger oldValue = callCount.putIfAbsent(methodName, new AtomicInteger(1));
            if (oldValue != null)
            {
                oldValue.incrementAndGet();
            }
        }
    }
}
