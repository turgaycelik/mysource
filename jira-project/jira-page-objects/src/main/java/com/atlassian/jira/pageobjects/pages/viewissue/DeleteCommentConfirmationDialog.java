package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.dialogs.JiraDialog;
import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Represents the delete comment confirmation dialog.
 *
 * @since v5.0
 */
public class DeleteCommentConfirmationDialog extends JiraDialog
{
    private final static String DIALOG_ELEMENT_ID = "delete-comment-dialog";
    private final static String DIRTY_FLAG_CLASS = "dirtyFlagClass";

    @Inject
    private PageBinder pageBinder;

    @Inject
    private PageElementFinder locator;

    @Inject
    private TraceContext traceContext;

    private final String issueKey;

    private PageElement dialog;

    public DeleteCommentConfirmationDialog(String issueKey)
    {
        this.issueKey = issueKey;
    }

    @WaitUntil
    public void waitUntilPageReady()
    {
        Poller.waitUntilTrue(locator.find(By.id(DIALOG_ELEMENT_ID)).timed().isVisible());
    }

    @Init
    public void init()
    {
        dialog = locator.find(By.id(DIALOG_ELEMENT_ID));
    }

    public ViewIssuePage confirm()
    {
        PageElement deleteButton = locator.find(By.id("comment-delete-submit"));
        deleteButton.javascript().execute("AJS.$('#" + DIALOG_ELEMENT_ID + "').addClass('" + DIRTY_FLAG_CLASS + "');");
        Tracer tracer = traceContext.checkpoint();
        deleteButton.click();
        Poller.waitUntilFalse(dialog.timed().hasClass(DIRTY_FLAG_CLASS));
        ViewIssuePage viewIssuePage = pageBinder.bind(ViewIssuePage.class, issueKey);
        return viewIssuePage.waitForAjaxRefresh(tracer);
    }

    public ViewIssuePage cancel()
    {
        PageElement cancelLink = locator.find(By.id("comment-delete-cancel"));
        cancelLink.click();
        return pageBinder.bind(ViewIssuePage.class, issueKey);
    }
}
