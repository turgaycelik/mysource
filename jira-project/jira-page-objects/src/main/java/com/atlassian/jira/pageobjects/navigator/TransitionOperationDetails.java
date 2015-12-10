package com.atlassian.jira.pageobjects.navigator;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Edit the fields for a bulk transition
 *
 * @since v6.0
 */
public class TransitionOperationDetails extends AbstractJiraPage
{
    @ElementBy (id = "bulkedit")
    private PageElement bulkeditForm;

    @ElementBy (id = "next")
    protected PageElement next;

    @Override
    public TimedCondition isAt()
    {
        return bulkeditForm.timed().isVisible();
    }

    @Override
    public String getUrl()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<PageElement> screenTabs()
    {
        return elementFinder.findAll(By.cssSelector(".aui-tabs .menu-item"));
    }

    public PageElement getCurrentScreenPanel()
    {
        return elementFinder.find(By.cssSelector(".tabs-pane.active-pane"));
    }

    public PageElement switchToScreenTab(final String screenName)
    {
        elementFinder.find(By.linkText(screenName)).click();
        return getCurrentScreenPanel();
    }

    public TransitionOperationConfirmation submit()
    {
        next.click();
        return pageBinder.bind(TransitionOperationConfirmation.class);
    }
}
