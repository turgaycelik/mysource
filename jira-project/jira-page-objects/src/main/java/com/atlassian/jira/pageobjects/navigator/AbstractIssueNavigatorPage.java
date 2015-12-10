package com.atlassian.jira.pageobjects.navigator;

import com.atlassian.jira.pageobjects.components.JiraAjsDropdown;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.openqa.selenium.By;

/**
 * Abstract base class for issue navigator pages
 *
 * @since 5.2
 */
public abstract class AbstractIssueNavigatorPage extends AbstractJiraPage
{
    public enum NavigatorMode
    {
        BASIC("basic"),
        ADVANCED("advanced");
        private final String val;

        private NavigatorMode(String val)
        {
            this.val = val;
        }

        public String toString()
        {
            return val;
        }
    }
    public enum ViewMode
    {
        LIST("list-view"),
        DETAIL("split-view");
        private final String val;

        private ViewMode(String val)
        {
            this.val = val;
        }

        public String toString()
        {
            return val;
        }
    }

    @ElementBy(id = "search-header-view")
    private PageElement searchHeader;

    @ElementBy(className = "search-button")
    protected PageElement searchButton;

    @ElementBy(className = "navigator-content")
    protected PageElement mainContent;

    @ElementBy(id = "issuetable", within = "mainContent")
    protected PageElement resultsWrap;

    @ElementBy (className = "mode-switcher")
    protected PageElement searchSwitcher;

    /** @deprecated find within {@link #searchSwitcher} instead. */
    @ElementBy(cssSelector = "a.switcher-item.active", within = "searchSwitcher")
    protected PageElement modeSwitcher;

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(
                searchHeader.timed().isPresent(),
                searchSwitcher.withTimeout(TimeoutType.COMPONENT_LOAD).timed().isPresent(),
                mainContent.timed().isPresent()
        );
    }

    protected TimedQuery<String> toggleSearchMode(final NavigatorMode toMode)
    {
        final PageElement activeSwitcher = searchSwitcher.find(By.className("active"));
        if (!activeSwitcher.getAttribute("data-id").equals(toMode.toString()))
        {
            activeSwitcher.click();
        }
        return searchSwitcher.find(By.className("active")).timed().getAttribute("data-id");
    }

    protected TimedQuery<Boolean> toggleNavigatorView(final ViewMode toView)
    {
        if (elementFinder.find(By.id("layout-switcher-button")).isPresent())
        {
            final JiraAjsDropdown layoutSwitcher = pageBinder.bind(JiraAjsDropdown.class, "layout-switcher-button");
            layoutSwitcher.openAndClick(By.cssSelector(".aui-list-item-link[data-layout-key='"+toView+"']"));
            return mainContent.find(By.className(toView.toString())).withTimeout(TimeoutType.AJAX_ACTION).timed().isPresent();
        }
        else
        {
            // It could have been disabled by the ka.KILL_SWITCH dark feature. let's not panic, people!
            return hasResults();
        }
    }

    public TimedCondition hasResults()
    {
        return resultsWrap.timed().isPresent();
    }

    public IssueNavigatorResults getResults()
    {
        return pageBinder.bind(IssueNavigatorResults.class);
    }


    public IssueNavigatorToolsMenu toolsMenu()
    {
        return pageBinder.bind(IssueNavigatorToolsMenu.class);
    }

    public BasicSearch switchToSimple()
    {
        return pageBinder.bind(BasicSearch.class);
    }

    public AdvancedSearch switchToAdvanced()
    {
        return pageBinder.bind(AdvancedSearch.class);
    }

}
