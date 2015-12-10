package com.atlassian.jira.pageobjects.pages;


import com.atlassian.jira.pageobjects.components.DropDown;
import com.atlassian.jira.pageobjects.components.JiraHeader;
import com.atlassian.jira.pageobjects.gadgets.GadgetContainer;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.page.HomePage;
import org.openqa.selenium.By;

/**
 * Page object implementation for the Dashbaord page in JIRA.
 * 
 */
public class DashboardPage extends AbstractJiraPage implements HomePage<JiraHeader>
{
    private static final String URI = "/secure/Dashboard.jspa";

    @ElementBy(className = "page-type-dashboard")
    protected PageElement dashboardBody;

    protected DropDown profileDropdown;

    @Init
    public void initialise()
    {
        profileDropdown = pageBinder.bind(DropDown.class, By.cssSelector("#header-details-user .drop"), By.id("user-options-list"));
    }

    @Override
    public TimedCondition isAt()
    {
        return dashboardBody.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return URI;
    }

    public GadgetContainer gadgets()
    {
        return pageBinder.bind(GadgetContainer.class);
    }

}
