package com.atlassian.jira;

import com.atlassian.jira.extension.ContainerProvider;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;

import java.util.List;

/**
 * This class holds reference to {@link ComponentContainer} used to instantiate objects
 *
 * @since v6.2
 */
public class WrappedComponentContainer
{
    private volatile ComponentContainer componentContainer;
    private volatile MutablePicoContainer wrappedContainer;

    public WrappedComponentContainer(final ComponentContainer componentContainer) {
        this.componentContainer = componentContainer;
        this.wrappedContainer = componentContainer.getPicoContainer();
    }

    public MutablePicoContainer getPicoContainer(){
        return wrappedContainer;
    }

    public ComponentContainer getComponentContainer()
    {
        return componentContainer;
    }

    public boolean isWrapped(){
        return componentContainer.getPicoContainer() != wrappedContainer;
    }

    public void wrapWith(final ContainerProvider containerProvider)
    {
        Preconditions.checkState(!isWrapped(), "Component container is already wrapped");
        wrappedContainer = containerProvider.getContainer(componentContainer.getPicoContainer());
    }

    public void dispose()
    {
        removeAllComponents();
        wrappedContainer.dispose();
        wrappedContainer = null;
        componentContainer = null;
    }

    private void removeAllComponents()
    {
        List<ComponentAdapter<?>> componentAdapters = Lists.newArrayList(wrappedContainer.getComponentAdapters());
        for (ComponentAdapter adapter : componentAdapters)
        {
            wrappedContainer.removeComponent(adapter.getComponentKey());
        }
    }
}
