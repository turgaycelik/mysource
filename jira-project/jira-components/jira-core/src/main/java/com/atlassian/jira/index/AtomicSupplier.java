package com.atlassian.jira.index;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import com.atlassian.jira.util.Supplier;
import com.atlassian.util.concurrent.LazyReference;

import net.jcip.annotations.ThreadSafe;

/**
 * Thread-safe atomic reference like structure with lazy creation semantics
 * and ability to set to null if the reference is the same as current.
 * <p>
 * Note: this class expects to work with non-null references and treats null
 * as a special case. Do not use it if the create method is able to return
 * null as a valid value.
 *
 * @param <T> the contained reference type.
 */
@ThreadSafe
abstract class AtomicSupplier<T> implements Supplier<T>
{
    private final AtomicReference<LazyReference<T>> ref = new AtomicReference<LazyReference<T>>();

    public final @Nonnull
    T get()
    {
        LazyReference<T> lazyReference = ref.get();
        while (lazyReference == null)
        {
            ref.compareAndSet(null, new LazyReference<T>()
            {
                @Override
                protected T create() throws Exception
                {
                    return AtomicSupplier.this.create();
                }
            });
            lazyReference = ref.get();
        }
        return lazyReference.get();
    }

    /**
     * Template method for creating the internal reference.
     * @return
     */
    protected abstract @Nonnull
    T create();

    /**
     * Set the internal reference to null if it is currently pointing to the value passed in.
     *
     * @param expect only set to null if this is the same reference we are currently holding.
     */
    public void compareAndSetNull(final T expect)
    {
        final LazyReference<T> current = ref.get();
        if (expect == current.get())
        {
            ref.compareAndSet(current, null);
        }
    }
}
