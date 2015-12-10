package com.atlassian.jira.web;

import com.atlassian.sal.api.web.context.HttpContext;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * An injectable alternative to ExecutingHttpRequest
 *
 * @since v6.0
 */
public class HttpServletVariablesImpl implements HttpServletVariables, HttpContext
{
    @Override
    public HttpServletRequest getHttpRequest()
    {
       return assertInsideHttp(ExecutingHttpRequest.get());
    }

    @Override
    public HttpSession getHttpSession()
    {
        return getHttpRequest().getSession();
    }

    @Override
    public HttpServletResponse getHttpResponse()
    {
        return assertInsideHttp(ExecutingHttpRequest.getResponse());
    }

    @Override
    public ServletContext getServletContext()
    {
        return ServletContextProvider.getServletContext();
    }

    // SAL HttpContext

    @Nullable
    @Override
    public HttpServletRequest getRequest()
    {
        return getHttpRequest();
    }

    @Nullable
    @Override
    public HttpServletResponse getResponse()
    {
        return getHttpResponse();
    }

    @Nullable
    @Override
    public HttpSession getSession(final boolean create)
    {
        return getHttpRequest().getSession(create);
    }

    private <T> T assertInsideHttp(final T httpObj)
    {
        if (httpObj == null) {
            throw new IllegalStateException("You must be inside a HTTP request thread to call on the HttpServletVariables component");
        }
        return httpObj;
    }
}
