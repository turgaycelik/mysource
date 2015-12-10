package com.atlassian.jira.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit Test for UnsupportedBrowserManager
 *
 * @since v4.2
 */
@RunWith (MockitoJUnitRunner.class)
public class TestUnsupportedBrowserManager
{
    public static final String IE6_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)";
    public static final String IE7_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; GTB6.4; .NET CLR 1.1.4322; FDM; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)";
    public static final String IE8_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0)";
    public static final String FF36_USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2.6) Gecko/20100625 Firefox/3.6.28";
    public static final String FF_USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2.6) Gecko/20100625 Firefox/16.0.1";

    private static final String FF36_NOT_SUPPORTED_KEY = "browser.ff36.nosupport";
    private static final String IE6_NOT_SUPPORTED_KEY = "browser.ie6.nosupport";
    private static final String IE7_NOT_SUPPORTED_KEY = "browser.ie7.nosupport";
    private static final String IE8_NOT_SUPPORTED_KEY = "browser.ie8.nosupport";

    @Mock
    private ApplicationProperties applicationProperties;

    private UnsupportedBrowserManager unsupportedBrowserManager;

    @Before
    public void setUp()
    {
        unsupportedBrowserManager = new UnsupportedBrowserManager(applicationProperties);
    }

    @Test
    public void ie6IsUnsupported()
    {
        final HttpServletRequest ie6Request = mockRequestWithUserAgent(IE6_USER_AGENT);
        assertIsUnsupported(ie6Request, IE6_NOT_SUPPORTED_KEY);

        UserAgentUtil.Browser ie6Browser = new UserAgentUtil.Browser(
                UserAgentUtil.BrowserFamily.MSIE,
                UserAgentUtil.BrowserMajorVersion.MSIE6,
                "MSIE6.0"
        );
        assertIsUnsupported(ie6Browser, IE6_NOT_SUPPORTED_KEY);
    }

    @Test
    public void ie7IsUnsupported()
    {
        final HttpServletRequest ie7Request = mockRequestWithUserAgent(IE7_USER_AGENT);
        assertIsUnsupported(ie7Request, IE7_NOT_SUPPORTED_KEY);

        UserAgentUtil.Browser ie7Browser = new UserAgentUtil.Browser(
                UserAgentUtil.BrowserFamily.MSIE,
                UserAgentUtil.BrowserMajorVersion.MSIE7,
                "MSIE7.0"
        );
        assertIsUnsupported(ie7Browser, IE7_NOT_SUPPORTED_KEY);
    }

    @Test
    public void ie8IsUnsupported()
    {
        final HttpServletRequest ie8Request = mockRequestWithUserAgent(IE8_USER_AGENT);
        assertIsUnsupported(ie8Request, IE8_NOT_SUPPORTED_KEY);

        UserAgentUtil.Browser ie8Browser = new UserAgentUtil.Browser(
                UserAgentUtil.BrowserFamily.MSIE,
                UserAgentUtil.BrowserMajorVersion.MSIE8,
                "MSIE8.0"
        );
        assertIsUnsupported(ie8Browser, IE8_NOT_SUPPORTED_KEY);
    }

    @Test
    public void firefox36IsUnsupported()
    {
        final HttpServletRequest firefox36Request = mockRequestWithUserAgent(FF36_USER_AGENT);
        assertIsUnsupported(firefox36Request, FF36_NOT_SUPPORTED_KEY);

        UserAgentUtil.Browser firefox36Browser = new UserAgentUtil.Browser(
                UserAgentUtil.BrowserFamily.FIREFOX,
                UserAgentUtil.BrowserMajorVersion.FIREFOX36,
                "Firefox3.6.28"
        );
        assertIsUnsupported(firefox36Browser, FF36_NOT_SUPPORTED_KEY);
    }

    @Test
    public void firefoxIsSupported()
    {
        final HttpServletRequest firefoxRequest = mockRequestWithUserAgent(FF_USER_AGENT);
        assertIsSupported(firefoxRequest);

        UserAgentUtil.Browser firefoxBrowser = new UserAgentUtil.Browser(
                UserAgentUtil.BrowserFamily.FIREFOX,
                UserAgentUtil.BrowserMajorVersion.FIREFOX_UNKNOWN,
                "Firefox16.0.1"
        );
        assertIsSupported(firefoxBrowser);
    }

    @Test
    public void testApplicationPropertiesToggle()
    {
        when(applicationProperties.getDefaultString(APKeys.JIRA_BROWSER_UNSUPPORTED_WARNINGS_DISABLED)).thenReturn("true");

        assertFalse("Unsupported Browser warnings should be disabled", unsupportedBrowserManager.isCheckEnabled());
    }

    @Test
    public void testCookieHandling()
    {
        final HttpServletRequest requestWithCookies = mockRequestWithCookies(new Cookie("UNSUPPORTED_BROWSER_WARNING", "Handled"));
        assertTrue("Cookie should be present", unsupportedBrowserManager.isHandledCookiePresent(requestWithCookies));

        final HttpServletRequest requestWithNoCookies = mockRequestWithCookies();
        assertFalse("Cookie should be absent", unsupportedBrowserManager.isHandledCookiePresent(requestWithNoCookies));
    }

    private HttpServletRequest mockRequestWithUserAgent(String userAgent)
    {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("USER-AGENT")).thenReturn(userAgent);
        return request;
    }

    private HttpServletRequest mockRequestWithCookies(Cookie... cookies)
    {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(cookies);
        return request;
    }

    private void assertIsUnsupported(HttpServletRequest request, String expectedMessageKey)
    {
        assertTrue("Browser on request should have been flagged as unsupported", unsupportedBrowserManager.isUnsupportedBrowser(request));
        assertEquals(expectedMessageKey, unsupportedBrowserManager.getMessageKey(request));
    }

    private void assertIsUnsupported(UserAgentUtil.Browser browser, String expectedMessageKey)
    {
        assertTrue("Browser should have been flagged as unsupported", unsupportedBrowserManager.isUnsupportedBrowser(browser));
        assertEquals(expectedMessageKey, unsupportedBrowserManager.getMessageKey(browser));
    }

    private void assertIsSupported(HttpServletRequest request)
    {
        assertFalse("Browser on request should have been flaged as supported", unsupportedBrowserManager.isUnsupportedBrowser(request));
        assertNull("There should be no message on for the request", unsupportedBrowserManager.getMessageKey(request));
    }

    private void assertIsSupported(UserAgentUtil.Browser browser)
    {
        assertFalse("Browser on request should have been flaged as supported", unsupportedBrowserManager.isUnsupportedBrowser(browser));
        assertNull("There should be no message on for the request", unsupportedBrowserManager.getMessageKey(browser));
    }
}
