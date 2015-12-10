package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.components.menu.JiraAuiDropdownMenu;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;


/**
 * JIRA Administration page to manage Share Dashboards
 */
public class SharedDashboardsPage extends AbstractJiraPage
{
    @ElementBy (id = "search-dashboards-form")
    PageElement searchDashboards;

    @Inject
    protected PageElementFinder finder;

    private String URI = "/secure/admin/filters/ViewSharedDashboards.jspa";

    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return searchDashboards.timed().isPresent();
    }

    public JiraAuiDropdownMenu openOperationsDropdownForDashboard(String filterId) {
        String triggerId = filterId+"_operations";
        String dropdownId = filterId+"_operations_drop";
        JiraAuiDropdownMenu menu = pageBinder.bind(JiraAuiDropdownMenu.class, By.id(triggerId), By.id(dropdownId));
        menu.open();
        return menu;
    }

    public SharedDashboardsPage sortBy(String fieldName) {
        PageElement columnHeader = finder.find(By.id("page_sort_" + fieldName));
        columnHeader.click();
        waitUntilTrue(columnHeader.find(By.tagName("img")).timed().isVisible());
        return this;
    }
}
