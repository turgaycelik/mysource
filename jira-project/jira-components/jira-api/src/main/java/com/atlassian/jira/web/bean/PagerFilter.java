/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.web.bean;

import com.atlassian.annotations.PublicApi;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This is a super class that implements paging for browsers.
 * <p/>
 * Most other filters (which want paging ability) will extend this.
 */
@PublicApi
public class PagerFilter<T> implements Serializable
{
    private static final int PAGES_TO_LIST = 5;

    // the number of issues per page
    private int max = 20;
    private int start = 0;

    /**
     * A collection of {@link Page} objects
     *
     * @deprecated since 4.0 use #getPages() rather than access pages directly
     */
    protected Collection<Page> pages;

    public PagerFilter()
    {}

    public PagerFilter(final PagerFilter old)
    {
        this.setMax(old.getMax());
        this.setStart(old.getStart());
    }

    public PagerFilter(final int max)
    {
        if (max == -1)
        {
            this.max = Integer.MAX_VALUE;
        }
        else
        {
            this.max = max;
        }
    }

    public PagerFilter(final int start, final int max)
    {
        this(max);
        this.setStart(start);
    }

    /**
     * A pager that will return unlimited number of objects.
     *
     * @return A PagerFilter with a max set to {@link Integer#MAX_VALUE}
     */
    public static PagerFilter getUnlimitedFilter()
    {
        return new PagerFilter(Integer.MAX_VALUE);
    }

    /**
     * A pager that has its start aligned to the page containing the index.
     *
     * @param index the index of a result whose page you want the pager to start at
     * @param max the maximum number of results in a page
     * @return a new pager aligned to the page containing the index
     * @throws IllegalArgumentException if index is less than 0
     */
    public static PagerFilter newPageAlignedFilter(int index, int max)
    {
        if (index < 0)
        {
            throw new IllegalArgumentException(String.format("index %d is less than 0.", index));
        }
        if (max == 0)
        {
            return new PagerFilter(index, max);
        }
        return new PagerFilter(index - (index % max), max);
    }

    /**
     * Gets the current page out of a list of objects.
     *
     * @return the sublist that is the current page.
     */
    public List<T> getCurrentPage(final List<T> itemsCol)
    {
        List<T> items;

        if (itemsCol == null)
        {
            items = new ArrayList<T>(); // should never call this but just incase!
        }
        else
        {
            items = itemsCol;
        }

        if (items.isEmpty())
        {
            start = 0;
            return Collections.emptyList();
        }

        // now return the appropriate page of issues
        // now make sure that the start is valid
        if (start >= items.size())
        {
            start = 0;
            return items.subList(0, Math.min(max, items.size()));
        }
        else
        {
            return items.subList(start, Math.min(start + max, items.size()));
        }
    }

    public List<Page> getPages(final Collection<T> itemsCol)
    {
        if (pages == null)
        {
            pages = generatePages(itemsCol);
        }

        return restrictPages(pages, itemsCol.size());
    }

    protected Collection<Page> getPages()
    {
        return pages;
    }

    /**
     * generates a collection of page objects which keep track of the pages for display
     *
     * @param items
     */
    public List<Page> generatePages(final Collection<T> items)
    {
        if ((items == null) || items.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<Page> pages = new ArrayList<Page>();

        final int total = items.size();

        int pageNumber = 1;
        for (int index = 0; index < total; index += max)
        {
            pages.add(new Page(index, pageNumber, this));
            pageNumber++;
        }

        return Collections.unmodifiableList(pages);
    }

    /**
     * Restrict the pagers to a certain number of pages on either side of the current page.
     * <p/>
     * The number of pages to list is stored in {@link #PAGES_TO_LIST}.
     */
    public List<Page> restrictPages(final Collection<Page> pages, final int size)
    {
        final List<Page> pagesToDisplay = new ArrayList<Page>(2 * PAGES_TO_LIST);

        // enhance the calculation so that at least
        // PAGES_TO_LIST-1 pages are always shown
        //
        // calculate sliding window
        final int maxpage = (size + max - 1) / max; // 1 .. n
        int firstpage = 1; // 1 .. n
        int lastpage = firstpage + PAGES_TO_LIST + PAGES_TO_LIST - 2; // 1 .. n
        if (lastpage < maxpage)
        {
            final int ourpage = (getStart() / max) + 1; // 1 .. n
            if (ourpage - firstpage > PAGES_TO_LIST - 1)
            {
                lastpage = ourpage + PAGES_TO_LIST - 1;
                if (lastpage > maxpage)
                {
                    lastpage = maxpage;
                }
                firstpage = lastpage - PAGES_TO_LIST - PAGES_TO_LIST + 2;
            }
        }
        else if (lastpage > maxpage)
        {
            lastpage = maxpage;
        }

        final int minstart = (firstpage - 1) * max;
        final int maxstart = (lastpage - 1) * max;
        for (final Page page : pages)
        {
            if (page.getStart() <= size)
            {
                final boolean largerThanMin = page.getStart() >= minstart;
                final boolean smallerThanMax = page.getStart() <= maxstart;
                if (largerThanMin && smallerThanMax)
                {
                    pagesToDisplay.add(page);
                }
            }
        }
        return Collections.unmodifiableList(pagesToDisplay);
    }

    public int getMax()
    {
        return max;
    }

    public int getPageSize()
    {
        return getMax();
    }

    public void setMax(final int max)
    {
        if (this.max != max)
        {
            pages = null;
        }
        this.max = max;
    }

    public int getStart()
    {
        return start;
    }

    public void setStart(final int start)
    {
        this.start = start;
    }

    public int getEnd()
    {
        return Math.max(start + max, max);
    }

    public int getNextStart()
    {
        return Math.max(start + max, max);
    }

    public int getPreviousStart()
    {
        return Math.max(0, start - max);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("start", getStart()).append("end", getEnd()).append("max", getMax()).toString();
    }
}
