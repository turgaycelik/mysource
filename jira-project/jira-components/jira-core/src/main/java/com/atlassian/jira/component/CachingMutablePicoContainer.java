package com.atlassian.jira.component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.NameBinding;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;
import org.picocontainer.lifecycle.LifecycleState;

/**
 * Wrapper around MutablePicoContainer which provides simple component caching.
 * This is to workaround performance problems in Pico 2 as reported at https://extranet.atlassian.com/pages/viewpage.action?pageId=2193130032&focusedCommentId=2193132213#comment-2193132213
 */
public class CachingMutablePicoContainer implements MutablePicoContainer
{
    private final MutablePicoContainer delegatePicoContainer;
    private final ConcurrentMap<Object, Object> componentCache = new ConcurrentHashMap<Object, Object>();

    public CachingMutablePicoContainer(final MutablePicoContainer delegatePicoContainer)
    {
        this.delegatePicoContainer = delegatePicoContainer;
    }

    @Override
    public MutablePicoContainer addComponent(final Object componentKey, final Object componentImplementationOrInstance, final Parameter... parameters)
    {
        delegatePicoContainer.addComponent(componentKey, componentImplementationOrInstance, parameters);
        return this;
    }

    @Override
    public MutablePicoContainer addComponent(final Object implOrInstance)
    {
        delegatePicoContainer.addComponent(implOrInstance);
        return this;
    }

    @Override
    public MutablePicoContainer addConfig(final String name, final Object val)
    {
        delegatePicoContainer.addConfig(name, val);
        return this;
    }

    @Override
    public MutablePicoContainer addAdapter(final ComponentAdapter<?> componentAdapter)
    {
        delegatePicoContainer.addAdapter(componentAdapter);
        return this;
    }

    @Override
    public <T> ComponentAdapter<T> removeComponent(final Object componentKey)
    {
        componentCache.remove(componentKey);
        return delegatePicoContainer.removeComponent(componentKey);
    }

    @Override
    public <T> ComponentAdapter<T> removeComponentByInstance(final T componentInstance)
    {
        // We don't actually ever call this method ATM, but anyway...
        ComponentAdapter<T> removedComponent = delegatePicoContainer.removeComponentByInstance(componentInstance);
        if (removedComponent != null)
        {
            componentCache.remove(removedComponent.getComponentKey());
        }
        return removedComponent;
    }

    @Override
    public MutablePicoContainer makeChildContainer()
    {
        delegatePicoContainer.makeChildContainer();
        return this;
    }

    @Override
    public MutablePicoContainer addChildContainer(final PicoContainer child)
    {
        delegatePicoContainer.addChildContainer(child);
        return this;
    }

    @Override
    public boolean removeChildContainer(final PicoContainer child)
    {
        return delegatePicoContainer.removeChildContainer(child);
    }

    @Override
    public MutablePicoContainer change(final Properties... properties)
    {
        delegatePicoContainer.change(properties);
        return this;
    }

    @Override
    public MutablePicoContainer as(final Properties... properties)
    {
        delegatePicoContainer.as(properties);
        return this;
    }

    @Override
    public void setName(final String name)
    {
        delegatePicoContainer.setName(name);
    }

    @Override
    public void setLifecycleState(final LifecycleState lifecycleState)
    {
        delegatePicoContainer.setLifecycleState(lifecycleState);
    }

    @Override
    public String getName()
    {
        return delegatePicoContainer.getName();
    }

    @Override
    public LifecycleState getLifecycleState()
    {
        return delegatePicoContainer.getLifecycleState();
    }

    @Override
    public Object getComponent(final Object componentKeyOrType)
    {
        return delegatePicoContainer.getComponent(componentKeyOrType);
    }

    @Override
    public Object getComponent(final Object componentKeyOrType, final Type into)
    {
        return delegatePicoContainer.getComponent(componentKeyOrType, into);
    }

    @Override
    public <T> T getComponent(final Class<T> componentType)
    {
        @SuppressWarnings ("unchecked")
        T component = (T) componentCache.get(componentType);
        if (component != null)
        {
            return component;
        }
        component = delegatePicoContainer.getComponent(componentType);
        if (component != null)
        {
            componentCache.putIfAbsent(componentType, component);
        }
        return component;
    }

    @Override
    public <T> T getComponent(final Class<T> componentType, final Class<? extends Annotation> binding)
    {
        return delegatePicoContainer.getComponent(componentType, binding);
    }

    @Override
    public List<Object> getComponents()
    {
        return delegatePicoContainer.getComponents();
    }

    @Override
    public PicoContainer getParent()
    {
        return delegatePicoContainer.getParent();
    }

    @Override
    public ComponentAdapter<?> getComponentAdapter(final Object componentKey)
    {
        return delegatePicoContainer.getComponentAdapter(componentKey);
    }

    @Override
    public <T> ComponentAdapter<T> getComponentAdapter(final Class<T> componentType, final NameBinding componentNameBinding)
    {
        return delegatePicoContainer.getComponentAdapter(componentType, componentNameBinding);
    }

    @Override
    public <T> ComponentAdapter<T> getComponentAdapter(final Class<T> componentType, final Class<? extends Annotation> binding)
    {
        return delegatePicoContainer.getComponentAdapter(componentType, binding);
    }

    @Override
    public Collection<ComponentAdapter<?>> getComponentAdapters()
    {
        return delegatePicoContainer.getComponentAdapters();
    }

    @Override
    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Class<T> componentType)
    {
        return delegatePicoContainer.getComponentAdapters(componentType);
    }

    @Override
    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Class<T> componentType, final Class<? extends Annotation> binding)
    {
        return delegatePicoContainer.getComponentAdapters(componentType, binding);
    }

    @Override
    public <T> List<T> getComponents(final Class<T> componentType)
    {
        return delegatePicoContainer.getComponents(componentType);
    }

    @Override
    public void accept(final PicoVisitor visitor)
    {
        delegatePicoContainer.accept(visitor);
    }

    @Override
    public void start()
    {
        delegatePicoContainer.start();
    }

    @Override
    public void stop()
    {
        delegatePicoContainer.stop();
    }

    @Override
    public void dispose()
    {
        delegatePicoContainer.dispose();
        componentCache.clear();
    }
}
