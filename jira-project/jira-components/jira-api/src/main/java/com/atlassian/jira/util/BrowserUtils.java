package com.atlassian.jira.util;

import com.atlassian.jira.web.ExecutingHttpRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

import static com.atlassian.jira.util.UserAgentUtil.BrowserFamily.IE;
import static com.atlassian.jira.util.UserAgentUtil.BrowserFamily.MSIE;
import static com.atlassian.jira.util.UserAgentUtil.BrowserMajorVersion.FIREFOX2;
import static com.atlassian.jira.util.UserAgentUtil.BrowserMajorVersion.MSIE8;
import static com.atlassian.jira.util.UserAgentUtil.OperatingSystem.OperatingSystemFamily.LINUX;
import static com.atlassian.jira.util.UserAgentUtil.OperatingSystem.OperatingSystemFamily.MAC;
import static com.atlassian.jira.util.UserAgentUtil.OperatingSystem.OperatingSystemFamily.WINDOWS;

public class BrowserUtils
{
    public static final String USER_AGENT_HEADER = "USER-AGENT";

    private static final Logger log = Logger.getLogger(BrowserUtils.class);


    /**
     * Returns the modifier key appropriate for the user's browser and platform (used for access keys)
     *
     * @return Key or key combinations e.g. "Alt" or "Ctrl"
     */
    public static String getModifierKey()
    {
        try
        {
            HttpServletRequest request = ExecutingHttpRequest.get();
            if (request != null)
            {
                return getModifierKey(request.getHeader(USER_AGENT_HEADER));
            }
        }
        catch (RuntimeException rte)
        {
            // really shouldn't happen but we cannot afford any unhandled exceptions like http://jira.atlassian.com/browse/JRA-19508
            log.warn(rte);
        }
        return "Alt";
    }

    static String getModifierKey(String userAgent)
    {
        UserAgentUtil userAgentUtil = new UserAgentUtilImpl();
        final UserAgentUtil.UserAgent userAgentInfo = userAgentUtil.getUserAgentInfo(userAgent);

        final UserAgentUtil.Browser browser = userAgentInfo.getBrowser();
        final UserAgentUtil.OperatingSystem operatingSystem = userAgentInfo.getOperatingSystem();
        switch (browser.getBrowserFamily())
        {
            case MSIE:
            case IE:
                return "Alt";
            case FIREFOX:
            case GECKO:
                if (operatingSystem.getOperatingSystemFamily() == WINDOWS || operatingSystem.getOperatingSystemFamily() == LINUX)
                {
                    if (browser.getBrowserMajorVersion().compareTo(FIREFOX2) < 0)
                    {
                        return "Alt";
                    }
                    else
                    {
                        return "Alt+Shift";
                    }
                }
                else if (operatingSystem.getOperatingSystemFamily() == MAC)
                {
                    return "Ctrl";
                }
                break;
            case OPERA:
                return "Shift+Esc";
            case SAFARI:
                if (operatingSystem.getOperatingSystemFamily() == WINDOWS)
                {
                    return "Alt";
                }
                return "Ctrl+Alt";
            case CHROME:
                if (operatingSystem.getOperatingSystemFamily() == WINDOWS)
                {
                    return "Alt";
                }
                else if (operatingSystem.getOperatingSystemFamily() == MAC)
                {
                    return "Ctrl+Alt";
                }

        }
        return "Alt";
    }

    public static boolean isIe456Or7(String userAgent)
    {
        if (StringUtils.isBlank(userAgent))
        {
            return false;
        }
        UserAgentUtil userAgentUtil = new UserAgentUtilImpl();
        final UserAgentUtil.UserAgent userAgentInfo = userAgentUtil.getUserAgentInfo(userAgent);

        final UserAgentUtil.Browser browser = userAgentInfo.getBrowser();
        return browser.getBrowserFamily() == MSIE && browser.getBrowserMajorVersion().compareTo(MSIE8) < 0;
    }

    /**
     * Returns TRUE if the browser requires a Filter style opacity statement for PNGs
     *
     * <ul>
     * <li> IE 5 upwards on Windows</li>
     * </ul>
     *
     * @param userAgent the user agent header from the request
     * @return true if the browser is IE 5+ on Windows
     */
    public static boolean isFilterBasedPngOpacity(String userAgent)
    {
        if (StringUtils.isBlank(userAgent))
        {
            return false;
        }

        UserAgentUtil userAgentUtil = new UserAgentUtilImpl();
        final UserAgentUtil.UserAgent userAgentInfo = userAgentUtil.getUserAgentInfo(userAgent);

        return userAgentInfo.getOperatingSystem().getOperatingSystemFamily() == WINDOWS
                && (userAgentInfo.getBrowser().getBrowserFamily() == MSIE || userAgentInfo.getBrowser().getBrowserFamily() == IE);
    }
}
