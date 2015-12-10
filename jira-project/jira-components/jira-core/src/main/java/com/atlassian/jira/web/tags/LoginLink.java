/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.tags;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.seraph.util.RedirectUtils;
import webwork.view.taglib.WebWorkBodyTagSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

public class LoginLink extends WebWorkBodyTagSupport
{
    private String returnUrl;
    private static final String OS_DESTINATION_TOKEN = "os_destination=";

    public int doEndTag() throws JspException
    {
        try
        {
            String body = bodyContent.getString();

            bodyContent.clearBody();

            String link = getLoginLink((HttpServletRequest) pageContext.getRequest(), body);
            bodyContent.getEnclosingWriter().write(link);
        }
        catch (IOException e)
        {
            throw new JspException("IOException: " + e);
        }

        return super.doEndTag();
    }

    public String getLoginLink(HttpServletRequest request, String textToLink)
    {
        StringBuilder link = new StringBuilder();

        link.append("<a class=\"lnk\" rel=\"nofollow\" href=\"");
        link.append(getLoginLink(request));
        link.append("\">");
        link.append(textToLink);
        link.append("</a>");

        return link.toString();
    }

    public String getLoginLink(final HttpServletRequest request)
    {
        final String loginUrl = RedirectUtils.getLinkLoginURL(request);
        if (returnUrl != null)
        {
            final Object returnUrlObj = findValue(this.returnUrl);
            if (returnUrlObj != null && returnUrlObj instanceof String)
            {
                final int start = loginUrl.indexOf(OS_DESTINATION_TOKEN);
                if (start != -1)
                {
                    return loginUrl.substring(0, start + OS_DESTINATION_TOKEN.length()) + encodeUrl((String) returnUrlObj);
                }
            }
        }
        // If we have JUST logged out then we dont want any os_destination parameters since logging in from logout should not
        // take you to logout again!
        if (request.getAttribute("jira.logout.page.executed") != null)
        {
            return removeOSDestination(loginUrl);
        }
        return loginUrl;
    }

    private static final Pattern OS_DESTINATION_REGEX = Pattern.compile("[&]*os_destination=[^&]*[&]*");

    private String removeOSDestination(String loginUrl)
    {
        String url = OS_DESTINATION_REGEX.matcher(loginUrl).replaceAll("");
        if (url.endsWith("?"))
        {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public String getReturnUrl()
    {
        return returnUrl;
    }

    public void setReturnUrl(final String returnUrl)
    {
        this.returnUrl = returnUrl;
    }

    private static String encodeUrl(final String url)
    {
        try
        {
            return URLEncoder.encode(url, ComponentAccessor.getApplicationProperties().getEncoding());
        }
        catch (final UnsupportedEncodingException e)
        {
            throw new AssertionError(e);
        }
    }
}
