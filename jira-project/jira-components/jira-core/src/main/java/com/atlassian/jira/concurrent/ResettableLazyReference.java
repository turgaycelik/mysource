package com.atlassian.jira.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;

import com.atlassian.fugue.Option;
import com.atlassian.jira.util.Supplier;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Lazy reference holder that can be initialized with supplier on call to {@link #getOrCreate(Supplier)}
 * The reference can be cleared atomically with {@link #reset()} or {@link #safeReset(Object)}.
 * Once reset, no other thread is able obtain the same value from this reference (though other
 * threads may already have obtained it before the reset occurred).
 *
 * @since v6.3.9
 */
public class ResettableLazyReference<T>
{
    private final Lock lock = new ReentrantLock();
    private volatile T reference;

    /**
     * Resets this reference to empty, returning the old value.
     *
     * @return option containing the previous value of the reference, if there was one
     */
    public Option<T> reset()
    {
        lock.lock();
        try
        {
            final T oldReference = reference;
            reference = null;
            return Option.option(oldReference);
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Compares the reference value to the {@code expected} value and resets it if and only if it is identical.
     * <p/>
     * The reset request will fail (return {@code Option.none()} and have no effect) if the {@code expected}
     * value supplied is {@code null}.
     *
     * @param expected the expected value of the reference
     * @return an option to the reset value.  If no reset was performed (either because the expected value was
     *      given as {@code null} or because it did not match the value currently held by the reference), then
     *      {@code Option.none()} is returned.
     */
    @SuppressWarnings("ObjectEquality")  // Identity equality is intentional, here
    public Option<T> safeReset(final T expected)
    {
        if (expected != null)
        {
            lock.lock();
            try
            {
                if (reference == expected)
                {
                    reference = null;
                    return Option.some(expected);
                }
            }
            finally
            {
                lock.unlock();
            }
        }
        return Option.none();
    }

    /**
     * @return {@code Option.some} on the current value for this reference; {@code Option.none()} if it has not
     *      been initialized yet
     */
    public Option<T> get()
    {
        return Option.option(reference);
    }

    /**
     * Gets the reference's value, obtaining the new value from the supplier if it was not previously
     * initialized.
     * <p/>
     * Only one thread will be allowed to call the supplier at any given time, and it will not be
     * called again before the reference is {@link #reset()}.
     *
     * @param creator supplier to be used for creating this reference
     * @return value for this reference or newly created one
     * @throws IllegalArgumentException if {@link Supplier#get() creator.get()} returns {@code null}
     */
    @Nonnull
    public T getOrCreate(final Supplier<T> creator)
    {
        final T localReference = reference;
        return (localReference != null) ? localReference : getOrCreateUnderLock(creator);
    }

    @Nonnull
    private T getOrCreateUnderLock(final Supplier<T> creator)
    {
        lock.lock();
        try
        {
            if (reference == null)
            {
                reference = notNull("creator.get()", creator.get());
            }
            return reference;
        }
        finally
        {
            lock.unlock();
        }
    }
}
