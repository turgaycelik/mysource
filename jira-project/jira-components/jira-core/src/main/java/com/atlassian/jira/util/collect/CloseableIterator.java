package com.atlassian.jira.util.collect;

import com.atlassian.jira.util.Closeable;
import com.atlassian.jira.util.Consumer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.atlassian.jira.util.collect.CloseableIterator.Functions.foreach;

/**
 * For iterators that need to be closed after use. All CloseableIterators should be unmodifiable and throw exceptions if
 * the {@link #remove()} method is called.
 *
 * @since v3.13
 */
public interface CloseableIterator<E> extends Iterator<E>, Closeable
{
    /**
     * Unsupported operation.
     * 
     * @throws UnsupportedOperationException always
     */
    void remove();

    /**
     * Utility class for transforming a {@link EnclosedIterable} into a {@link List}. Generally you only want to do
     * this when the size of the iterable is small as it loads all the elements into memory.
     */
    public class ListResolver<T>
    {
        /**
         * Get an {@link ArrayList} of the contents of the supplied {@link CloseableIterator}
         * 
         * @return a mutable {@link ArrayList} containing all elements of the iterator.
         */
        public List<T> get(final CloseableIterator<T> it)
        {
            final List<T> result = new ArrayList<T>();
            foreach(it, new Consumer<T>()
            {
                public void consume(final T element)
                {
                    result.add(element);
                }
            });
            return result;
        }
    }

    public final class Functions
    {
        public static <T> void foreach(final CloseableIterator<T> iterator, final Consumer<T> consumer)
        {
            try
            {
                CollectionUtil.foreach(iterator, consumer);
            }
            finally
            {
                iterator.close();
            }
        }

        private Functions()
        {
            throw new AssertionError("cannot instantiate!");
        }
    }
}
