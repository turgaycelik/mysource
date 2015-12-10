package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class PluginsOverviewPage extends  AbstractJiraPage
{
    private static final String URI = "/plugins/servlet/upm";

    @ElementBy(id = "upm-title")
    private PageElement upmTitle;

    public PluginsOverviewPage()
    {
    }

    @Override
    public TimedCondition isAt()
    {
        return upmTitle.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return  URI;
    }
}
