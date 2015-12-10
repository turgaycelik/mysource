package com.atlassian.sal.jira.web.context;

import com.atlassian.jira.web.ExecutingHttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * JIRA SAL implementation of the HttpContext, this implementation delegates to the JIRA internal {@link
 * com.atlassian.jira.web.ExecutingHttpRequest} class.
 *
 * @since v6.0
 */
public class HttpContext implements com.atlassian.sal.api.web.context.HttpContext
{
    /**
     * @return HttpServletRequest
     * @see com.atlassian.sal.api.web.context.HttpContext#getRequest()
     */
    @Override
    public HttpServletRequest getRequest()
    {
        return ExecutingHttpRequest.get();
    }

    /**
     * @return HttpServletResponse
     * @see com.atlassian.sal.api.web.context.HttpContext#getResponse()
     */
    @Override
    public HttpServletResponse getResponse()
    {
        return ExecutingHttpRequest.getResponse();
    }

    /**
     * @param create should be <tt>true</tt> to create a new session for the active request or <tt>false</tt> to return
     * <tt>null</tt> if there is no current session
     * @return the HttpSession associated with this request or <tt>null</tt> if <tt>create</tt> is false and the request
     *         has no session, or if there is no active request
     * @see com.atlassian.sal.api.web.context.HttpContext#getSession(boolean)
     */
    @Override
    public HttpSession getSession(boolean create)
    {
        HttpServletRequest request = getRequest();

        return request == null ? null : request.getSession(create);
    }
}
