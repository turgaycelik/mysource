package com.atlassian.jira.web.servletcontext;

/**
 * A long variable that is tied to the life of the current servlet context
 *
 * @since v4.2
 */
public class ServletContextLong implements java.io.Serializable
{
    private final ServletContextAccessor<Long> accessor;

    /**
     * Constructs a reference that has a 0 value
     *
     * @param attributeName the name of the servlet context reference
     */
    public ServletContextLong(final String attributeName)
    {
        accessor = new ServletContextAccessor<Long>(attributeName);
    }

    /**
     * Sets the value of the servlet context long
     *
     * @param value the new value of the long
     */
    public void set(final long value)
    {
        accessor.set(value);
    }

    /**
     * @return the current value of the servlet context long or 0 if its never been set
     */
    public long get()
    {
        Long value = accessor.get();
        return value == null ? 0L : value;
    }
}