package com.atlassian.jira.web.filters.accesslog;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * This can give back a IP address String that take into account proxies and X-Forwarded-For etc...
 */
public class AccessLogIPAddressUtil
{
    /**
     * This will return a set of IP addresses that MAY include PROXY forwarded addresses
     *
     * @param httpServletRequest the request in play
     * @return an address in the format nnn.nnn.nnn.nnn [, nnn.nnn.nnn.nnn ...]
     */
    public static String getRemoteAddr(HttpServletRequest httpServletRequest)
    {
        // if we have come through a proxy like Apache then this may be set.  We use that instead in this case.
        final String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        final String remoteAddr = httpServletRequest.getRemoteAddr();
        if (StringUtils.isBlank(xForwardedFor) && StringUtils.isBlank(remoteAddr))
        {
            return null;
        }
        if (StringUtils.isBlank(xForwardedFor))
        {
            return removeSpaces(remoteAddr);
        }
        return removeSpaces(new StringBuilder(xForwardedFor).append(",").append(remoteAddr).toString());
    }

    /**
     * This will shorten the Request URL into something that is more acceptable from a logging point of view
     *
     * @param httpServletRequest the request in play
     * @return the request URL shortened down to remove hostname and context
     */
    public static String getShortenedRequestUrl(final HttpServletRequest httpServletRequest)
    {
        String requestURL = StringUtils.trimToEmpty(httpServletRequest.getRequestURL().toString());
        requestURL = removeHostName(requestURL);
        requestURL = removeContextPath(httpServletRequest, requestURL);
        return requestURL;
    }

    private static String removeHostName(final String requestURL)
    {
        int httpIndex = requestURL.indexOf("http://");
        int httpsIndex = requestURL.indexOf("https://");
        if (httpIndex == 0)
        {
            int slashIndex = requestURL.indexOf('/', httpIndex + 7);
            if (slashIndex != -1)
            {
                return requestURL.substring(slashIndex);
            }
        }
        else if (httpsIndex == 0)
        {
            int slashIndex = requestURL.indexOf('/', httpsIndex + 8);
            if (slashIndex != -1)
            {
                return requestURL.substring(slashIndex);
            }
        }
        return requestURL;
    }


    private static String removeContextPath(final HttpServletRequest httpServletRequest, String requestURL)
    {
        final String contextPath = StringUtils.trimToEmpty(httpServletRequest.getContextPath());
        final int index = requestURL.indexOf(contextPath);
        if (index == 0)
        {
            requestURL = requestURL.substring(index + contextPath.length());
        }
        return requestURL;
    }

    private static String removeSpaces(final String s)
    {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray())
        {
            if (!Character.isWhitespace(c))
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
