package com.atlassian.jira.web.filters;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.MockitoAnnotations.Mock;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestMultipartBoundaryCheckFilter
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Mock
    private ServletRequest request;
    @Mock
    private ServletResponse response;
    @Mock
    private FilterChain chain;

    private MultipartBoundaryCheckFilter filter;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp()
    {
        filter = new MultipartBoundaryCheckFilter();
    }

    @Test
    public void testNonMultipartContentType() throws Exception
    {
        stubContentType("text/html");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    public void testMultipartNoBoundaryContentType() throws Exception
    {
        stubContentType("multipart/form-data");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    public void testMultipartBoundaryRFCContentType() throws Exception
    {
        stubContentType(String.format("multipart/form-data; boundary=%s", StringUtils.leftPad("123", 40, "-")));

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    public void testMultipartBoundaryNonRFCContentType() throws Exception
    {
        stubContentType(String.format("multipart/form-data; boundary=%s", StringUtils.leftPad("456", 100, "-")));
        expectedException.expect(RuntimeException.class);

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(request, response);
    }

    @Test
    public void testMultipartInvalidBoundaryNonRFCContentType() throws Exception
    {
        stubContentType(String.format("multipart/form-data; boundary=%s #", StringUtils.leftPad("456", 100, "-")));
        expectedException.expect(RuntimeException.class);

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(request, response);
    }

    private void stubContentType(String contentType)
    {
        when(request.getContentType()).thenReturn(contentType);
    }
}
