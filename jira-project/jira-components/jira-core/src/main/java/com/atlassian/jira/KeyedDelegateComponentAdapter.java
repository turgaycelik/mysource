package com.atlassian.jira;

import javax.annotation.Nonnull;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.behaviors.AbstractBehavior;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Used to register a single class instance against multiple keys in a {@link org.picocontainer.PicoContainer}.
 *
 * @param <T> the concrete class.
 */
class KeyedDelegateComponentAdapter<T> extends AbstractBehavior<T>
{
    private final Class<? super T> key;

    KeyedDelegateComponentAdapter(final @Nonnull Class<? super T> key, final @Nonnull ComponentAdapter delegate)
    {
        super(delegate);
        notNull("delegate", delegate);
        this.key = notNull("key", key);
    }

    @Override
    public Class<? super T> getComponentKey()
    {
        return key;
    }


    @Override
    public String getDescriptor()
    {
        return "KeyedDelegateComponentAdapter";
    }

}
