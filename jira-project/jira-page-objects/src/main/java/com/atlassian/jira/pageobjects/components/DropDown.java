package com.atlassian.jira.pageobjects.components;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Simple dropdown that has a "trigger" element that opens a "target" element (generally some kind of menu).
 *
 * @since v5.0
 */
public class DropDown
{
    private final By triggerLocator;
    private final By dropdownLocator;

    @Inject protected PageBinder pageBinder;
    @Inject protected PageElementFinder elementFinder;

    public DropDown(By triggerLocator, By dropdownLocator)
    {
        this.triggerLocator = triggerLocator;
        this.dropdownLocator = dropdownLocator;
    }

    protected PageElement trigger()
    {
        return elementFinder.find(triggerLocator);
    }

    protected PageElement dropDown()
    {
        return elementFinder.find(dropdownLocator);
    }

    public DropDown open()
    {
        if (!isOpen())
        {
            trigger().click();
            waitForOpen();
        }
        return this;
    }

    public <T> T openAndClick(By locator, final Class<T> next, Object...args)
    {
        open();
        return click(locator, next, args);
    }

    public void openAndClick(By locator)
    {
        open();
        getDropDownItem(locator).click();
    }

    public <T> T click(By locator, final Class<T> next, Object...args)
    {
        getDropDownItem(locator).click();
        return pageBinder.bind(next, args);
    }

    public boolean isExists()
    {
        return trigger().isPresent();
    }

    public boolean hasItemById(final String id)
    {
        return hasItemBy(By.id(id));
    }

    public boolean hasItemBy(final By locator)
    {
        if (!isExists())
        {
            return false;
        }

        boolean opened = false;

        if (!isOpen())
        {
            opened = true;
            open();
        }
        boolean present = getDropDownItem(locator).isPresent();

        if (opened)
        {
            close();
        }

        return present;
    }

    private PageElement getDropDownItem(final By locator)
    {
        return dropDown().find(locator);
    }

    public void waitForOpen()
    {
        Poller.waitUntilTrue(dropDown().timed().isVisible());
    }

    public void waitForClose()
    {
        Poller.waitUntilFalse(dropDown().timed().isVisible());
    }

    public boolean isOpen()
    {
        return dropDown().isPresent() && dropDown().isVisible();
    }

    public DropDown close()
    {
        if (isOpen())
        {
            trigger().click();
        }
        return this;
    }
}
