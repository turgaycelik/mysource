package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.Page;

/**
 * A page with tabs.
 *
 * @since v4.4
 */
public interface TabPage<T extends Tab> extends Page
{
    /**
     * Open tab of given type.
     *
     * @param tabClass type of the tab
     * @param <TT> tab parameter of the tab
     * @return open tab
     */
    <TT extends T> TT openTab(Class<TT> tabClass);

    /**
     * Check whether this page has given tab.
     *
     * @param tabClass tab class
     * @return <code>true</code>, if this page has the tab
     */
    boolean hasTab(Class<? extends T> tabClass);
}
