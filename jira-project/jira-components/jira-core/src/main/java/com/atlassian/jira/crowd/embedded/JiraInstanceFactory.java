package com.atlassian.jira.crowd.embedded;

import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.google.common.base.Preconditions;

public class JiraInstanceFactory implements InstanceFactory
{
    private final ComponentClassManager componentClassManager;

    public JiraInstanceFactory(ComponentClassManager componentClassManager)
    {
        this.componentClassManager = Preconditions.checkNotNull(componentClassManager);
    }

    public Object getInstance(String clazzName) throws ClassNotFoundException
    {
        return componentClassManager.newInstance(clazzName);
    }

    public Object getInstance(String clazzName, ClassLoader classLoader) throws ClassNotFoundException
    {
        return componentClassManager.newInstance(clazzName);
    }

    public <T> T getInstance(Class<T> tClass)
    {
        Preconditions.checkNotNull(tClass);
        
        try
        {
            return (T)componentClassManager.newInstance(tClass.getName());
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException("This really should never happen", e);
        }
    }
}
