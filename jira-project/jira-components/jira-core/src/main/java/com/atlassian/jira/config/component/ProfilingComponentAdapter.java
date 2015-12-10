package com.atlassian.jira.config.component;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;

import com.atlassian.util.profiling.UtilTimerStack;

import org.apache.log4j.Logger;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.AbstractBehavior;

public class ProfilingComponentAdapter<T> extends AbstractBehavior<T>
{
    private static final Logger log = Logger.getLogger(ProfilingComponentAdapter.class);

    private final AtomicReference<T> instanceReference = new AtomicReference<T>();
    private final AtomicReference<T> profilingReference = new AtomicReference<T>();

    public ProfilingComponentAdapter(ComponentAdapter delegate)
    {
        super(delegate);
    }

    @Override
    public T getComponentInstance(PicoContainer container, final Type into) throws PicoCompositionException
    {
        if (instanceReference.get() == null)
        {
            if(log.isDebugEnabled())
            {
                //this may be needed to debug issues when instantiating a particular component instance fails.  This can
                //happen for example if a component that needs to be injected is registered against an interface in the
                //ContainerRegistrar, however the constructor for the component instance that we're getting here declares
                //the concrete class.  If the concrete class is wrapped by a dynamic proxy (such as the ProfilingComponentAdaptor
                //may create), then this would blow up.
                log.debug("Getting component instance with key '" + getComponentKey() + "' and implementation class '" + getComponentImplementation() +"'.");
            }
            @SuppressWarnings("unchecked")
            final T componentInstance;
            try
            {
                //noinspection unchecked
                componentInstance =  delegate.getComponentInstance(container, into);
            }
            catch (PicoCompositionException ex)
            {
                // Pico's Error message fails to name the Class we are trying to instantiate, so throw our own error with a better message.
                throw new PicoCompositionException("Error trying to instantiate " + getComponentImplementation() + ": " + ex.getMessage(), ex);
            }
            final Object key = super.getComponentKey();
            if (key != null && key instanceof Class && ((Class)key).isInterface())
            {
                @SuppressWarnings("unchecked")
                final T profiledInstance = (T) GroupedMethodProfiler.getProfiledObject(componentInstance);
                profilingReference.set(profiledInstance);
            }
            else
            {
                profilingReference.set(componentInstance);
            }
            instanceReference.set(componentInstance);
        }

        if (UtilTimerStack.isActive())
        {
            return profilingReference.get();
        }
        else
        {
            return instanceReference.get();
        }
    }

    @Override
    public String getDescriptor()
    {
       return "ProfilingComponentAdapter";
    }

}
