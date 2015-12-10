/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.bean;

import com.atlassian.jira.web.component.IssuePage;

import java.io.Serializable;

public class Page implements Serializable, IssuePage
{
    private int start;
    private PagerFilter pagerFilter;
    private int pageNumber;

    public Page(int start, int pageNumber, PagerFilter pagerFilter)
    {
        this.start = start;
        this.pageNumber = pageNumber;
        this.pagerFilter = pagerFilter;
    }

    public boolean isCurrentPage()
    {
        return pagerFilter.getStart() >= start && pagerFilter.getStart() < start + pagerFilter.getMax();
    }

    public int getStart()
    {
        return start;
    }

    public int getPageNumber()
    {
        return pageNumber;
    }

    public PagerFilter getPagerFilter()
    {
        return pagerFilter;
    }

    public int getNiceStart()
    {
        return getStart() + 1;
    }
}
