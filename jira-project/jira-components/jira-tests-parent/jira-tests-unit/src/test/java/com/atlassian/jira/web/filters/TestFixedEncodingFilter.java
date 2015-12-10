package com.atlassian.jira.web.filters;

import java.io.IOException;

import javax.servlet.ServletException;

import com.atlassian.jira.mock.servlet.MockFilterChain;
import com.atlassian.jira.mock.servlet.MockFilterConfig;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.mock.servlet.MockHttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test case for the {@link FixedEncodingFilter},
 *
 * @since v4.2
 */
public class TestFixedEncodingFilter
{
    private FixedEncodingFilter tested;

    private MockFilterConfig mockFilterConfig = new MockFilterConfig("PathExclusion");
    private MockFilterChain mockChain = new MockFilterChain();
    private MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    private MockHttpServletResponse mockResponse = new MockHttpServletResponse();

    @Before
    public void setUp() throws Exception
    {

    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void testInstantiateWithConstructorArguments() throws Exception
    {
        tested = new FixedEncodingFilter("ISO-8859-1", "application/json;charset=UTF-8");
        runTestedFilterLifecycle();
        assertEncoding("ISO-8859-1");
        assertContentType("application/json;charset=UTF-8");
    }

    @Test
    public void testFilterConfigDoesNotOverrideConstructorParams() throws Exception
    {
        mockFilterConfig.addInitParam(FixedEncodingFilter.InitParams.ENCODING.key(), "UTF-8");
        mockFilterConfig.addInitParam(FixedEncodingFilter.InitParams.CONTENT_TYPE.key(), "application/x-www-form-urlencoded;charset=ISO-8859-1");
        tested = new FixedEncodingFilter("cp1250", "text/html");
        runTestedFilterLifecycle();
        assertEncoding("cp1250");
        assertContentType("text/html");
    }

    @Test
    public void testContentTypeFromConfig() throws Exception
    {
        mockFilterConfig.addInitParam(FixedEncodingFilter.InitParams.ENCODING.key(), "UTF-8");
        mockFilterConfig.addInitParam(FixedEncodingFilter.InitParams.CONTENT_TYPE.key(), "application/x-www-form-urlencoded;charset=ISO-8859-1");
        tested = new FixedEncodingFilter("cp1250");
        runTestedFilterLifecycle();
        assertEncoding("cp1250");
        assertContentType("application/x-www-form-urlencoded;charset=ISO-8859-1");
    }

    @Test
    public void testEncodingAndContentTypeFromConfig() throws Exception
    {
        mockFilterConfig.addInitParam(FixedEncodingFilter.InitParams.ENCODING.key(), "UTF-8");
        mockFilterConfig.addInitParam(FixedEncodingFilter.InitParams.CONTENT_TYPE.key(), "application/json;charset=ISO-8859-1");
        tested = new FixedEncodingFilter();
        runTestedFilterLifecycle();
        assertEncoding("UTF-8");
        assertContentType("application/json;charset=ISO-8859-1");
    }

    @Test
    public void testDefaultContentType() throws Exception
    {
        mockFilterConfig.addInitParam(FixedEncodingFilter.InitParams.ENCODING.key(), "UTF-8");
        tested = new FixedEncodingFilter();
        runTestedFilterLifecycle();
        assertEncoding("UTF-8");
        assertContentType("text/html; charset=UTF-8");
    }

    @Test
    public void testEncodingRequired() throws Exception
    {
        tested =  new FixedEncodingFilter();
        try
        {
            runTestedFilterLifecycle();
            fail("Expected exception because of missing encoding parameter");
        }
        catch(IllegalStateException expected)
        {
        }
    }

    @Test
    public void testInvalidEncoding() throws Exception
    {
        mockFilterConfig.addInitParam(FixedEncodingFilter.InitParams.ENCODING.key(), "Seriously, there is no such encoding scheme!");
        tested =  new FixedEncodingFilter();
        try
        {
            runTestedFilterLifecycle();
            fail("Expected exception because of invalid encoding");
        }
        catch(IllegalStateException expected)
        {
        }
    }

    private void assertEncoding(String encoding)
    {
        assertEquals(encoding, tested.getEncoding());
        assertEquals(encoding, mockRequest.getCharacterEncoding());
    }

    private void assertContentType(String contentType)
    {
        assertEquals(contentType, tested.getContentType());
        assertEquals(contentType, mockResponse.getContentType());
    }

    private void runTestedFilterLifecycle() throws IOException, ServletException
    {
        tested.init(mockFilterConfig);
        tested.doFilter(mockRequest, mockResponse, mockChain);
        tested.destroy();
    }

}
