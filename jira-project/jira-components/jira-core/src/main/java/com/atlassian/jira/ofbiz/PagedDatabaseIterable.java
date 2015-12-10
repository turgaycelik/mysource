package com.atlassian.jira.ofbiz;

import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Resolver;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.collect.CloseableIterator;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.collect.ListOrderComparator;
import com.atlassian.jira.util.collect.ResolvingComparator;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A abstract implementation of {@link com.atlassian.jira.util.collect.EnclosedIterable} that takes a list of ids and
 * then pages them by opening an {@link OfBizListIterator} for each page.
 * <p/>
 * This implementation supports preserving the order of the elements in the returned list but will load all the elements
 * into memory in order to do so.
 * <p/>
 * This implementation uses the {@link DatabaseIterator} but implements paging and sorting on top of it.
 *
 * @since v3.13
 */
public abstract class PagedDatabaseIterable<E, K> implements EnclosedIterable<E>
{
    private static final int DEFAULT_PAGE_SIZE = 100;

    private final List<K> ids;
    private final Resolver<E, K> keyResolver;
    private final int pageSize;

    private final Supplier<Resolver<GenericValue, E>> resolverFactory = new Supplier<Resolver<GenericValue, E>>()
    {
        public com.atlassian.jira.util.Resolver<GenericValue, E> get()
        {
            return getResolver();
        };
    };

    private final Function<List<K>, OfBizListIterator> iteratorFactory = new Function<List<K>, OfBizListIterator>()
    {
        public OfBizListIterator get(final List<K> input)
        {
            return createListIterator(input);
        }
    };

    /**
     * Use where the order of the result is not important.
     *
     * @param ids a List<K> must not be null
     */
    public PagedDatabaseIterable(final List<K> ids)
    {
        this(ids, null);
    }

    /**
     * Use where the order of the result <strong>is</strong> important.
     *
     * @param ids a List<K> must not be null
     * @param keyResolver to get a domain object's key (ie. the element in the ids list). Null if sort order is
     * unimportant
     */
    public PagedDatabaseIterable(final List<K> ids, final Resolver<E, K> keyResolver)
    {
        this(ids, keyResolver, DEFAULT_PAGE_SIZE);
    }

    /**
     * Use where the order of the result <strong>is</strong> important.
     *
     * @param ids a List<K> must not be null
     * @param keyResolver to get a domain object's key (ie. the element in the ids list). Null if sort order is
     * unimportant
     * @param pageSize the size of the pages to use
     */
    public PagedDatabaseIterable(final List<K> ids, final Resolver<E, K> keyResolver, final int pageSize)
    {
        this.ids = Collections.unmodifiableList(notNull("ids", ids));
        this.keyResolver = keyResolver;
        this.pageSize = pageSize;
    }

    CloseableIterator<E> iterator()
    {
        if (isEmpty())
        {
            return new EmptyIterator<E>();
        }
        if (keyResolver != null)
        {
            return new SortedIterator<E, K>(keyResolver, ids, new PagingIterator<E, K>(ids, pageSize, resolverFactory, iteratorFactory));
        }
        return new PagingIterator<E, K>(ids, pageSize, resolverFactory, iteratorFactory);
    }

    public final void foreach(final Consumer<E> consumer)
    {
        CloseableIterator.Functions.foreach(this.iterator(), consumer);
    }

    /**
     * Create a new iterator from the subset of ids.
     *
     * @param ids list of ids
     * @return an instance of OfBizListIterator
     */
    protected abstract OfBizListIterator createListIterator(List<K> ids);

    /**
     * Used to turn generic values into Domain objects
     *
     * @return a resolver
     */
    protected abstract Resolver<GenericValue, E> getResolver();

    public final int size()
    {
        return ids.size();
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * {@link CloseableIterator} that loads all the elements into memory and then sorts them based on the ids list
     * order.
     */
    private static final class SortedIterator<E, K> implements CloseableIterator<E>
    {
        private final Iterator<E> it;

        SortedIterator(final Resolver<E, K> keyResolver, final List<K> ids, final PagingIterator<E, K> delegate)
        {
            // the list resolver closes the iterator for us
            final List<E> list = new ArrayList<E>(new CloseableIterator.ListResolver<E>().get(delegate));
            Collections.sort(list, new ResolvingComparator<E, K>(keyResolver, new ListOrderComparator<K>(ids)));
            it = list.iterator();
        }

        public E next()
        {
            return it.next();
        }

        public void remove()
        {
            it.remove();
        }

        ///CLOVER:OFF
        public boolean hasNext()
        {
            return it.hasNext();
        }

        ///CLOVER:ON

        ///CLOVER:OFF
        public void close()
        {}
        ///CLOVER:ON
    }

    /**
     * An iterator that pages the Database queries into manageable chunks.
     */
    private static final class PagingIterator<E, K> implements CloseableIterator<E>
    {
        private CloseableIterator<E> delegate = new EmptyIterator<E>(); // we close this in createDelegate() so must be non-null
        private int end = -1; // start at -1 so we init start properly
        private final List<K> ids;
        private final int pageSize;
        private final Supplier<Resolver<GenericValue, E>> resolverFactory;
        private final Function<List<K>, OfBizListIterator> iteratorFactory;

        public PagingIterator(final List<K> ids, final int pageSize, final Supplier<Resolver<GenericValue, E>> resolverFactory, final Function<List<K>, OfBizListIterator> iteratorFactory)
        {
            this.ids = ids;
            this.pageSize = pageSize;
            this.resolverFactory = resolverFactory;
            this.iteratorFactory = iteratorFactory;
            createDelegate();
        }

        public E next()
        {
            if (!delegate.hasNext() && hasMorePages())
            {
                createDelegate();
            }
            return delegate.next();
        }

        public boolean hasNext()
        {
            return delegate.hasNext() || hasMorePages();
        }

        public void close()
        {
            delegate.close();
        }

        public void remove()
        {
            delegate.remove();
        }

        private boolean hasMorePages()
        {
            return end < ids.size() - 1;
        }

        private void createDelegate()
        {
            final int start = end + 1; // end inits to -1
            end = Math.min(end + pageSize, ids.size());
            delegate.close();
            delegate = new DatabaseIterator<E>(resolverFactory.get(), iteratorFactory.get(ids.subList(start, end)));
        }
    }
}