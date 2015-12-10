package com.atlassian.jira.util.http;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.plugin.searchrequestview.HttpRequestHeaders;
import com.atlassian.jira.plugin.searchrequestview.RequestHeaders;

import javax.servlet.http.HttpServletResponse;

/**
 * Utility methods for HTTP-level operations
 *
 * @since v3.12.3
 */
@PublicApi
public class JiraHttpUtils
{
    /**
     * Sets the Cache-Control, Pragma and Expires headers on a response so that the response content will not be cached
     *
     * @param response the response object
     */
    public static void setNoCacheHeaders(HttpServletResponse response)
    {
        setNoCacheHeaders(new HttpRequestHeaders(response));
    }

    /**
     * Sets the Cache-Control, Pragma and Expires headers on a response so that the response content will not be cached
     * Note: this is a convenience method for existing SearchRequestView code
     *
     * @param requestHeaders wrapper object around a response (used in SearchRequestViews)
     */
    public static void setNoCacheHeaders(RequestHeaders requestHeaders)
    {
        requestHeaders.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        requestHeaders.setHeader("Pragma", "no-cache");
        requestHeaders.setDateHeader("Expires", -1);
    }
}
