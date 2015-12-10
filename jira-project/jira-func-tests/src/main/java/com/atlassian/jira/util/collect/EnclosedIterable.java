package com.atlassian.jira.util.collect;

import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Resolver;

import java.util.ArrayList;
import java.util.List;

/**
 * A limited collection view that may be backed by the something that needs closing, for example a connection to a
 * database.
 * <p>
 * You can access all elements using the {@link #foreach(Consumer)} method.
 *
 * @since v3.13
 */
public interface EnclosedIterable<T> extends Sized
{
    /**
     * Apply the sink to all elements in the Collection.
     */
    void foreach(Consumer<T> sink);

    /**
     * @return the likely size of the objects passed into the sink in {@link #foreach(Consumer)}. Be careful depending on this size
     *         being exact, as in many cases its best efforts value or may be stable due to concurrent changes.
     */
    int size();

    /**
     * @return true if the there is no data behind it.
     */
    boolean isEmpty();

    /**
     * Utility class for transforming a {@link EnclosedIterable} into a {@link List}. Generally you only want to do
     * this when the size of the iterable is small as it loads all the elements into memory.
     */
    public class ListResolver<T> implements Resolver<EnclosedIterable<T>, List<T>>
    {
        /**
         * Get an {@link ArrayList} of the contents of the supplied {@link EnclosedIterable}
         * 
         * @return a mutable {@link ArrayList} containing all elements of the iterable.
         */
        public List<T> get(final EnclosedIterable<T> iterable)
        {
            final List<T> result = new ArrayList<T>();
            iterable.foreach(new Consumer<T>()
            {
                public void consume(final T element)
                {
                    result.add(element);
                }
            });
            return result;
        }
    }

    public class Functions
    {
        /**
         * Pass all the elements of the iterable to the supplied {@link Consumer}. Guarantees that the iterator used will be
         * closed correctly
         *
         * @param iterable containing elements of type T
         * @param sink that will consume the elements
         */
        public static <T> void apply(final EnclosedIterable<T> iterable, final Consumer<T> sink)
        {
            iterable.foreach(sink);
        }

        /**
         * Get an {@link ArrayList} of the contents of the supplied {@link EnclosedIterable}
         * 
         * @return a mutable {@link ArrayList} containing all elements of the iterable.
         */
        public static <T> List<T> toList(final EnclosedIterable<T> iterable)
        {
            return toList(iterable, new Function<T, T>()
            {
                public T get(final T input)
                {
                    return input;
                }
            });
        }

        /**
         * Get an {@link ArrayList} of the contents of the supplied {@link EnclosedIterable}
         * transformed by the supplied transform function into the new type O.
         * 
         * @return a mutable {@link ArrayList} containing all elements of the iterable.
         */
        public static <I, O> List<O> toList(final EnclosedIterable<I> iterable, final Function<I, O> transformer)
        {
            final List<O> result = new ArrayList<O>(iterable.size());
            iterable.foreach(new Consumer<I>()
            {
                public void consume(final I element)
                {
                    result.add(transformer.get(element));
                };
            });
            return result;
        }
    }
}
