package com.atlassian.jira.util.velocity;

import java.util.Map;

/**
 * Holder for various parameters stored in the HTTPRequest.  This may be used to
 * access certain request parameters 'safely' (i.e.: if the HttpRequest cannot be
 * accessed directly).
 */
public interface RequestContextParameterHolder
{
    /**
     * Returns the same as {@link javax.servlet.http.HttpServletRequest#getServletPath()}
     *
     * @return ServletPath
     */
    String getServletPath();

    /**
     * Returns the same as {@link javax.servlet.http.HttpServletRequest#getRequestURL()} )}
     *
     * @return requestURL
     */
    String getRequestURL();

    /**
     * Returns the same as {@link javax.servlet.http.HttpServletRequest#getQueryString()} )}
     *
     * @return queryString
     */
    String getQueryString();

    /**
     * Returns the same as {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
     *
     * @return the same as {@link javax.servlet.http.HttpServletRequest#getParameterMap()}, never null, unmodifiable
     * @since v3.10
     */
    Map getParameterMap();
}
