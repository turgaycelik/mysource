package com.atlassian.jira.pageobjects.components.menu;

import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.pageobjects.elements.PageElement;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import static com.atlassian.pageobjects.elements.query.Conditions.not;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * @since v5.0
 */
public class IssuesMenu extends JiraAuiDropdownMenu
{

    public IssuesMenu()
    {
        super(By.id("find_link"), By.id("find_link-content"));
    }

    public IssuesMenu open()
    {
        super.open();
        waitUntilLoadComplete();
        return this;
    }

    public List<String> getRecentIssues()
    {
        final List<String> issues = new ArrayList<String>();
        for (final PageElement element : getElementsForRecentIssues())
        {
            if (!element.getAttribute("id").equals("issue_lnk_more_lnk"))
            {
                issues.add(StringUtils.trim(element.getText()));
            }
        }
        return issues;
    }

    public List<PageElement> getElementsForRecentIssues() {
        return getDropdown().findAll(By.cssSelector("#issues_history_main a"));
    }

    /**
     * Click the "Search for issues" item in the issues dropdown.
     */
    public void searchForIssues()
    {
        getDropdown().find(By.id("issues_new_search_link_lnk")).click();
    }

    public void manageFilters() {
        getDropdown().find(By.id("issues_manage_filters_link_lnk")).click();
    }

    public boolean isMoreIssuesVisible() {
        return !getDropdown().findAll(By.id("issue_lnk_more_lnk")).isEmpty();
    }

    public void openMoreIssues() {
        getDropdown().find(By.id("issue_lnk_more_lnk")).click();
    }

    public List<PageElement> getElementsForFilters()
    {
        return getDropdown().findAll(By.cssSelector("#issues_filter_main a"));
    }

    public void waitUntilLoadComplete()
    {
        waitUntilTrue(not(getDropdown().timed().hasClass("aui-dropdown2-loading")));
    }

    public List<String> getRecentSavedSearches()
    {
        final List<String> searches = Lists.newArrayList();
        for (final PageElement element : getElementsForFilters())
        {
            if (!element.getAttribute("id").equals("filter_lnk_more_lnk"))
            {
                searches.add(StringUtils.trim(element.getText()));
            }
        }
        return searches;
    }
}