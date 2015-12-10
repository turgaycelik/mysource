package com.atlassian.jira.web.component;

import com.atlassian.jira.web.bean.PagerFilter;

import java.util.Collections;
import java.util.List;

public class AllIssuesIssuePager implements IssuePager
{
    private final List allIssues;
    private final PagerFilter pagerFilter;

    public AllIssuesIssuePager(List allIssues, PagerFilter pagerFilter)
    {
        this.allIssues = allIssues != null ? allIssues : Collections.EMPTY_LIST;
        this.pagerFilter = pagerFilter;
    }

    public int getStart()
    {
        return pagerFilter.getStart();
    }

    public int getEnd()
    {
        return pagerFilter.getEnd();
    }

    public int getTotal()
    {
        return allIssues.size();
    }

    public int getPreviousStart()
    {
        return pagerFilter.getPreviousStart();
    }

    public int getNextStart()
    {
        return pagerFilter.getNextStart();
    }

    public List getPages()
    {
        return pagerFilter.restrictPages(pagerFilter.generatePages(allIssues), allIssues.size());
    }



}
