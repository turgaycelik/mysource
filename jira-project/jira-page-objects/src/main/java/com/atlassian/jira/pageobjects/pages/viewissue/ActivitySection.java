package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Activity Module of View Issue page
 *
 * @since v5.2
 */
public class ActivitySection
{
    @Inject PageElementFinder pageElementFinder;

    @Inject PageBinder binder;

    @ElementBy (id = "activitymodule")
    private PageElement module;

    private String issueKey;

    public ActivitySection(final String issueKey)
    {
        this.issueKey = issueKey;
    }

    @WaitUntil
    final public void ready()
    {
        waitUntilTrue(module.timed().isPresent());
        waitUntilFalse(module.timed().hasClass("updating"));
    }

    public HistoryModule historyModule()
    {
        module.find(By.id("changehistory-tabpanel")).click();
        Poller.waitUntilTrue("History tab is not active.", module.find(By.id("changehistory-tabpanel")).timed().hasClass("active"));
        return binder.bind(HistoryModule.class);
    }

    public CommentsModule commentsModule()
    {
        module.find(By.id("comment-tabpanel")).click();
        Poller.waitUntilTrue("Comments tab is not active.", module.find(By.id("comment-tabpanel")).timed().hasClass("active"));
        return binder.bind(CommentsModule.class, issueKey);
    }

}
