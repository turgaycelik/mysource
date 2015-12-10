package com.atlassian.jira.web.action;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.PagerFilter;

import webwork.action.ParameterAware;

/**
 * An abstract browser that implements a few helpful things for paging.
 */
public abstract class AbstractBrowser extends IssueActionSupport implements ParameterAware
{
    private static final Collection<String> MAX_VALUES = CollectionBuilder.list("10", "20", "50", "100");

    protected Map params;

    /**
     * Return the current 'page' of items (given max and start) for the current filter
     */
    public abstract List getCurrentPage();

    public abstract List getBrowsableItems();

    public abstract PagerFilter getPager();

    public abstract void resetPager();

    public void setParameters(Map parameters)
    {
        this.params = parameters;
    }

    protected String getSingleParam(String s)
    {
        String[] paramAr = (String[]) params.get(s);
        if (paramAr == null || paramAr.length == 0)
        {
            return null;
        }
        else
        {
            return paramAr[0];
        }
    }

    public void setStart(String start)
    {
        try
        {
            getPager().setStart(Integer.parseInt(start));
        }
        catch (Exception e)
        {
            log.info("Setting start to " + start + " failed. [" + e.getClass().getName() + " " + e.getMessage());
        }
    }

    /**
     * Return the 'readable' start (ie 1 instead of 0).
     */
    public int getNiceStart()
    {
        if (getBrowsableItems() == null || getBrowsableItems().isEmpty())
        {
            return 0;
        }
        return getPager().getStart() + 1;
    }

    /**
     * Return the 'readable' end.
     */
    public int getNiceEnd()
    {
        return getPager().getStart() + getCurrentPage().size();
    }

    /**
     * The different preset values that max can take.
     * <p/>
     * Used by forms to display a select box of max items per page.
     */
    public Collection<String> getMaxValues()
    {
        return MAX_VALUES;
    }
}
