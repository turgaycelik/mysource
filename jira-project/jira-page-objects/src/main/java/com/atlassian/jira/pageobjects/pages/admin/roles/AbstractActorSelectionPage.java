package com.atlassian.jira.pageobjects.pages.admin.roles;

import com.atlassian.jira.pageobjects.components.userpicker.LegacyPicker;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

/**
 * Class for user and group role actor action pages in the admin section.
 *
 * @since v5.2
 */
public abstract class AbstractActorSelectionPage extends AbstractJiraPage
{
    @ElementBy(className = "buttons-container")
    protected PageElement buttonsContainer;
    @ElementBy(className = "watcher-list")
    protected PageElement watcherListContainer;

    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.id(pickerId())).timed().isPresent();
    }

    protected abstract String pickerId();

    public LegacyPicker getPicker()
    {
        return pageBinder.bind(LegacyPicker.class, pickerId());
    }

    public void add()
    {
        buttonsContainer.find(By.name("add")).click();
    }

    public SelectedItemList selectedItemList()
    {
        return pageBinder.bind(SelectedItemList.class, watcherListContainer);
    }
}
