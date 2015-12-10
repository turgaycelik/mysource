package com.atlassian.jira.util.http;

import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Simple helper class to help create a well-formed URLs. Also gives us a central point to do any encoding related
 * stuff
 */
public class JiraUrl extends HttpURL
{
    private static final int HTTP_DEFAULT_PORT = 80;
    private static final int HTTPS_DEFAULT_PORT = 443;
    private static final String HTTP_SCHEME = "http";
    private static final String HTTPS_SCHEME = "https";

    public JiraUrl(final String baseUrl) throws URIException
    {
        super(baseUrl);
    }

    /**
     * Sets the query string from the list of {@link NameValuePair} objects. Both name & value are encoded in the
     * default encoding. Null or empty list will clear the query string.
     */
    public void setQuery(final List<NameValuePair> nameValuePairs) throws URIException
    {
        if ((nameValuePairs == null) || nameValuePairs.isEmpty())
        {
            super.setQuery(null);
        }
        else
        {
            final String[] names = new String[nameValuePairs.size()];
            final String[] values = new String[nameValuePairs.size()];
            for (int i = 0; i < nameValuePairs.size(); i++)
            {
                final NameValuePair nvp = nameValuePairs.get(i);
                names[i] = nvp.getName();
                values[i] = nvp.getValue();
            }

            super.setQuery(names, values);
        }
    }

    /**
     * Attempts to construct a baseUrl using the {@link HttpServletRequest} object.
     *
     * @param request The incoming http request
     * @return a string containing the absolute base URL
     */
    public static String constructBaseUrl(final HttpServletRequest request)
    {
        final StringBuilder sb = new StringBuilder();
        final String scheme = request.getScheme();
        sb.append(scheme);
        sb.append("://");
        sb.append(request.getServerName());
        final int port = request.getServerPort();
        if (!isStandardPort(scheme, port))
        {
            sb.append(":");
            sb.append(port);
        }
        sb.append(request.getContextPath());
        return sb.toString();
    }

    private static boolean isStandardPort(String scheme, int port)
    {
        if (scheme.equalsIgnoreCase(HTTP_SCHEME) && port == HTTP_DEFAULT_PORT)
        {
            return true;
        }
        if (scheme.equalsIgnoreCase(HTTPS_SCHEME) && port == HTTPS_DEFAULT_PORT)
        {
            return true;
        }
        return false;
    }

    /**
     * Extracts the action (e.g. AdminSummary.jspa) from a URL
     *
     * @param url   A URL (or partial URL) like "/secure/AdminSummary.jspa"
     * @return      Returns the original URL if an action is not found
     */
    public static String extractActionFromURL (String url)
    {
        if (url.contains(".jspa"))
        {
            url = url.substring(url.lastIndexOf("/"));
        }

        // ignore parameters
        if (url.contains("?"))
        {
            url = url.substring(0, url.indexOf("?"));
        }

        return url;
    }
}
