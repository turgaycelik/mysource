package com.atlassian.jira.ofbiz;

import com.atlassian.jira.exception.DataAccessException;
import org.ofbiz.core.entity.EntityListIterator;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class DefaultOfBizListIterator implements OfBizListIterator
{
    private static final Logger log = LoggerFactory.getLogger(DefaultOfBizListIterator.class);
    private final EntityListIterator iterator;

    public DefaultOfBizListIterator(final EntityListIterator iterator)
    {
        this.iterator = iterator;

    }

    public void setDelegator(final GenericDelegator genericDelegator)
    {
        iterator.setDelegator(genericDelegator);
    }

    public boolean isCaseSensitive(final String fieldName)
    {
        try
        {
            return iterator.isCaseSensitive(fieldName);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public void afterLast()
    {
        try
        {
            iterator.afterLast();
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public void beforeFirst()
    {
        try
        {
            iterator.beforeFirst();
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public boolean last()
    {
        try
        {
            return iterator.last();
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public boolean first()
    {
        try
        {
            return iterator.first();
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public void close()
    {
        try
        {
            iterator.close();
        }
        catch (final GenericEntityException e)
        {
            log.error("Error closing iterator: " + iterator, e);
        }
    }

    public GenericValue currentGenericValue()
    {
        try
        {
            return iterator.currentGenericValue();
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public int currentIndex()
    {
        try
        {
            return iterator.currentIndex();
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public boolean absolute(final int i)
    {
        try
        {
            return iterator.absolute(i);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public GenericValue next()
    {
        return iterator.next();
    }

    public int nextIndex()
    {
        return iterator.nextIndex();
    }

    public GenericValue previous()
    {
        return iterator.previous();
    }

    public int previousIndex()
    {
        return iterator.previousIndex();
    }

    public void setFetchSize(final int i)
    {
        try
        {
            iterator.setFetchSize(i);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public List<GenericValue> getCompleteList()
    {
        try
        {
            return iterator.getCompleteList();
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public List<GenericValue> getPartialList(final int i, final int i1)
    {
        try
        {
            return iterator.getPartialList(i, i1);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public void add(final GenericValue o)
    {
        iterator.add(o);
    }

    public void remove()
    {
        iterator.remove();
    }

    public void set(final GenericValue o)
    {
        iterator.set(o);
    }

    @Override
    public Iterator<GenericValue> iterator()
    {
        return new LookaheadIterator(iterator);
    }

    /**
     * Wrapper around the OfBiz EntityListIterator that works with <em>forward-only</em> (i.e. non-scrollable) result
     * sets. This iterator works by getting one result from the database before clients ask for it, and comparing that
     * to {@code null} in order to determine if there are more results in the result set.
     */
    private static final class LookaheadIterator implements Iterator<GenericValue>
    {
        private final Iterator<GenericValue> delegate;
        private GenericValue nextResult;

        private LookaheadIterator(Iterator<GenericValue> iterator)
        {
            delegate = iterator;
            nextResult = delegate.next();
        }

        @Override
        public boolean hasNext()
        {
            return nextResult != null;
        }

        @Override
        public GenericValue next()
        {
            GenericValue next = nextResult;
            nextResult = delegate.next();

            return next;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
