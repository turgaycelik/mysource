package com.atlassian.jira.web.component.webfragment;

import com.atlassian.jira.web.tags.LoginLink;

import javax.servlet.http.HttpServletRequest;

/**
 * This file provides specific context information for the system-user-nav-bar.vm template
 */
public class UserNavContextLayoutBean implements ContextLayoutBean
{
    private final HttpServletRequest request;

    public UserNavContextLayoutBean(HttpServletRequest request)
    {
        this.request = request;
    }

    public String getLoginLink(String textToLink)
    {
        return new LoginLink().getLoginLink(request, textToLink);
    }

    public String getLoginLink()
    {
        return new LoginLink().getLoginLink(request);
    }
}
