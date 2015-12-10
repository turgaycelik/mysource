package com.atlassian.jira.mock.servlet;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import com.atlassian.jira.util.collect.IteratorEnumeration;

/**
 * Mock implementation of the {@link javax.servlet.FilterConfig}  interface.
 *
 * @since v4.2
 */
public class MockFilterConfig implements FilterConfig
{
    private String filterName;
    private final Map<String,String> initParams = new LinkedHashMap<String,String>();
    private ServletContext servletContext;

    public MockFilterConfig()
    {
    }

    public MockFilterConfig(final String filterName)
    {
        this.filterName = filterName;
    }

    public MockFilterConfig stubFilterName(String newName)
    {
        this.filterName = newName;
        return this;
    }

    public MockFilterConfig addInitParam(String name, String value)
    {
        this.initParams.put(name, value);
        return this;
    }

    public String getFilterName()
    {
        return filterName;
    }

    public ServletContext getServletContext()
    {
        if(servletContext != null)
        {
            return servletContext;
        }
        else
        {
            throw new UnsupportedOperationException("NO");
        }
    }

    public void setServletContext(final ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }

    public String getInitParameter(final String name)
    {
        return initParams.get(name);
    }

    public Enumeration getInitParameterNames()
    {
        return IteratorEnumeration.fromIterator(initParams.keySet().iterator());
    }
}
