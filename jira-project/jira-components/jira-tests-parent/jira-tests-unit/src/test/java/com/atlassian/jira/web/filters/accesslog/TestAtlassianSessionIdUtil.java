package com.atlassian.jira.web.filters.accesslog;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import com.mockobjects.servlet.MockHttpSession;

import org.junit.Test;

import junit.framework.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 */
public class TestAtlassianSessionIdUtil
{
    private static final String JSESSION_VALUE_1 = "5ABE652343674332";

    private static final String JSESSION_ENCODED_VALUE_1 = "p3yo5w";
    private static final String JSESSION_ENCODED_COOKIE_VALUE_1 = "p3yo5w-5ABE652343674332";

    private static final String JSESSION_VALUE_2 = "CAFEBABE12312DF12";
    private static final String JSESSION_ENCODED_VALUE_2 = "jqhbx2";

    public static final String CONTEXT_PATH = "/testjirapath";

    @Test
    public void testEncodingStability()
    {
        String value1 = AtlassianSessionIdUtil.generateASESSIONID(JSESSION_VALUE_1);
        String value2 = AtlassianSessionIdUtil.generateASESSIONID(JSESSION_VALUE_1);

        assertNotNull(value1);
        assertEquals(value1, value2);
        assertEquals(JSESSION_ENCODED_VALUE_1, value1);
    }

    @Test
    public void testEncodingDifferencesWhenDifferentValues()
    {
        String value1 = AtlassianSessionIdUtil.generateASESSIONID(JSESSION_VALUE_1);
        String value2 = AtlassianSessionIdUtil.generateASESSIONID(JSESSION_VALUE_2);

        assertEquals(JSESSION_ENCODED_VALUE_1, value1);
        assertEquals(JSESSION_ENCODED_VALUE_2, value2);
    }

    @Test
    public void testNullInput()
    {
        assertNull(AtlassianSessionIdUtil.generateASESSIONID(null));
    }

    @Test
    public void testNoSessionNoACookie()
    {
        // Test that if the JSession cookie does not exist, then no ASession cookie is added
        MockHttpServletRequest mockRequest = buildMockRequest(null, CONTEXT_PATH);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse()
        {
            @Override
            public void addCookie(Cookie cookie)
            {
                Assert.fail("AddCookie should not be called");
            }

            @Override
            public void addHeader(String headerName, String headerValue)
            {
                Assert.fail("addHeader should not be called.");
            }
        };

        final String returnedValue = AtlassianSessionIdUtil.generateAtlassianSessionHash(mockRequest, mockResponse);

        assertNull(returnedValue);

        mockRequest.verify();
        mockResponse.verify();
    }

    @Test
    public void testSessionAttributeIsSet()
    {
        MockHttpSession mockSession = new MockHttpSession()
        {
            @Override
            public String getId()
            {
                return JSESSION_VALUE_1;
            }

        };
        MockHttpServletRequest mockRequest = buildMockRequest(mockSession, CONTEXT_PATH);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final String returnedValue = AtlassianSessionIdUtil.generateAtlassianSessionHash(mockRequest, mockResponse);

        assertEquals(JSESSION_ENCODED_VALUE_1, returnedValue);

        mockSession.verify();
        mockRequest.verify();
        mockResponse.verify();
    }

    @Test
    public void testNoCookieWhenSessionIdIsNull()
    {
        MockHttpSession mockSession = new MockHttpSession()
        {
            @Override
            public String getId()
            {
                return null;
            }

        };

        MockHttpServletRequest mockRequest = buildMockRequest(mockSession, CONTEXT_PATH);

        MockHttpServletResponse mockResponse = new MockHttpServletResponse()
        {
            @Override
            public void addCookie(Cookie cookie)
            {
                Assert.fail("AddCookie should not be called.");
            }

            @Override
            public void addHeader(String headerName, String headerValue)
            {
                Assert.fail("AddHeader should not be called.");
            }
        };

        final String returnedValue = AtlassianSessionIdUtil.generateAtlassianSessionHash(mockRequest, mockResponse);

        assertNull(returnedValue);

        mockSession.verify();
        mockRequest.verify();
        mockResponse.verify();
    }

    private MockHttpServletRequest buildMockRequest(final HttpSession session, final String contextPath)
    {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest()
        {
            @Override
            public HttpSession getSession(boolean createSession)
            {
                return session;
            }
        };

        mockRequest.setupGetContextPath(contextPath);

        return mockRequest;
    }

    private static class MockHttpServletRequest extends com.mockobjects.servlet.MockHttpServletRequest
    {
        public int getRemotePort()
        {
            return 0;
        }

        public String getLocalName()
        {
            return null;
        }

        public String getLocalAddr()
        {
            return null;
        }

        public int getLocalPort()
        {
            return 0;
        }
    }

    private static class MockHttpServletResponse extends com.mockobjects.servlet.MockHttpServletResponse
    {
        public String getContentType()
        {
            return null;
        }

        public void setCharacterEncoding(final String s)
        {
        }
    }
}
