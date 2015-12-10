package com.atlassian.jira.webtest.webdriver.selenium;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.elements.PageElementFinder;

/**
 * Replacement for Navigator that we use in Selenium tests
 *
 * @since v5.1
 */
public class WebDriverNavigator
{
    private JiraTestedProduct jira;
    private IssueNavigation issueNavigation;

    public WebDriverNavigator(JiraTestedProduct jira, PageElementFinder pageElementFinder) {
        this.jira = jira;
        this.issueNavigation = new IssueNavigation(jira, pageElementFinder);
    }

    public IssueNavigation issue() {
        return issueNavigation;
    }

}
