package com.atlassian.jira.pageobjects.components.menu;

import com.atlassian.jira.pageobjects.components.DropDown;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * The JIRA version of the AUI dropdown.
 * https://extranet.atlassian.com/x/VocUc
 *
 * @since v5.0
 */
public class JiraAuiDropdownMenu<T extends JiraAuiDropdownMenu<T>> extends DropDown
{
    @Inject protected PageElementFinder finder;
    @Inject protected PageBinder pageBinder;

    protected PageElement triggerElement;

    /**
     * @param triggerLocator The locator to the trigger element
     * @param dropdownLocator The locator to the dropdown element
     */
    public JiraAuiDropdownMenu(By triggerLocator, By dropdownLocator)
    {
        super(triggerLocator, dropdownLocator);
    }

    @Init
    public void initialize()
    {
        triggerElement = super.trigger();
    }

    /**
     * Opens dropdown by clicking on trigger element
     */
    public T open()
    {
        super.open();
        //noinspection unchecked
        return (T) this;
    }

    /**
     * Gets dropdown page element
     */
    protected PageElement getDropdown()
    {
        return super.dropDown();
    }

    /**
     * Closes dropdown by clicking trigger element again (toggling state)
     */
    public T close()
    {
        super.close();
        //noinspection unchecked
        return (T) this;
    }

    public void waitUntilOpen()
    {
        super.waitForOpen();
    }

    public void waitUntilClose()
    {
        super.waitForClose();
    }

}
