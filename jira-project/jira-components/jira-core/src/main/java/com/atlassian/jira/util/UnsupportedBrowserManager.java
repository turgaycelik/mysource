package com.atlassian.jira.util;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.collect.MapBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Contains a map of unsupported browsers and error message keys. It also provides helper methods to access and manipulate this map.
 *
 * @since v4.2
 */
public class UnsupportedBrowserManager
{
    private ApplicationProperties properties;

    private volatile Map<UserAgentUtil.Browser, String> unsupportedBrowsers;


    public UnsupportedBrowserManager(ApplicationProperties properties)
    {
        this.properties = properties;
        unsupportedBrowsers = MapBuilder.newBuilder(
                new UserAgentUtil.Browser(UserAgentUtil.BrowserFamily.FIREFOX, UserAgentUtil.BrowserMajorVersion.FIREFOX36, "*"),
                "browser.ff36.nosupport",
                new UserAgentUtil.Browser(UserAgentUtil.BrowserFamily.MSIE, UserAgentUtil.BrowserMajorVersion.MSIE6, "MSIE6.0"),
                "browser.ie6.nosupport",
                new UserAgentUtil.Browser(UserAgentUtil.BrowserFamily.MSIE, UserAgentUtil.BrowserMajorVersion.MSIE7, "MSIE7.0"),
                "browser.ie7.nosupport",
                new UserAgentUtil.Browser(UserAgentUtil.BrowserFamily.MSIE, UserAgentUtil.BrowserMajorVersion.MSIE8, "MSIE8.0"),
                "browser.ie8.nosupport"
        ).toMap();
    }

    /**
     * @param browser The {@link UserAgentUtil.Browser} that is to tested
     * @return Whether the browser is unsupported
     */
    public boolean isUnsupportedBrowser(UserAgentUtil.Browser browser)
    {
        return getMessageKey(browser) != null;
    }

    /**
     * @param request  The HttpServletRequest to extract the user agent from
     * @return whether the browser retrieved from the user agent in the current request is unsupported
     */
    public boolean isUnsupportedBrowser(HttpServletRequest request)
    {
       return isUnsupportedBrowser(getBrowser(request));
    }


    /**
     * @param browser  The {@link UserAgentUtil.Browser}
     * @return the message key stored in the map for a specific unsupported browser, null if the browser is supported
     */
    public String getMessageKey(UserAgentUtil.Browser browser)
    {
        String message = unsupportedBrowsers.get(browser);
        return (message != null) ? message : unsupportedBrowsers.get(ignoreMinorVersion(browser));
    }

    /**
     * @param request  The HttpServletRequest to extract the user agent from
     * @return the message key stored in the map for the browser retrieved from the user agent in the current request, null
     * if the client browser is supported
     */
    public String getMessageKey(HttpServletRequest request)
    {
        return getMessageKey(getBrowser(request));
    }


    /**
     * @return true if the jira.browser.unsupported.warnings.disabled property is false
     */
    public boolean isCheckEnabled()
    {
        return "false".equals(properties.getDefaultString(APKeys.JIRA_BROWSER_UNSUPPORTED_WARNINGS_DISABLED));
    }

    /**
     * @param request  The HttpServletRequest to extract the cookie from
     * @return whether the client has saved an UNSUPPORTED_BROWSER_WARNING cookie in the current request headers
     */
    public boolean isHandledCookiePresent(HttpServletRequest request)
    {
        if (request != null) {
            if (request.getCookies()!=null) {
                for (Cookie cookie : request.getCookies()) {
                     if (cookie.getName().equals("UNSUPPORTED_BROWSER_WARNING")) {
                         return true;
                     }
                }
            }
        }
        return false;
    }

    /**
     * @param request  The HttpServletRequest to extract the user agent from
     * @return The {@link UserAgentUtil.Browser} that is deduced from the user agent header in the request
     */
    public UserAgentUtil.Browser getBrowser(HttpServletRequest request)
    {
        if (request != null)
        {
            final String userAgent = request.getHeader(BrowserUtils.USER_AGENT_HEADER);
            final UserAgentUtil userAgentUtil = new UserAgentUtilImpl();
            return userAgentUtil.getUserAgentInfo(userAgent).getBrowser();
        }
        return null;
    }

    private UserAgentUtil.Browser ignoreMinorVersion(UserAgentUtil.Browser browser)
    {
        return new UserAgentUtil.Browser(browser.getBrowserFamily(), browser.getBrowserMajorVersion(), "*");
    }
}
