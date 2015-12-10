package com.atlassian.jira.web.servletcontext;

/**
 * A servlet context reference is a variable that is tied to the life of the current servlet context
 *
 * @since v4.2
 */
public class ServletContextReference<V> implements java.io.Serializable
{
    private final ServletContextAccessor<V> accessor;

    /**
     * Constructs a reference that has a null value
     *
     * @param attributeName the name of the servlet context reference
     */
    public ServletContextReference(final String attributeName)
    {
        accessor = new ServletContextAccessor<V>(attributeName);
    }

    /**
     * Sets the value of the servlet context reference
     *
     * @param value the new value of the reference
     */
    public void set(final V value)
    {
        accessor.set(value);
    }

    /**
     * @return the current value of the servlet context reference
     */
    public V get()
    {
        return accessor.get();
    }
}
