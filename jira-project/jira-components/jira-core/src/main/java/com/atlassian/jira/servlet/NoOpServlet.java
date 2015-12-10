package com.atlassian.jira.servlet;

import com.atlassian.core.servlet.AbstractNoOpServlet;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * NoOpServlet is a dummy servlet used only to provide a servlet mapping for url patterns that dont have any. This is
 * necessary as some application servers like WebSphere 6.1.0.5 returns a 404 if there are no mapped servlet before
 * applying filters to the request which could potentially change the URL mapped to a valid servlet. For example, the
 * URLRewriter filter does this. Hence this dummy servlet should never handle any requests.
 * <p/>
 * If this servlet receives a request, it will simply log all relevant information from the request that may be of help
 * in determining why the request was received, as this would not be the desired result.
 */
public class NoOpServlet extends AbstractNoOpServlet
{
    protected String getUserName(HttpServletRequest request)
    {
        HttpSession httpSession = request.getSession(false);//dont create the session if there isnt one already
        if (httpSession != null)
        {

            User remoteUser = ComponentAccessor.getComponent(JiraAuthenticationContext.class).getLoggedInUser();
            if (remoteUser != null)
            {
                return remoteUser.getName();
            }
        }
        return null;
    }
}
