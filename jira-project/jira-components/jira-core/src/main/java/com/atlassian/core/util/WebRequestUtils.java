package com.atlassian.core.util;

import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;

/**
 * This was taken from atlassian-webwork1 and placed into its now rightful home of JIRA.
 *
 * @since 4.3
 */
public class WebRequestUtils
{
    public static final int OTHER = 0;

    public static boolean isGoodBrowser(HttpServletRequest request)
    {
        String userAgent = request.getHeader("USER-AGENT");

        if (userAgent == null || (userAgent.indexOf("MSIE") == -1 && userAgent.indexOf("Mozilla/5") == -1 && userAgent.indexOf("Opera") == -1))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public static final int OPERA = 10;

    public static int getBrowser(HttpServletRequest request)
    {
        String userAgent = request.getHeader("USER-AGENT");

        if (userAgent == null)
        {
            return OTHER;
        }
        else if (userAgent.indexOf("Opera") != -1)
        {
            return OPERA;
        }
        else
        {
            return OTHER;
        }
    }

    public static final int WINDOWS = 10;
    public static final int MACOSX = 20;
    public static final int LINUX = 30;

    public static int getBrowserOperationSystem(HttpServletRequest request)
    {
        String userAgent = request != null ? request.getHeader("USER-AGENT") : null;

        if (userAgent != null)
        {
             userAgent = userAgent.toLowerCase();
            if ((userAgent.indexOf("windows") != -1))
            {
                return WINDOWS;
            }
            else if (userAgent.indexOf("mac os x") != -1)
            {
                return MACOSX;
            }
            else if (userAgent.indexOf("linux") != -1)
            {
                return LINUX;
            }
            else
            {
                return OTHER;
            }
        }
        else
        {
            return OTHER;
        }
    }

    /**
     * Returns the modifier key appropriate for this request (used for access keys)
     *
     * @return Key or key combinations e.g. "Alt" or "Ctrl"
     */
    public static String getModifierKey()
    {
        HttpServletRequest request = ActionContext.getRequest();
        if (request != null)
        {
            String userAgent = org.apache.commons.lang.StringUtils.lowerCase(request.getHeader("USER-AGENT"));
            if (StringUtils.contains(userAgent, "safari") || StringUtils.contains(userAgent, "mac"))
            {
                return "Ctrl";
            }
            else if (StringUtils.contains(userAgent, "opera"))
            {
                return "Shift + Esc";
            }
        }

        return "Alt";
    }

}