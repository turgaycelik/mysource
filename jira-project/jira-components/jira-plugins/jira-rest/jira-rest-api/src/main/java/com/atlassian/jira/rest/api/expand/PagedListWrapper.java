package com.atlassian.jira.rest.api.expand;


import com.atlassian.plugins.rest.common.expand.Expandable;
import com.atlassian.plugins.rest.common.expand.entity.ListWrapper;
import com.atlassian.plugins.rest.common.expand.entity.ListWrapperCallback;
import com.atlassian.plugins.rest.common.expand.parameter.Indexes;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Collection;
import java.util.List;

/**
 * This ListWrapper will use any expansion varibales to restrict the size of the list. You pass expansion variables like
 * ?expand=sharedUsers[10:40] This returns a ListWrapper with an offset of 10 and an end index of 40. The indexes are
 * bound within the range of size
 *
 * @since v6.0
 */
public abstract class PagedListWrapper<T, Z> implements ListWrapper<T>
{
    @XmlAttribute
    protected int size;

    @XmlAttribute (name = "max-results")
    private final int maxResults;

    @XmlAttribute (name = "start-index")
    protected int startIndex;

    @XmlAttribute (name = "end-index")
    protected int endIndex;

    @XmlAttribute (name = "items")
    @Expandable
    private final Collection<T> items = Lists.newArrayList();

    // this is for JAXB
    private PagedListWrapper()
    {
        size = 0;
        maxResults = 0;
        startIndex = 0;
        endIndex = 0;
    }

    // this is for PagedListWrapperDocExample
    private PagedListWrapper(int maxResults, int startIndex, int endIndex, Collection<T> items)
    {
        this.maxResults = maxResults;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.items.addAll(items);
        this.size = this.items.size();
    }

    public PagedListWrapper(int size, int maxResults)
    {
        this.size = size;
        this.maxResults = maxResults;
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


    public ListWrapperCallback<T> getPagingCallback()
    {
        return new ListWrapperCallback<T>()
        {

            final List<T> pagedBeans = Lists.newArrayList();

            @Override
            public List<T> getItems(Indexes indexes)
            {
                size = getBackingListSize();
                if (size > 0)
                {
                    startIndex = indexes.getMinIndex(getSize());
                    if (startIndex >= 0)
                    {
                        endIndex = calculateEndIndex(indexes);
                        final List<Z> sortedBackingObjects = getOrderedList(startIndex, endIndex);
                        for (Z backingObject : sortedBackingObjects)
                        {
                            pagedBeans.add(fromBackedObject(backingObject));
                        }
                    }
                }
                return pagedBeans;
            }

            private int calculateEndIndex(Indexes indexes)
            {
                // we are 0-based here
                final int maxIndex = indexes.getMaxIndex(getSize());
                final int resultsSize = maxIndex - startIndex + 1;
                final int maxResultsSize = getMaxResults();
                if (resultsSize > maxResultsSize)
                {
                    final int maxAllowedEndIdex = startIndex + maxResultsSize - 1;
                    return Math.min(maxAllowedEndIdex, getSize() - 1);
                }
                else
                {
                    return maxIndex;
                }
            }
        };
    }

    public abstract T fromBackedObject(Z backedObject);

    public abstract int getBackingListSize();

    public abstract List<Z> getOrderedList(int startIndex, int endIndex);

    /**
     * This class is for documentation purpose only, do not use it.
     */
    public static class PagedListWrapperDocExample<T, Z> extends PagedListWrapper<T, Z>
    {
        public PagedListWrapperDocExample(int maxResults, int startIndex, int endIndex, Collection<T> items)
        {
            super(maxResults, startIndex, endIndex, items);
        }

        @Override
        public T fromBackedObject(Z backedObject)
        {
            throw new UnsupportedOperationException("This class is for documentation purpose only, do not use it.");
        }

        @Override
        public int getBackingListSize()
        {
            throw new UnsupportedOperationException("This class is for documentation purpose only, do not use it.");
        }

        @Override
        public List<Z> getOrderedList(int startIndex, int endIndex)
        {
            throw new UnsupportedOperationException("This class is for documentation purpose only, do not use it.");
        }
    }
}
