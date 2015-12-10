package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.google.inject.Inject;
import org.openqa.selenium.By;

/**
 * Representation of a single subtask on the view issue page
 *
 * @since v5.0
 */
public class Subtask
{
    @Inject
    PageElementFinder finder;

    @Inject
    PageBinder pageBinder;

    private final PageElement issue;

    private String id;


    public Subtask(final PageElement issue)
    {
        this.issue = issue;
        this.id = issue.getAttribute("rel");
    }

    public String getSummary()
    {
        return issue.find(By.className("stsummary")).getText();
    }

    /**
     * Invoke given <tt>issueOperation</tt> and return the target page object.
     *
     * @param issueOperation issue operation to invoke
     * @param <T> target page objects type
     * @return instance of the target page object
     */
    public <T> T invoke(String issueOperation, Class<T> targetClass, Object... args)
    {
        invokeIssueOperation(issueOperation);
        return pageBinder.bind(targetClass, args);
    }

    private void invokeIssueOperation(String issueOperation)
    {
        PageElement menu = openIssueActionsMenu();
        menu.find(By.className(issueOperation)).click();
    }

    private PageElement openIssueActionsMenu()
    {
        PageElement issueActionsTrigger = issue.find(By.className("issue-actions-trigger"));

        // HACK: Button appears on mouseover, which fails in webdriver, so use javascript to make element appear and be clickable
        issueActionsTrigger.javascript().execute("AJS.$(arguments[0]).addClass('active');");
        Poller.waitUntilTrue("Issue actions trigger is not visible.", issueActionsTrigger.timed().isVisible());

        issueActionsTrigger.click();

        return finder.find(By.id("actions_" + id + "_drop"));
    }
}
