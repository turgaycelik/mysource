/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.issue;

import java.net.MalformedURLException;
import java.net.URL;

public class URLUtil {
    public static String addRequestParameter(String rawUrl, String parameter)
    {
        if (rawUrl == null)
        {
            throw new NullPointerException("Cannot add a url parameter to a null URL");
        }
        if (parameter == null || (parameter.trim().length() == 0))
        {
            return rawUrl;
        }
        String sep = (rawUrl.indexOf('?') > -1) ? "&" : "?";
        return rawUrl + sep + parameter;
    }

    public static String addContextPathToURLIfAbsent(final String contextPath, final String urlOrPartial)
    {
        String iconurlWithContext;
        try
        {
            new URL(urlOrPartial);
            iconurlWithContext = urlOrPartial;
        }
        catch (MalformedURLException e)
        {
            iconurlWithContext = contextPath + urlOrPartial;
        }

        return iconurlWithContext;
    }
}