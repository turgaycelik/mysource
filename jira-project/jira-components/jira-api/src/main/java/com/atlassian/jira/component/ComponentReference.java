package com.atlassian.jira.component;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.util.concurrent.Supplier;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Provides a way to obtain a dependency that cannot be injected due to a circular reference
 * and without having to make numerous explicit calls to {@code ComponentAccessor}.
 * <p>
 * Note that this is both serializable and thread-safe.  However, it will only resolve components
 * that were registered with the component class provided in the constructor; you can not specify
 * some other interface as with {@link ComponentAccessor#getComponentOfType(Class)} or reference
 * OSGi services as with {@link ComponentAccessor#getOSGiComponentInstanceOfType(Class)}.  This
 * is a good thing; you should be using the proper registration key, anyway.
 * </p>
 * <p>
 * Unlike {@code LazyReference}, the {@link #get()} method may be re-entered and multiple threads
 * are permitted to resolve the reference if necessary.  On the other hand, it also cannot deadlock,
 * which {@code LazyReference} will do (in versions prior to 2.5.0) if a single thread attempts to
 * re-enter it due to circular logic.  Since circular logic using this class will return to the
 * {@code ComponentAccessor} and eventually to Pico, the container's normal dependency loop
 * detection is applied at the time the component is requested, which means you'll get told what
 * the circular path was.
 * </p>
 *
 * @param <T> the registration key for the component; usually an interface
 * @since 6.3
 */
@ExperimentalApi
public class ComponentReference<T> implements Supplier<T>, com.atlassian.jira.util.Supplier<T>, Serializable
{
    private static final long serialVersionUID = 2634940091578441311L;

    private final Class<T> componentClass;
    private volatile transient T component;

    /**
     * Internal constructor.
     * <p>
     * Use the factory method {@link ComponentAccessor#getComponentReference(Class)} to create component references.
     * </p>
     * @param componentClass the registration key for the component; usually an interface and never {@code null}
     */
    ComponentReference(@Nonnull Class<T> componentClass)
    {
        this.componentClass = notNull("componentClass", componentClass);
    }

    @Override
    public T get()
    {
        T instance = component;  // volatile read
        if (instance == null)
        {
            instance = ComponentAccessor.getComponent(componentClass);
            component = instance;  // volatile write
        }
        return instance;
    }

    /**
     * Dependency references are equal iff they are references for the same {@code componentClass}.
     */
    @Override
    public final boolean equals(final Object o)
    {
        return o instanceof ComponentReference && componentClass == ((ComponentReference<?>)o).componentClass;
    }

    @Override
    public final int hashCode()
    {
        return componentClass.hashCode();
    }

    @Override
    public String toString()
    {
        return "ComponentReference[" + componentClass + ']';
    }
}

