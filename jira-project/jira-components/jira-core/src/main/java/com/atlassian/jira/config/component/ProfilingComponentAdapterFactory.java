package com.atlassian.jira.config.component;

import java.util.Properties;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.behaviors.AbstractBehaviorFactory;

public class ProfilingComponentAdapterFactory extends AbstractBehaviorFactory
{

    private ProfilingComponentAdapterFactory()
    {
    }

    @Override
    public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor componentMonitor, final LifecycleStrategy lifecycleStrategy, final Properties componentProperties, final Object componentKey, final Class<T> componentImplementation, final Parameter... parameters)
            throws PicoCompositionException
    {
        return componentMonitor.newBehavior(new ProfilingComponentAdapter<T>(super.createComponentAdapter(
                componentMonitor,
                lifecycleStrategy,
                componentProperties,
                componentKey,
                componentImplementation,
                parameters)));
    }

    @Override
    public <T> ComponentAdapter<T> addComponentAdapter(final ComponentMonitor componentMonitor, final LifecycleStrategy lifecycleStrategy, final Properties componentProperties, final ComponentAdapter<T> adapter)
    {
        return componentMonitor.newBehavior(new ProfilingComponentAdapter(super.addComponentAdapter(
                componentMonitor,
                lifecycleStrategy,
                componentProperties,
                adapter)));
    }

}