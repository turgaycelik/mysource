package com.atlassian.jira.pageobjects.navigator;

import com.atlassian.jira.pageobjects.components.menu.JiraAuiDropdownMenu;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Object for interacting with the Tools Menu.
 */
public class IssueNavigatorToolsMenu
{
    @Inject
    PageBinder pageBinder;

    @Inject
    protected PageElementFinder finder;

    private JiraAuiDropdownMenu toolsMenu;

    @Init
    public void initialise()
    {
        toolsMenu = pageBinder.bind(JiraAuiDropdownMenu.class, By.className("header-tools"), By.className("header-tools-menu"));
    }

    public IssueNavigatorToolsMenu open()
    {
        toolsMenu.open();
        return this;
    }

    public boolean isOpen()
    {
        return toolsMenu.isOpen();
    }

    public IssueNavigatorToolsMenu close()
    {
        toolsMenu.close();
        return this;
    }

    public BulkEdit bulkChange() {
        finder.find(By.id("bulkedit_all")).click();
        return pageBinder.bind(BulkEdit.class);
    }
}
