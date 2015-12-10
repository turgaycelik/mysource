package com.atlassian.jira.web.servletcontext;

/**
 * A boolean variable that is tied to the life of the current servlet context
 *
 * @since v4.2
 */
public class ServletContextBoolean
{
    private final ServletContextAccessor<Boolean> accessor;

    /**
     * Constructs a reference that has a false value
     *
     * @param attributeName the name of the servlet context reference
     */
    public ServletContextBoolean(final String attributeName)
    {
        accessor = new ServletContextAccessor<Boolean>(attributeName);
    }

    /**
     * Sets the value of the servlet context boolean
     *
     * @param value the new value of the boolean
     */
    public void set(final boolean value)
    {
        accessor.set(value);
    }

    /**
     * @return the current value of the servlet context boolean
     */
    public boolean get()
    {
        Boolean value = accessor.get();
        return Boolean.TRUE.equals(value);
    }
}