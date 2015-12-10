package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.DelayedBinder;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.atlassian.jira.util.dbc.Assertions.stateTrue;

/**
 * Support for tabbed components.
 *
 * @since v4.4
 */
public final class TabSupport<T extends Tab>
{

    private final PageElement tabLinkContainer;

    @Inject
    private PageBinder pageBinder;

    public TabSupport(PageElement tabLinkContainer)
    {
        this.tabLinkContainer = tabLinkContainer;
    }

    <TT extends T> TT openTab(Class<TT> tabClass, Object... args)
    {
        final DelayedBinder<TT> tab = pageBinder.delayedBind(tabClass, args);
        final PageElement link = tabLinkContainer.find(By.id(tab.inject().get().linkId()));
        stateTrue("Link " + link + " for tab " + tabClass.getName() + " not found", link.isPresent());
        link.click();
        final TT realTab = tab.bind();
        waitUntilTrue(realTab.isOpen());
        return realTab;
    }

    public boolean hasTab(Class<? extends T> tabClass)
    {
        final DelayedBinder<? extends T> tab = pageBinder.delayedBind(tabClass);
        final PageElement link = tabLinkContainer.find(By.id(tab.inject().get().linkId()));
        return link.isPresent();
    }

}
