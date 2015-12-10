package com.atlassian.jira.config.component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;

// TODO Remove this class in favour of {@link org.picocontainer.injectors.ProviderAdapter} pattern
public abstract class AbstractSwitchingInvocationAdaptor<T> extends AbstractComponentAdaptor<T>
{

    private final Class<? extends T> enabledClass;
    private final Class<? extends T> disabledClass;

    protected AbstractSwitchingInvocationAdaptor(
            final Class<T> interfaceClass, final Class<? extends T> enabledClass, final Class<? extends T> disabledClass)
    {
        super(interfaceClass);
        this.enabledClass = enabledClass;
        this.disabledClass = disabledClass;
    }

    protected boolean isEnabled()
    {
        return getInvocationSwitcher().isEnabled();
    }

    public Class<? extends T> getComponentImplementation()
    {
        return isEnabled() ? enabledClass : disabledClass;
    }

    protected InvocationHandler getHandler(final PicoContainer container)
    {
        Object enabled = container.getComponent(enabledClass);
        Object disabled = container.getComponent(disabledClass);

        return new SwitchingInvocationHandler(enabled, disabled, getInvocationSwitcher());
    }

    public T getComponentInstance(final PicoContainer container) throws PicoCompositionException
    {
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { interfaceClass }, getHandler(container));
    }

   protected abstract InvocationSwitcher getInvocationSwitcher();
}
