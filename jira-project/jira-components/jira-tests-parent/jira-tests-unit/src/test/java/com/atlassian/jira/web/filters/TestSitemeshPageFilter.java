package com.atlassian.jira.web.filters;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.mock.servlet.MockFilterChain;
import com.atlassian.jira.mock.servlet.MockFilterConfig;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.mock.servlet.MockHttpServletResponse;
import com.atlassian.jira.util.collect.IteratorEnumeration;

import com.opensymphony.module.sitemesh.Factory;
import com.opensymphony.module.sitemesh.RequestConstants;
import com.opensymphony.sitemesh.webapp.SiteMeshFilter;

import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import static com.atlassian.jira.easymock.MockType.DEFAULT;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.fail;

/**
 * Test case for the SitemeshPageFilter,
 *
 * @since v4.2
 */
public class TestSitemeshPageFilter
{
    /**
     * The request attribute that needs to get set (got using reflection).
     */
    private static final String ALREADY_APPLIED_KEY;

    static
    {
        Field f = ReflectionUtils.findField(SiteMeshFilter.class, "ALREADY_APPLIED_KEY");

        ReflectionUtils.makeAccessible(f);
        ALREADY_APPLIED_KEY = (String) ReflectionUtils.getField(f, null);
    }

    private SitemeshPageFilter tested;
    private MockFilterChain mockChain;
    private MockHttpServletResponse mockResponse;

    @Mock (DEFAULT) MockHttpServletRequest httpRequest;
    @Mock MockFilterConfig mockFilterConfig;
    @Mock ServletContext servletContext;
    @Mock Factory factory;

    @Test
    public void differentPathShouldNotBeExcluded() throws Exception
    {
        setExcludedPaths("/one,/two,/four");

        expectRequestExcluded(false);

        runTest("/three");
        verify(httpRequest);
    }

    @Test
    public void subpathShouldNotBeExcludedByExactPattern() throws Exception
    {
        setExcludedPaths("/one");

        expectRequestExcluded(false);

        runTest("/one/three");
        verify(httpRequest);
    }

    @Test
    public void exactMatchShouldBeExcluded() throws Exception
    {
        setExcludedPaths("/one");

        expectRequestExcluded(true);

        runTest("/one");
        verify(httpRequest);
    }

    @Test
    public void subpathShouldBeExcludedByWildcardPattern() throws Exception
    {
        setExcludedPaths("/one,/two/*");

        expectRequestExcluded(true);

        runTest("/two/three");
        verify(httpRequest);
    }

    @Test
    public void wildcardAfterSlashShouldNotExcludePath() throws Exception
    {
        setExcludedPaths("/one/two/*,/one");

        expectRequestExcluded(false);

        runTest("/one/two");
        verify(httpRequest);
    }

    @Test
    public void wildcardWithNoSlashShouldExcludeFullPath() throws Exception
    {
        setExcludedPaths("/one/two*");

        expectRequestExcluded(true);

        runTest("/one/two");
        verify(httpRequest);
    }

    @Test
    public void pathsInInitParamShouldBeTrimmed() throws Exception
    {
        setExcludedPaths("  /one,\n/one/two  ,/three");

        expectRequestExcluded(true);

        runTest("/one/two");
        verify(httpRequest);
    }

    @Test
    public void excludePathInitParamIsRequired() throws Exception
    {
        setExcludedPaths(null);
        try
        {
            runTest("/abc");
            fail("Expected exception because of missing excluded paths init param");
        }
        catch (IllegalStateException expected)
        {
        }
    }

    @Test
    public void excludeHeaderIsWorking() throws Exception
    {
        setExcludedPaths("/ignored");
        setExcludedHeaders("X-IGNORE, X-SHEEIT");

        expectRequestExcluded(true);

        runTest("/one/two", "X-IGNORE");
        verify(httpRequest);
    }

    private void setExcludedHeaders(String excludedHeaders)
    {
        expect(mockFilterConfig.getInitParameter(SitemeshPageFilter.InitParams.EXCLUDED_HEADERS.key())).andStubReturn(excludedHeaders);
    }

    @Before
    public void setUpMocks() throws Exception
    {
        tested = new SitemeshPageFilter();
        mockChain = new MockFilterChain();
        mockResponse = new MockHttpServletResponse();

        EasyMockAnnotations.initMocks(this);
        setUpRequest();
        setUpFilterConfig();
    }

    protected void expectRequestExcluded(boolean excluded)
    {
        if (!excluded)
        {
            httpRequest.setAttribute(ALREADY_APPLIED_KEY, Boolean.TRUE);
        }
    }

    private void setUpFilterConfig()
    {
        expect(mockFilterConfig.getFilterName()).andStubReturn("PathExclusion");
        expect(mockFilterConfig.getServletContext()).andStubReturn(servletContext);
        expect(servletContext.getAttribute("sitemesh.factory")).andStubReturn(factory);
    }

    private void setUpRequest()
    {
        expect(httpRequest.getAttribute(ALREADY_APPLIED_KEY)).andReturn(null).times(0, 1);

        // stuff that the sitemesh filter sets
        expect(httpRequest.getPathInfo()).andStubReturn(null);
        expect(httpRequest.getQueryString()).andStubReturn(null);

        // may or may not get called
        httpRequest.setAttribute("com.opensymphony.sitemesh.USINGSTREAM", false);
        expectLastCall().anyTimes();
        httpRequest.setAttribute(RequestConstants.SECONDARY_STORAGE_LIMIT, -1L);
        expectLastCall().anyTimes();
    }

    private void runTest(String servletPath) throws IOException, ServletException
    {
        runTest(servletPath, "");
    }

    private void runTest(String servletPath, String header) throws IOException, ServletException
    {
        // set up request path and headers
        expect(httpRequest.getServletPath()).andStubReturn(servletPath);
        if (!isBlank(header))
        {
            expect(httpRequest.getHeaders(header)).andStubReturn(IteratorEnumeration.fromIterable(Arrays.asList("true")));
            expect(httpRequest.getHeaderNames()).andStubReturn(IteratorEnumeration.fromIterable(Arrays.asList(header)));
        }
        else
        {
            expect(httpRequest.getHeaderNames()).andStubReturn(IteratorEnumeration.fromIterable(Collections.<Object>emptyList()));
        }

        EasyMockAnnotations.replayMocks(this);
        tested.init(mockFilterConfig);
        tested.doFilter(httpRequest, mockResponse, mockChain);
        tested.destroy();
    }

    private void setExcludedPaths(String excludedPaths)
    {
        expect(mockFilterConfig.getInitParameter(SitemeshPageFilter.InitParams.EXCLUDED_PATHS.key())).andStubReturn(excludedPaths);
    }
}
