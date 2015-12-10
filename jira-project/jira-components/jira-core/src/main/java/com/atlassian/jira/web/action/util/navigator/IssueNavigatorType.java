package com.atlassian.jira.web.action.util.navigator;

import com.atlassian.jira.web.SessionKeys;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Represents the tab's on the issue navigator whose status needs to be saved into the session.
 *
 * @since v4.0
 */
public enum IssueNavigatorType
{
    SIMPLE, ADVANCED;

    public String getModeName()
    {
        return name().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Return the tab currently stored in the session. Null will be returned to indicate that there was no tab
     * in the session.
     *
     * @param request the request the tab cookie is stored in.
     * @return the tab in the session, or no tab if there was none.
     * @throws IllegalArgumentException if the passed session is null.
     */
    public static IssueNavigatorType getFromCookie(final HttpServletRequest request)
    {
        if (request != null)
        {
            final Cookie cookie = getNavigatorTypeCookie(request);
            if (cookie != null)
            {
                return getTypeFromString(cookie.getValue());
            }
        }
        return IssueNavigatorType.SIMPLE;
    }

    /**
     * Store the passed tab in the passed session.
     *
     * @param response the response to store cookie tab in. May not be null.
     * @param type the tab to store in the cookie.
     * @throws IllegalArgumentException if either session or tab is null.
     */
    public static void setInCookie(final HttpServletResponse response, final IssueNavigatorType type)
    {
        notNull("type", type);
        if (response != null)
        {
            final Cookie cookie = new Cookie(SessionKeys.ISSUE_NAVIGATOR_TYPE, type.name());
            cookie.setMaxAge(60 * 60 * 24 * 365 * 10);
            response.addCookie(cookie);
        }
    }

    /**
     * Remove any tab session state from the session.
     *
     * @param request the request to remove tab state cookie from.
     * @throws IllegalArgumentException if the session is null.
     */
    public static void clearCookie(final HttpServletRequest request)
    {
        if (request != null)
        {
            final Cookie cookie = getNavigatorTypeCookie(request);
            if (cookie != null)
            {
                cookie.setValue(null);
            }
        }
    }

    private static Cookie getNavigatorTypeCookie(final HttpServletRequest request)
    {
        final Cookie[] cookies = request.getCookies();
        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                if (SessionKeys.ISSUE_NAVIGATOR_TYPE.equals(cookie.getName()))
                {
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * Return the navigator tab for the specified string. The case of the string is ignored when this method is used.
     *
     * @param name the name of the tab to return.
     * @throws IllegalArgumentException if the name is null.
     * @return the tab whose name is the passed string. It will return null if there was no matching tab.
     */
    public static IssueNavigatorType getTypeFromString(final String name)
    {
        notBlank("name", name);
        try
        {
            return IssueNavigatorType.valueOf(name.toUpperCase(Locale.ENGLISH));
        }
        catch (final IllegalArgumentException e)
        {
            return null;
        }
    }
}
