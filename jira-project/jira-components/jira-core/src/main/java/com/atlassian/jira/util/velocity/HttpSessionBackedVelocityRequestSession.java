package com.atlassian.jira.util.velocity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Enumeration;

/**
 * An implementation of the VelocityRequestSession that is backed by a {@link HttpSession}
 * All methods are a direct call through to the underlying HttpSession.
 * This will only create the request if necessary.
 *
 * @since v4.0
 */
public class HttpSessionBackedVelocityRequestSession implements VelocityRequestSession
{
    private final HttpServletRequest request;

    public HttpSessionBackedVelocityRequestSession(HttpServletRequest request)
    {
        this.request = request;
    }

    private HttpSession getSession(boolean create)
    {
        return request.getSession(create);
    }

    public String getId()
    {
        final HttpSession curSession = getSession(true);
        if(curSession == null)
        {
            return null;
        }
        return curSession.getId();
    }

    public Object getAttribute(String s)
    {
        final HttpSession curSession = getSession(false);
        return curSession == null ? null : curSession.getAttribute(s);
    }

    public Enumeration<String> getAttributeNames()
    {
        final HttpSession curSession = getSession(false);
        return curSession == null ? Collections.enumeration(Collections.<String>emptyList()) : curSession.getAttributeNames();
    }

    public void setAttribute(String s, Object o)
    {
        final HttpSession curSession = getSession(true);
        curSession.setAttribute(s, o);
    }

    public void removeAttribute(String s)
    {
        final HttpSession curSession = getSession(false);
        if (curSession != null)
        {
            curSession.removeAttribute(s);
        }
    }

    public void invalidate()
    {
        final HttpSession curSession = getSession(false);
        if (curSession != null)
        {
            curSession.invalidate();
        }
    }
}
