package com.atlassian.jira.pageobjects.components;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import javax.inject.Inject;

public class JiraCommonHeader
{
    @Inject
    protected PageElementFinder elementFinder;

    @Inject
    protected PageBinder pageBinder;

    public TimedCondition isAt()
    {
        return elementFinder.find(By.cssSelector("nav.aui-header")).timed().isPresent();
    }

}
