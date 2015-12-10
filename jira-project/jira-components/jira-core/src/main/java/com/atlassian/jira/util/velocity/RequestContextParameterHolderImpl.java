package com.atlassian.jira.util.velocity;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the {@link com.atlassian.jira.util.velocity.RequestContextParameterHolder}.
 * Parameters will return null if the HttpRequest used to construct this class is null.
 */
public class RequestContextParameterHolderImpl implements RequestContextParameterHolder
{
    private final String servletPath;
    private final StringBuffer requestURL;
    private final String queryString;
    private final Map parameterMap;


    public RequestContextParameterHolderImpl(HttpServletRequest request)
    {
        if (request != null)
        {
            this.servletPath = request.getServletPath();
            this.requestURL = request.getRequestURL();
            this.queryString = request.getQueryString();
            this.parameterMap = Collections.unmodifiableMap(new HashMap(request.getParameterMap()));
        }
        else
        {
            servletPath = queryString = null;
            requestURL = null;
            parameterMap = null;
        }
    }

    public String getServletPath()
    {
        return servletPath;
    }

    public String getRequestURL()
    {
        if (requestURL != null)
        {
            return requestURL.toString();
        }
        return null;
    }

    public String getQueryString()
    {
        return queryString;
    }

    public Map getParameterMap()
    {
        return parameterMap;
    }
}
