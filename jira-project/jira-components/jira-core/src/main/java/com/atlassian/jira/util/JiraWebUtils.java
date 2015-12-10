package com.atlassian.jira.util;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 *  
 */
public class JiraWebUtils
{
    /**
     * Return the HttpRequest, but overriding the getRequestURI() method to call back to webwork
     * if needed.  This is because in some application servers, getRequestURI() returns the 'view' page,
     * which is almost always not what we need.
     */
    public static HttpServletRequest getHttpRequest()
    {
        return new HttpServletRequestWrapper(ActionContext.getRequest())
        {
            public String getRequestURI()
            {
                String requestURI = (String) getAttribute("webwork.request_uri");
                if (requestURI == null)
                {
                    requestURI = super.getRequestURI();
                }

                /**
                 * Websphere returns may return requestURIs with query strings, which just ain't right. Strip it away if the case
                 */
                if (StringUtils.contains(requestURI, '?'))
                {
                    return StringUtils.substringBefore(requestURI, "?");
                }
                else
                {
                    return requestURI;
                }
            }

            public String getContextPath()
            {
                try
                {
                    return super.getContextPath();
                }
                catch (NullPointerException e) // Resin throws a NullPointerException in some cases - JRA-11038
                {
                    return new DefaultVelocityRequestContextFactory(ComponentAccessor.getApplicationProperties()).getJiraVelocityRequestContext().getBaseUrl();
                }
            }
        };
    }
}
