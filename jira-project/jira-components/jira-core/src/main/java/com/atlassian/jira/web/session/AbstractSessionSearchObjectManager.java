package com.atlassian.jira.web.session;

import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.jira.util.velocity.VelocityRequestSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A very basic implementation of {@link SessionSearchObjectManager} which only allows a single object to be set session-wide.
 * <p/>
 * Developers note: to see our failed attempt at providing an object-per-window implementation, consult the SVN history.
 *
 * @since v4.2
 */
@NonInjectableComponent
public abstract class AbstractSessionSearchObjectManager<T> implements SessionSearchObjectManager<T>
{
    protected final HttpServletRequest request;
    protected final Session session;

    protected AbstractSessionSearchObjectManager(final HttpServletRequest request, final Session session)
    {
        this.request = notNull("request", request);
        this.session = notNull("session", session);
    }

    public T getCurrentObject()
    {
        return getLastViewedObject();
    }

    public void setCurrentObject(final T object)
    {
        setLastViewedObject(object);
    }

    /**
     * @return the key which will be used to store the last viewed object in the session.
     */
    protected abstract String getLastViewedSessionKey();

    /**
     * Returns the last viewed object session-wide.
     *
     * @return the last viewed object; could be null.
     */
    private T getLastViewedObject()
    {
        return (T) session.get(getLastViewedSessionKey());
    }

    /**
     * Set the last viewed object session-wide.
     *
     * @param object the object to set.
     * @see #getLastViewedObject()
     */
    private void setLastViewedObject(final T object)
    {
        session.set(getLastViewedSessionKey(), object);
    }

    /**
     * Simple interface for a wrapper of the various representations of Sessions we might use.
     */
    public static interface Session
    {
        /**
         * @param s session object key
         * @return the object from the session; null if it doesn't exist
         */
        Object get(String s);

        /**
         * @param s session object key
         * @param o the object to store
         */
        void set(String s, Object o);

        /**
         * @return a unique String identifier for the session being wrapped
         */
        String getId();
    }

    ///CLOVER:OFF
    static class HttpSessionWrapper implements Session
    {
        private final HttpSession session;

        public HttpSessionWrapper(final HttpSession session)
        {
            this.session = session;
        }

        public Object get(final String s)
        {
            return session.getAttribute(s);
        }

        public void set(final String s, final Object o)
        {
            session.setAttribute(s, o);
        }

        public String getId()
        {
            return session.getId();
        }
    }

    public static class VelocityRequestSessionWrapper implements Session
    {
        private final VelocityRequestSession session;

        public VelocityRequestSessionWrapper(final VelocityRequestSession session)
        {
            this.session = session;
        }

        public Object get(final String s)
        {
            return session.getAttribute(s);
        }

        public void set(final String s, final Object o)
        {
            session.setAttribute(s, o);
        }

        public String getId()
        {
            return session.getId();
        }
    }
///CLOVER:ON
}
