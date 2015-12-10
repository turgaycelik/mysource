package com.atlassian.jira.pageobjects.framework;

import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Finder for message boxes.
 *
 * @since v4.4
 */
public class MessageBoxFinder
{
    // TODO might want to make message a class

    @Inject
    private PageElementFinder elementFinder;

    public PageElement find(MessageType type, By parentLocator)
    {
        return find(type, elementFinder.find(parentLocator));
    }

    public PageElement find(MessageType type, PageElement parent)
    {
        return parent.find(By.cssSelector(".message." + type.cssClass()));
    }

    public PageElement find(MessageType type)
    {
        return find(type, elementFinder.find(By.tagName("body")));
    }
}
