package com.atlassian.sal.jira.component;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.sal.api.component.ComponentLocator;

import java.util.Collection;
import java.util.Map;

public class JiraComponentLocator extends ComponentLocator
{
    public JiraComponentLocator()
    {
        ComponentLocator.setComponentLocator(this);
    }

    @Override
    protected <T> T getComponentInternal(Class<T> iface)
    {
        return ComponentAccessor.getComponentOfType(iface);
    }

    @Override
    protected <T> T getComponentInternal(Class<T> iface, String componentId)
    {
        final Map<String, T> beansOfType = ComponentManager.getComponentsOfTypeMap(iface);
        return beansOfType.get(componentId);
    }

    @Override
    protected <T> Collection<T> getComponentsInternal(Class<T> iface)
    {
        return ComponentManager.getComponentsOfTypeMap(iface).values();
    }
}