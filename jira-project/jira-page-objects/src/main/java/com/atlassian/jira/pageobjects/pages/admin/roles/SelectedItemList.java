package com.atlassian.jira.pageobjects.pages.admin.roles;

import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.DataAttributeFinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Represents the watcher list on ViewRoleActorActionPage
 *
 * @since v5.2
 */
public class SelectedItemList
{
    @Inject protected PageBinder pageBinder;
    @Inject protected Timeouts timeouts;
    @Inject protected PageElementFinder elementFinder;
    @Inject protected ExtendedElementFinder extendedFinder;

    protected final PageElement container;

    public SelectedItemList(PageElement container)
    {
        this.container = container;
    }


    public TimedQuery<Iterable<SelectedItem>> getSelectedItems()
    {
        return PageElements.transformTimed(timeouts, pageBinder,
                extendedFinder.within(container.find(By.tagName("tbody"))).newQuery(By.tagName("tr")).supplier(),
                SelectedItem.class);
    }

    public SelectedItemList removeSelectedRows()
    {
        container.find(By.className("buttons-container")).find(By.name("remove")).click();
        return this;
    }

    public static class SelectedItem
    {
        protected final PageElement rowElement;

        public SelectedItem(PageElement rowElement)
        {
            this.rowElement = rowElement;
        }

        public SelectedItem selectRow()
        {
            rowElement.find(By.tagName("input")).select();
            return this;
        }

        public SelectedItem unselectRow()
        {
            if (rowElement.find(By.tagName("input")).isSelected())
            {
                rowElement.find(By.tagName("input")).click();
            }
            return this;
        }

        public TimedQuery<String> getRowKey()
        {
            return DataAttributeFinder.query(rowElement).timed().getDataAttribute("row-for");
        }
    }
}
