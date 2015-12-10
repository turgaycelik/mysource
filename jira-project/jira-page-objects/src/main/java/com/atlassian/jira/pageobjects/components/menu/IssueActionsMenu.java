package com.atlassian.jira.pageobjects.components.menu;

import com.atlassian.jira.pageobjects.model.IssueOperation;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import org.openqa.selenium.By;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Cog dropdown
 *
 * @since 5.0
 */
public class IssueActionsMenu extends JiraAuiDropdownMenu
{

    /**
     * @param triggerLocator selector for item that when clicked will invoke dropdown
     * @param dropdownLocator selector for the dropdown itself
     */
    public IssueActionsMenu(By triggerLocator, By dropdownLocator)
    {
        super(triggerLocator, dropdownLocator);
    }

    @Override
    @Init
    public void initialize() {
        super.initialize();
        waitUntilTrue(triggerElement.timed().hasClass("trigger-happy"));
    }

    /**
     * Opens dropdown
     */
    @Override
    public IssueActionsMenu open()
    {
        super.open();
        return this;
    }

    /**
     * Clicks specified item/action in the menu
     *
     * @param menuItem - Item/Action to be clicked in the menu
     * @deprecated use {@link #clickItem(com.atlassian.jira.pageobjects.model.IssueOperation)} instead
     */
    public IssueActionsMenu clickItem(final IssueActions menuItem)
    {
        getDropdown().find(menuItem.getSelector()).click();
        return this;
    }

    /**
     * Clicks specified item/action in the menu
     *
     * @param issueOperation - Item/Action to be clicked in the menu
     */
    public IssueActionsMenu clickItem(final IssueOperation issueOperation)
    {
        getDropdown().find(By.className(issueOperation.cssClass())).click();
        return this;
    }
}
