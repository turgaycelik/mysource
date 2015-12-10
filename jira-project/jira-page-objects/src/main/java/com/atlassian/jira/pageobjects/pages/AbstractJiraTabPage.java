package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

/**
 * Abstract implementation of {@link com.atlassian.jira.pageobjects.pages.TabPage}.
 *
 * @since v4.4
 */
public abstract class AbstractJiraTabPage<T extends Tab> extends AbstractJiraPage implements TabPage<T>
{
    /**
     * Default tab links container in JIRA.
     *
     */
    public static final By DEFAULT_TAB_CONTAINER_SELECTOR = By.cssSelector("ul.vertical.tabs");

    private final By tabLinksContainerLocator;

    protected PageElement tabLinksContainer;
    private TabSupport<T> tabSupport;

    public AbstractJiraTabPage()
    {
        this(DEFAULT_TAB_CONTAINER_SELECTOR);
    }

    public AbstractJiraTabPage(By tabLinksContainerLocator)
    {
        this.tabLinksContainerLocator = tabLinksContainerLocator;
    }

    @Init
    @SuppressWarnings("unchecked")
    public void init()
    {
        tabLinksContainer = elementFinder.find(tabLinksContainerLocator);
        tabSupport = pageBinder.bind(TabSupport.class, tabLinksContainer);
    }

    protected <TT extends T> Object[] argsForTab(Class<TT> tabClass)
    {
        return new Object[0];
    }

    @Override
    public final <TT extends T> TT openTab(Class<TT> tabClass)
    {
        return tabSupport.openTab(tabClass, argsForTab(tabClass));
    }

    @Override
    public boolean hasTab(Class<? extends T> tabClass)
    {
        return tabSupport.hasTab(tabClass);
    }
}
