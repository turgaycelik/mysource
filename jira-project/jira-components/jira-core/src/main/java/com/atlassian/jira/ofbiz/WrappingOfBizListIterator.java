package com.atlassian.jira.ofbiz;

import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericValue;

import java.util.Iterator;
import java.util.List;

import static com.atlassian.jira.ofbiz.IssueGenericValueFactory.wrap;

/**
 * @since v6.1
 */
class WrappingOfBizListIterator implements OfBizListIterator
{
    final private OfBizListIterator delegate;

    WrappingOfBizListIterator(final OfBizListIterator delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public void setDelegator(final GenericDelegator genericDelegator)
    {
        delegate.setDelegator(genericDelegator);
    }

    @Override
    public void afterLast()
    {
        delegate.afterLast();
    }

    @Override
    public void beforeFirst()
    {
        delegate.beforeFirst();
    }

    @Override
    public boolean last()
    {
        return delegate.last();
    }

    @Override
    public boolean first()
    {
        return delegate.first();
    }

    @Override
    public void close()
    {
        delegate.close();
    }

    @Override
    public GenericValue currentGenericValue()
    {
        return wrap(delegate.currentGenericValue());
    }

    @Override
    public int currentIndex()
    {
        return delegate.currentIndex();
    }

    @Override
    public boolean absolute(final int i)
    {
        return delegate.absolute(i);
    }

    @Override
    public GenericValue next()
    {
        return wrap(delegate.next());
    }

    @Override
    public int nextIndex()
    {
        return delegate.nextIndex();
    }

    @Override
    public GenericValue previous()
    {
        return wrap(delegate.previous());
    }

    @Override
    public int previousIndex()
    {
        return delegate.previousIndex();
    }

    @Override
    public void setFetchSize(final int i)
    {
        delegate.setFetchSize(i);
    }

    @Override
    public List<GenericValue> getCompleteList()
    {
        return wrap(delegate.getCompleteList());
    }

    @Override
    public List<GenericValue> getPartialList(final int i, final int i1)
    {
        return wrap(delegate.getPartialList(i, i1));
    }

    @Override
    public void add(final GenericValue o)
    {
        delegate.add(o);
    }

    @Override
    public void remove()
    {
        delegate.remove();
    }

    @Override
    public void set(final GenericValue o)
    {
        delegate.set(o);
    }

    @Override
    public boolean isCaseSensitive(final String fieldName)
    {
        return delegate.isCaseSensitive(fieldName);
    }

    @Override
    public Iterator<GenericValue> iterator()
    {
        return new WrappingIterator(delegate.iterator());
    }
}
