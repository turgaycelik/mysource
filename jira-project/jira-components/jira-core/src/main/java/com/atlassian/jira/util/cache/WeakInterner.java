package com.atlassian.jira.util.cache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Similar to a Guava {@code WeakInterner}, but significantly lighter weight.
 * <p>
 * The Guava implementation of a weak interner has considerable overhead because it is implemented in terms of
 * the {@code CustomConcurrentHashMap} grab bag of functionality with all its accompanying statistical tracking,
 * eviction policy, and so on.  This implementation is based directly on the lighter {@code ConcurrentHashMap}
 * implementation that comes with the JDK.
 * </p>
 * <p>
 * Note: This assumes that your interned objects are sane for interning purposes, meaning that they are
 * immutable objects with stable hash codes that are consistent with equals and have a reasonably efficient
 * implementation for {@link Object#equals(Object) equals}.  Violate these assumptions at your own risk.
 * </p>
 *
 * @since v6.3
 */
public class WeakInterner<T>
{
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

    /** The real backing store for the interner. */
    final ConcurrentMap<InternReference<T>, InternReference<T>> store;

    /** Weak references that are cleared by the GC get enqueued here so that we'll know to evict them in cleanUp(). */
    final ReferenceQueue<T> queue;



    public static <T> WeakInterner<T> newWeakInterner()
    {
        return new WeakInterner<T>();
    }



    public WeakInterner()
    {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
    }

    public WeakInterner(final int initialCapacity)
    {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
    }

    public WeakInterner(final int initialCapacity, final float loadFactor)
    {
        this(initialCapacity, loadFactor, DEFAULT_CONCURRENCY_LEVEL);
    }

    public WeakInterner(final int initialCapacity, final float loadFactor, final int concurrencyLevel)
    {
        this.store = new ConcurrentHashMap<InternReference<T>,InternReference<T>>(initialCapacity, loadFactor, concurrencyLevel);
        this.queue = new ReferenceQueue<T>();
    }


    /**
     * Weakly interns the specified non-null value.
     * <p>
     * Implicitly calls {@link #cleanUp()}.
     * </p>
     *
     * @param value the value to intern; must not be {@code null}
     * @return either another object that was previously interned and is semantically equal to {@code value}, or
     *          {@code value} itself if there is no previously interned instance available.
     * @throws IllegalArgumentException if {@code value} is {@code null}
     */
    @Nonnull
    public T intern(@Nonnull final T value)
    {
        return internImpl(notNull("value", value));
    }

    /**
     * Weakly interns the specified value, with {@code null} values tolerated.
     * <p>
     * Implicitly calls {@link #cleanUp()} unless {@code value} is {@code null}.
     * </p>
     *
     * @param value the value to intern; may be {@code null}
     * @return either {@code value} or another instance of that object which was previously interned and is
     *      semantically equal to {@code value}
     */
    @Nullable
    public T internOrNull(@Nullable final T value)
    {
        return (value != null) ? internImpl(value) : null;
    }

    /**
     * Requests an explicit clean-up pass.
     * <p>
     * The clean-up is invoked implicitly on every call to {@link #intern(Object)} or {@link #internOrNull(Object)}
     * (provided the argument is not in fact {@code null}), so it should not normally be necessary to call this
     * method explicitly.
     * </p>
     */
    public void cleanUp()
    {
        Reference<? extends T> dead = queue.poll();
        while (dead != null)
        {
            // Our "InternReference" objects are the only things that go in our ref queue, so the type check
            // would be redundant.
            //noinspection SuspiciousMethodCalls
            store.remove(dead);
            dead = queue.poll();
        }
    }



    @Nonnull
    private T internImpl(@Nonnull final T value)
    {
        cleanUp();

        final InternReference<T> ref = new InternReference<T>(value, queue);
        for (;;)
        {
            // If we are first, then our own value becomes the interned one
            final InternReference<T> existing = store.putIfAbsent(ref, ref);
            if (existing == null)
            {
                return value;
            }

            // Data race: GC can sneak in to clear the reference between the keys testing as equal and us using get()
            // to create a new strong reference to the interned value.  We must null check the interned value to make
            // sure that this hasn't happened.
            final T interned = existing.get();
            if (interned != null)
            {
                return interned;
            }

            // This shouldn't really be necessary as the null value should guarantee that it won't pass the equals
            // test on the next attempt; however, this is a rare and tiny extra cost (the next cleanUp() will do a
            // redundant remove() for this key) for the piece of mind that this dead key can't possibly get in our
            // way again.
            store.remove(existing);
        }
    }



    static final class InternReference<T> extends WeakReference<T>
    {
        private final int hash;

        InternReference(final T value, final ReferenceQueue<? super T> queue)
        {
            super(value, queue);
            this.hash = value.hashCode();
        }

        /**
         * Keys are equal by identity or by non-null interned value.
         * <p>
         * While the interned value is still reachable, a new key with the same value will be equal to it and find
         * the same entry.  However, once the GC has cleared the reference, it will only be equal via identity.  The
         * hash code of the key is the same as that of the interned value and is held locally so that it will remain
         * stable after the weak reference is cleared.
         */
        @Override
        public boolean equals(final Object obj)
        {
            return this == obj || (obj instanceof InternReference && equals((InternReference<?>)obj));
        }

        private boolean equals(@Nonnull final InternReference<?> other)
        {
            final T value = get();
            return value != null && value.equals(other.get());
        }

        @Override
        public int hashCode()
        {
            return hash;
        }

        @Override
        public String toString()
        {
            return "InternReference@" + Integer.toHexString(System.identityHashCode(this)) +
                    "[hash=" + hash +
                    ",referent=" + get() +
                    ']';
        }
    }
}
