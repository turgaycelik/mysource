package com.atlassian.jira.entity;

import com.atlassian.jira.util.collect.PagedList;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class PagedListIterator<E> implements Iterator<List<E>>
{
    private final PagedList<E> target;
    private final int size;
    private final int pageSize;
    private int currentPage;
    private int currentListCount;



    PagedListIterator(final PagedList<E> target)
    {
        this.target = target;
        size = target.getSize();
        pageSize = target.getPageSize();
    }

    @Override
    public boolean hasNext()
    {
        return (currentListCount < size);
    }

    @Override
    public List<E> next()
    {
        if (!this.hasNext())
        {
            throw new NoSuchElementException("There are no more pages to iterate over");
        }
        List<E> itemsInPage = target.getPage(currentPage + 1);
        currentPage++;
        currentListCount += pageSize;
        return itemsInPage;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("Remove not supported");
    }
}
