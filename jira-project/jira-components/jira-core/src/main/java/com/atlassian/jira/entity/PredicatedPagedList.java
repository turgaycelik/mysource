package com.atlassian.jira.entity;

import com.atlassian.jira.util.collect.PagedList;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.ofbiz.core.entity.EntityCondition;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Warps a Predicate around the EntityPagedList
 *
 * @since v6.1
 */
public class PredicatedPagedList<E> implements PagedList<E>
{
    final Predicate<E> predicate;
    final PagedList<E> delegate;

    public PredicatedPagedList(final PagedList<E> delegate, final Predicate<E> predicate)
    {
        this.predicate = predicate;
        this.delegate = delegate;
    }

    @Override
    public List<E> getPage(final int pageNumber)
    {
        List<E> unfilteredList = delegate.getPage(pageNumber);
        return Lists.newArrayList(Iterables.filter(unfilteredList, predicate));
    }

    @Override
    public Iterator<List<E>> iterator()
    {
        return new PagedListIterator<E>(this);
    }

    @Override
    public List<E> getCompleteList()
    {
        List<E> unfilteredList = delegate.getCompleteList();
        return Lists.newArrayList(Iterables.filter(unfilteredList, predicate));
    }

    @Override
    public int getSize()
    {
        return delegate.getSize();
    }

    @Override
    public int getPageSize()
    {
        return delegate.getPageSize();
    }
}
