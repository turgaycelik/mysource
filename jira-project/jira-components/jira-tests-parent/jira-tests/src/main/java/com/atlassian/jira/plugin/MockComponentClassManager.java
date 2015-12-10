package com.atlassian.jira.plugin;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.Plugin;

/**
 * @since v4.0
 */
public class MockComponentClassManager implements ComponentClassManager
{
    public <T> T newInstance(final String className) throws ClassNotFoundException
    {
        return JiraUtils.<T>loadComponent(className, getClass());
    }

    public <T> T newInstanceFromPlugin(final Class<T> clazz, final Plugin plugin)
    {
        try
        {
            return (T) newInstance(clazz.getName());
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    public <T> Class<T> loadClass(final String className) throws ClassNotFoundException
    {
        //noinspection unchecked
        return ClassLoaderUtils.loadClass(className, getClass());
    }
}
