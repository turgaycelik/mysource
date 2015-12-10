package com.atlassian.jira.config.component;

import java.lang.reflect.Type;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;

/**
 * A convenience class for implementing a Pico {@link ComponentAdapter}.
 *
 * @param <T> the type of component
 */
public abstract class AbstractComponentAdaptor<T> implements ComponentAdapter<T>
{
    protected final Class<T> interfaceClass;

    protected AbstractComponentAdaptor(final Class<T> interfaceClass)
    {
        this.interfaceClass = interfaceClass;
    }

    @Override
    public Object getComponentKey()
    {
        return interfaceClass;
    }

    @Override
    public void verify(final PicoContainer picoContainer) throws PicoCompositionException
    {}

    @Override
    public ComponentAdapter<T> getDelegate()
    {
        return null;
    }

    @Override
    public void accept(final PicoVisitor visitor)
    {
        visitor.visitComponentAdapter(this);
    }

    @Override
    public T getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException
    {
        return getComponentInstance(container);
    }

    @Override
    public <U extends ComponentAdapter> U findAdapterOfType(final Class<U> adapterType)
    {
        if (adapterType.isAssignableFrom(getClass()))
        {
            //noinspection unchecked
            return (U) this;
        }
        else if (getDelegate() != null)
        {
            return getDelegate().findAdapterOfType(adapterType);
        }
        return null;
    }

    @Override
    public String getDescriptor()
    {
        return this.getClass().getCanonicalName();
    }
}
