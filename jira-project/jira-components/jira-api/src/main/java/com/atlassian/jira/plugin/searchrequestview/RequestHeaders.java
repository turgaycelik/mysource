package com.atlassian.jira.plugin.searchrequestview;

import com.atlassian.annotations.PublicApi;

/**
 * This is a subset of the HttpServletResponse that just deals with setting headers.
 *
 * @see javax.servlet.http.HttpServletResponse
 */
@PublicApi
public interface RequestHeaders
{
    /**
     * Sets a response header with the given name and date-value.
     * The date is specified in terms of milliseconds since the epoch.
     * If the header had already been set, the new value overwrites the previous one.
     *
     * @param name the name of the header to set
     * @param date the assigned date value
     *
     * @see #addDateHeader(String, long)
     */
    void setDateHeader(String name, long date);

    /**
     * Adds a response header with the given name and
     * date-value.  The date is specified in terms of
     * milliseconds since the epoch.  This method allows response headers
     * to have multiple values.
     *
     * @param name the name of the header to set
     * @param date the additional date value
     *
     * @see #setDateHeader(String, long)
     */
    void addDateHeader(String name, long date);

    /**
     * Sets a response header with the given name and value.
     * <p>
     * If the header had already been set, the new value overwrites the previous one.
     *
     * @param name the name of the header
     * @param value the header value
     *
     * @see #addHeader(String, String)
     */
    void setHeader(String name, String value);

    /**
     * Adds a response header with the given name and value.
     * <p>
     * This method allows response headers to have multiple values.
     *
     * @param name the name of the header
     * @param value the additional header value
     *
     * @see #setHeader(String, String)
     */
    void addHeader(String name, String value);

    /**
     * Sets a response header with the given name and
     * integer value.  If the header had already been set, the new value
     * overwrites the previous one.  The <code>containsHeader</code>
     * method can be used to test for the presence of a header before
     * setting its value.
     *
     * @param name the name of the header
     * @param value the assigned integer value
     *
     * @see #addIntHeader(String, int)
     */
    void setIntHeader(String name, int value);

    /**
     * Adds a response header with the given name and
     * integer value.  This method allows response headers to have multiple
     * values.
     *
     * @param name the name of the header
     * @param value the assigned integer value
     *
     * @see #setIntHeader(String, int)
     */
    void addIntHeader(String name, int value);

}
