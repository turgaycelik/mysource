package com.atlassian.jira.component;

import com.atlassian.jira.ComponentManager;

/**
 * Worker class that insulates the API from the implementation dependencies in ManagerFactory etc.
 *
 * @since v4.3
 */
public class ComponentAccessorWorker implements ComponentAccessor.Worker
{
    @Override
    public <T> T getComponent(Class<T> componentClass)
    {
        return ComponentManager.getComponent(componentClass);
    }

    @Override
    public <T> T getComponentOfType(Class<T> componentClass)
    {
        return ComponentManager.getComponentInstanceOfType(componentClass);
    }

    @Override
    public <T> T getOSGiComponentInstanceOfType(Class<T> componentClass)
    {
        return ComponentManager.getOSGiComponentInstanceOfType(componentClass);
    }
}