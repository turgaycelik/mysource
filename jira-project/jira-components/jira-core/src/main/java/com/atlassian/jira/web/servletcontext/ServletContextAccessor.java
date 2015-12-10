package com.atlassian.jira.web.servletcontext;

import com.atlassian.jira.web.ServletContextProvider;

import javax.servlet.ServletContext;

/**
 * A support class to help access the servlet context
 *
 * @since v4.2
 */
class ServletContextAccessor<V>
{
    private final String attributeName;

    ServletContextAccessor(final String attributeName)
    {
        this.attributeName = attributeName;
    }

    V get()
    {
        V value = null;
        ServletContext context = ServletContextProvider.getServletContext();
        if (context != null)
        {
            //noinspection unchecked
            value = (V) context.getAttribute(attributeName);
        }
        return value;

    }

    void set(V value)
    {
        ServletContext context = ServletContextProvider.getServletContext();
        if (context == null)
        {
            throw new IllegalStateException("The servlet context has not been initialised yet");
        }
        context.setAttribute(attributeName, value);
    }

}
