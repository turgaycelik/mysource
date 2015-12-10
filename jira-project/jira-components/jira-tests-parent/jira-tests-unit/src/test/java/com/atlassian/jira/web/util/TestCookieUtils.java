package com.atlassian.jira.web.util;

import java.util.Map;

import javax.servlet.http.Cookie;

import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.mock.servlet.MockHttpServletResponse;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.Iterables;

import org.junit.Test;

import static com.atlassian.jira.matchers.IterableMatchers.emptyIterable;
import static com.atlassian.jira.matchers.IterableMatchers.iterableWithSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests the CookieUtils class.
 */
public class TestCookieUtils
{

    private static final Cookie COOKIE1 = new Cookie("some", "cookie");
    private static final Cookie COOKIE2 = new Cookie("other", "cookie");

    private static final Cookie HAPPY_COOKIE = new Cookie(CookieUtils.JSESSIONID, "foo");
    private static final Cookie ANOTHER_HAPPY_COOKIE = new Cookie(CookieUtils.JSESSIONID, "another happy");

    private static void assertHasSessionId(boolean expectedResult, Cookie[] cookies)
    {
        assertEquals(expectedResult, CookieUtils.hasSessionId(cookies));
    }

    private static void assertGetSingleSessionIdEquals(String expectedResult, Cookie[] cookies)
    {
        assertEquals(expectedResult, CookieUtils.getSingleSessionId(cookies));
    }

    @Test
    public void testHasSessionIdNullCookies()
    {
        assertHasSessionId(false, null);
    }

    @Test
    public void testGetSingleSessionIdNullCookies()
    {
        assertGetSingleSessionIdEquals(null, null);
    }

    @Test
    public void testHasSessionIdNoCookies()
    {
        assertHasSessionId(false, new Cookie[0]);
    }

    @Test
    public void testHasSessionIdNotValidCookie()
    {
        assertHasSessionId(false, new Cookie[]{COOKIE1});
        assertHasSessionId(false, new Cookie[]{COOKIE1, COOKIE2});
    }

    @Test
    public void testHasSessionIdValidCookie()
    {
        assertHasSessionId(true, new Cookie[]{HAPPY_COOKIE});
        assertHasSessionId(true, new Cookie[]{COOKIE1, HAPPY_COOKIE, COOKIE2});
        assertHasSessionId(true, new Cookie[]{HAPPY_COOKIE, HAPPY_COOKIE, COOKIE2});
        assertHasSessionId(true, new Cookie[]{ANOTHER_HAPPY_COOKIE, HAPPY_COOKIE});
    }

    @Test
    public void testCreateSessionCookie()
    {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest().setContextPath("/mycontext");
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        CookieUtils.setSessionCookie(mockRequest, mockResponse, null);
        assertThat(mockResponse.getCookies(), emptyIterable(Cookie.class));

        CookieUtils.setSessionCookie(mockRequest, mockResponse, "sessionId");
        assertThat(mockResponse.getCookies(), iterableWithSize(1, Cookie.class));

        final Cookie cookie = Iterables.get(mockResponse.getCookies(), 0);
        assertEquals(CookieUtils.JSESSIONID, cookie.getName());
        assertEquals("sessionId", cookie.getValue());
        assertEquals("/mycontext", cookie.getPath());

    }

    @Test
    public void testGetSingleSessionIdNoSession()
    {
        assertGetSingleSessionIdEquals(null, new Cookie[0]);
        assertGetSingleSessionIdEquals(null, new Cookie[]{COOKIE1});
        assertGetSingleSessionIdEquals(null, new Cookie[]{COOKIE1, COOKIE2});
    }

    @Test
    public void testGetSingleSessionIdOneSession()
    {
        assertGetSingleSessionIdEquals(HAPPY_COOKIE.getValue(), new Cookie[]{HAPPY_COOKIE});
        assertGetSingleSessionIdEquals(HAPPY_COOKIE.getValue(), new Cookie[]{HAPPY_COOKIE, COOKIE2});
    }

    @Test
    public void testGetSingleSessionIdMultipleSession()
    {
        assertGetSingleSessionIdEquals(null, new Cookie[]{HAPPY_COOKIE, ANOTHER_HAPPY_COOKIE});
        assertGetSingleSessionIdEquals(null, new Cookie[]{HAPPY_COOKIE, ANOTHER_HAPPY_COOKIE, COOKIE1});
    }

    @Test
    public void testGetCookie()
    {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addCookie("testcookie", "testvalue");
        assertEquals("testvalue", CookieUtils.getCookieValue("testcookie", mockRequest));
        assertNull(CookieUtils.getCookieValue("nonexistingcookiename", mockRequest));
    }

    @Test
    public void testCreateCookieWithContextPath()
    {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/some/path");
        Cookie newCookie = CookieUtils.createCookie("name", "value", mockRequest);
        assertEquals("name", newCookie.getName());
        assertEquals("value", newCookie.getValue());
        assertEquals("/some/path", newCookie.getPath());
    }

    @Test
    public void testCreateCookieWithoutContextPath()
    {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath(null);
        Cookie newCookie = CookieUtils.createCookie("name", "value", mockRequest);
        assertEquals("name", newCookie.getName());
        assertEquals("value", newCookie.getValue());
        assertEquals("/", newCookie.getPath());
    }

    @Test
    public void testCreateCookieWithEmptyContextPath()
    {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("");
        Cookie newCookie = CookieUtils.createCookie("name", "value", mockRequest);
        assertEquals("name", newCookie.getName());
        assertEquals("value", newCookie.getValue());
        assertEquals("/", newCookie.getPath());
    }

    @Test
    public void testcreateConglomerateCookieWithOneValue()
    {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Cookie cookie = CookieUtils.createConglomerateCookie("cong", MapBuilder.newBuilder("twixi-blocks", "#comment-13822").toMap(), mockRequest);

        assertEquals("twixi-blocks%3D%23comment-13822", cookie.getValue());
    }

    @Test
    public void testcreateConglomerateCookieWithEmptyValue()
    {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Cookie cookie = CookieUtils.createConglomerateCookie("cong", MapBuilder.newBuilder("twixi-blocks", "").toMap(), mockRequest);

        assertEquals("", cookie.getValue());
    }

    @Test
    public void testcreateConglomerateCookieWithTwoValues()
    {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> map = MapBuilder.newBuilder("twixi-blocks", "#comment-13822").add("#foo", "bar").toLinkedHashMap();
        Cookie cookie = CookieUtils.createConglomerateCookie("cong", map, mockRequest);

        assertEquals("twixi-blocks%3D%23comment-13822|%23foo%3Dbar", cookie.getValue());
    }
    @Test
    public void testParseConglomerateCookieWithNoCookie()
    {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String,String> map = CookieUtils.parseConglomerateCookie("cong", mockRequest);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testParseConglomerateCookieWithEmptyCookie()
    {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addCookie("cong", "");
        Map<String,String> map = CookieUtils.parseConglomerateCookie("cong", mockRequest);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testParseConglomerateCookieWithOneValue()
    {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addCookie("cong", "twixi-blocks%3D%23comment-13822");
        Map<String,String> map = CookieUtils.parseConglomerateCookie("cong", mockRequest);
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("#comment-13822", map.get("twixi-blocks"));
    }

    @Test
    public void testParseConglomerateCookieWithTwoValue()
    {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addCookie("cong", "twixi-blocks%3D%23comment-13822|%23foo%3dbar");
        Map<String,String> map = CookieUtils.parseConglomerateCookie("cong", mockRequest);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("#comment-13822", map.get("twixi-blocks"));
        assertEquals("bar", map.get("#foo"));
    }
}
