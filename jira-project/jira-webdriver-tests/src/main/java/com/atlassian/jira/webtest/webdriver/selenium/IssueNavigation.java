package com.atlassian.jira.webtest.webdriver.selenium;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.pageobjects.elements.PageElementFinder;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.1
 */
public class IssueNavigation
{
    private JiraTestedProduct jira;
    private PseudoSeleniumClient client;

    public IssueNavigation(JiraTestedProduct jira, PageElementFinder pageElementFinder) {
        this.jira = jira;
        this.client = new PseudoSeleniumClient(jira, pageElementFinder);
    }

    public void editIssue(String issueKey) {
        jira.visit(ViewIssuePage.class, issueKey);
        client.open(client.getAttribute("edit-issue@href"));
        client.waitForPageToLoad();
    }

    public ViewIssuePage viewIssue(String issueKey) {
        return jira.visit(ViewIssuePage.class, issueKey);
    }
}
