package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.util.UserAgentUtil;
import com.atlassian.jira.util.UserAgentUtilImpl;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Map;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.4
 */
public class IsBrowserCondition implements Condition
{
    public static final String USER_AGENT_HEADER = "USER-AGENT";
    private static final String BROWSER = "browser";
    private static final String LESS_THAN = "lessThan";
    private static final String GREATER_THAN = "greaterThan";
    private static final String VERSION = "version";
    private String browser;
    private int lessThanVersion = -1;
    private int greaterThanVersion = -1;
    private int version = -1;

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        if (params.containsKey(BROWSER))
        {
            browser = params.get(BROWSER);
        }
        else
        {
            throw new PluginParseException("Must specify browser");
        }
        if (params.containsKey(LESS_THAN))
        {
            lessThanVersion = Integer.parseInt(params.get(LESS_THAN));
        }
        if (params.containsKey(GREATER_THAN))
        {
            greaterThanVersion = Integer.parseInt(params.get(VERSION));
        }
        if (params.containsKey(VERSION))
        {
            version = Integer.parseInt(params.get(VERSION));
        }
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        UserAgentUtil userAgentUtil = new UserAgentUtilImpl();
        final HttpServletRequest request = ExecutingHttpRequest.get();

        final String userAgent = request.getHeader(USER_AGENT_HEADER);

        final UserAgentUtil.UserAgent userAgentInfo = userAgentUtil.getUserAgentInfo(userAgent);
        final UserAgentUtil.Browser userBrowser = userAgentInfo.getBrowser();
        final UserAgentUtil.BrowserFamily browserFamily = userBrowser.getBrowserFamily();
        final UserAgentUtil.BrowserFamily browserToTest = UserAgentUtil.BrowserFamily.valueOf(browser.toUpperCase(Locale.ENGLISH));

        if (browserToTest == browserFamily)
        {
            final UserAgentUtil.BrowserMajorVersion userVersion = userBrowser.getBrowserMajorVersion();
            if (version != -1)
            {
                final UserAgentUtil.BrowserMajorVersion versionToTest = UserAgentUtil.BrowserMajorVersion.valueOf(browserToTest.toString() + version);
                return userVersion == versionToTest;

            }
            if (lessThanVersion != -1)
            {
                final UserAgentUtil.BrowserMajorVersion versionToTest = UserAgentUtil.BrowserMajorVersion.valueOf(browserToTest.toString() + lessThanVersion);
                return userVersion.compareTo(versionToTest) < 0;

            }
            if (greaterThanVersion != -1)
            {
                final UserAgentUtil.BrowserMajorVersion versionToTest = UserAgentUtil.BrowserMajorVersion.valueOf(browserToTest.toString() + greaterThanVersion);
                return userVersion.compareTo(versionToTest) > 0;

            }
            return true;
        }

        return false;
    }
}
