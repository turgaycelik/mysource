package com.atlassian.jira.mock.ofbiz;

import com.atlassian.jira.ofbiz.OfBizListIterator;

import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericValue;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MockOfBizListIterator implements OfBizListIterator
{
    private final ListIterator<GenericValue> iterator;

    public MockOfBizListIterator(final List<GenericValue> list)
    {
        iterator = list.listIterator();
    }

    public GenericValue next()
    {
        if (iterator.hasNext())
        {
            return iterator.next();
        }
        return null;
    }

    public boolean isCaseSensitive(final String fieldName)
    {
        return false;
    }

    public GenericValue previous()
    {
        return iterator.previous();
    }

    public int nextIndex()
    {
        return iterator.nextIndex();
    }

    public int previousIndex()
    {
        return iterator.previousIndex();
    }

    public void remove()
    {
        iterator.remove();
    }

    public void set(final GenericValue o)
    {
        iterator.set(o);
    }

    public void add(final GenericValue o)
    {
        iterator.add(o);
    }

    public void setDelegator(final GenericDelegator genericDelegator)
    {

    }

    public void afterLast()
    {

    }

    public void beforeFirst()
    {

    }

    public boolean last()
    {
        return false;
    }

    public boolean first()
    {
        return false;
    }

    public void close()
    {

    }

    public GenericValue currentGenericValue()
    {
        return null;
    }

    public int currentIndex()
    {
        return 0;
    }

    public boolean absolute(final int i)
    {
        return false;
    }

    public void setFetchSize(final int i)
    {

    }

    public List<GenericValue> getCompleteList()
    {
        return null;
    }

    public List<GenericValue> getPartialList(final int i, final int i1)
    {
        return null;
    }

    @Override
    public Iterator<GenericValue> iterator()
    {
        return iterator;
    }
}
