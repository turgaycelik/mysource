package com.atlassian.jira.mock.web.action;

import java.util.List;
import java.util.Map;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.jira.web.action.AbstractBrowser;
import com.atlassian.jira.web.bean.PagerFilter;

import org.ofbiz.core.entity.GenericEntityException;

public class MockAbstractBrowser extends AbstractBrowser
{
    private PagerFilter pager;

    public List getCurrentPage()
    {
        return getPager().getCurrentPage(getBrowsableItems());
    }

    public List getBrowsableItems()
    {
        try
        {
            return CoreFactory.getGenericDelegator().findAll("Issue");
        }
        catch (GenericEntityException e)
        {
            e.printStackTrace(); //To change body of catch statement use Options | File Templates.
            throw new UnsupportedOperationException();
        }
    }

    public PagerFilter getPager()
    {
        if (pager == null)
        {
            pager = new PagerFilter();
        }
        return pager;
    }

    public void resetPager()
    {
    }

    public Map getParameters()
    {
        return super.params;
    }
}
