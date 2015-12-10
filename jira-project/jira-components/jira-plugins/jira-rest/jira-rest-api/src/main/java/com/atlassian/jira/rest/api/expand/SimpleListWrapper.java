package com.atlassian.jira.rest.api.expand;

import com.atlassian.plugins.rest.common.expand.entity.ListWrapper;
import com.atlassian.plugins.rest.common.expand.entity.ListWrapperCallback;
import com.atlassian.plugins.rest.common.expand.parameter.Indexes;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * This is a list wrapper that works by wrapping a java.util.Collection that has been eagerly loaded into memory. As
 * such, it does not take full advantage of the "expandable" concepts since it requires callers to have previously
 * loaded the entire data set into memory, but it has the advantage of being simple to implement.
 * <p/>
 * This class makes a shallow copy of the wrapped collection.
 *
 * @see com.atlassian.plugins.rest.common.expand.Expandable
 * @since v4.2
 */
@XmlRootElement (name = "list")
public class SimpleListWrapper<T> implements ListWrapper<T>
{
    /**
     * Returns an empty SimpleListWrapper.
     *
     * @param <T> the type of the list element
     * @return an empty SimpleListWrapper
     */
    public static <T> SimpleListWrapper<T> empty()
    {
        return of(Collections.<T>emptyList());
    }

    /**
     * Returns a new SimpleListWrapper that wraps the given elements.
     *
     * @param <T> the type of the list element
     * @param elements the elements to wrap
     * @return a new SimpleListWrapper
     */
    public static <T> SimpleListWrapper<T> of(T... elements)
    {
        return of(Arrays.asList(elements));
    }

    /**
     * Returns a new SimpleListWrapper that is backed by the given collection.
     *
     * @param <T> the type of the list element
     * @param list the backing Collection
     * @return a new SimpleListWrapper
     */
    public static <T> SimpleListWrapper<T> of(List<T> list)
    {
        return of(list, null);
    }

    /**
     * Returns a new SimpleListWrapper that is backed by the given list and returns at most maxResults items to the
     * client.
     *
     * @param <T> the type of the list element
     * @param list the backing List
     * @param maxResults the maximum number of results to return to the client in one call, or null
     * @return a new SimpleListWrapper
     */
    public static <T> SimpleListWrapper<T> of(List<T> list, Integer maxResults)
    {
        return new SimpleListWrapper<T>(list, maxResults, list.size());
    }

    /**
     * Returns a new SimpleListWrapper that is backed by the given list and returns at most maxResults items to the
     * client. The size attribute will contain the provided value, instead of the actual size of the passed-in list.
     *
     * @param <T> the type of the list element
     * @param list the backing List
     * @param maxResults the maximum number of results to return to the client in one call
     * @param size the size of the data set
     * @return a new SimpleListWrapper
     */
    public static <T> SimpleListWrapper<T> of(List<T> list, Integer maxResults, int size)
    {
        return new SimpleListWrapper<T>(list, maxResults, size);
    }

    /**
     * The size of the data set.
     */
    @XmlAttribute (name = "size")
    private int size;

    /**
     * The maximum number of results returned.
     */
    @XmlAttribute (name = "max-results")
    private Integer maxResults;

    /**
     * This private field is never changed from within this class. Instead, it is modified by the REST plugin's expand
     * functionality via Java reflection APIs.
     *
     * @see #pagingCallback
     */
    @XmlElement (name = "items")
    private List<T> items = Collections.emptyList();

    /**
     * The callback used by the REST plugin to expand the items.
     */
    @XmlTransient
    private ListWrapperCallback<T> pagingCallback;

    /**
     * No-arg constructor for use by tools that work using reflection.
     */
    private SimpleListWrapper()
    {
        // necessary for JAXB
    }

    /**
     * Creates a new SimpleListWrapper backed by the given list and returns at most maxResults items to the client.
     *
     * @param list a Collection
     * @param maxResults the maximum number of results to return to the client in one call
     * @param size the size of the data set
     */
    protected SimpleListWrapper(List<T> list, Integer maxResults, int size)
    {
        this.size = size;
        this.maxResults = maxResults;
        this.pagingCallback = ofList(ImmutableList.copyOf(list), maxResults != null ? maxResults : Integer.MAX_VALUE);
    }

    /**
     * Returns a ListWrapperCallback that the REST plugin can use to retrieve elements having specific indexes.
     *
     * @return a ListWrapperCallback<T>
     */
    public ListWrapperCallback<T> getPagingCallback()
    {
        return pagingCallback;
    }

    public int getSize()
    {
        return size;
    }

    public int getMaxResults()
    {
        return maxResults;
    }

    public final ListWrapperCallback<T> getCallback()
    {
        return new ListWrapperCallback<T>()
        {
            public List<T> getItems(Indexes indexes)
            {
                return getPagingCallback().getItems(indexes);
            }
        };
    }

    @Override
    public String toString()
    {
        return "SimpleListWrapper{" +
                "size=" + size +
                ", maxResults=" + maxResults +
                ", pagingCallback=" + pagingCallback +
                ", items=" + items +
                '}';
    }

    /**
     * Returns a new ListWrapperCallback that can be used to selectively retrieve items from the given list.
     *
     * @param items the List to wrap
     * @param maxResults the maximum number of elements that the wrapper will return
     * @return a new ListWrapperCallback
     * @throws NullPointerException if items is null
     * @throws IllegalArgumentException if maxResults is non-negative
     */
    static <T> ListWrapperCallback<T> ofList(final List<T> items, final int maxResults)
    {
        if (items == null) { throw new NullPointerException("items"); }
        if (maxResults < 0) { throw new IllegalArgumentException("maxResults must be non-negative: " + maxResults); }

        return new ListWrapperCallback<T>()
        {
            public List<T> getItems(Indexes indexes)
            {
                if (maxResults == 0)
                {
                    return emptyList();
                }

                int remainingResults = maxResults;
                final List<T> toReturn = Lists.newArrayListWithCapacity(Math.min(items.size(), maxResults));

                for (Integer i : indexes.getIndexes(items.size()))
                {
                    if (remainingResults-- == 0) { break; }
                    toReturn.add(items.get(i));
                }

                return toReturn;
            }
        };
    }
}
