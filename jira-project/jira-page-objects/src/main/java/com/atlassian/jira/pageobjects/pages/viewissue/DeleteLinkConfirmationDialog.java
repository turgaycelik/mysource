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
 * Represents the delete link confirmation dialog.
 *
 * @since v5.0
 */
public class DeleteLinkConfirmationDialog extends JiraDialog
{
    private final static String DIALOG_ELEMENT_ID = "delete-issue-link-dialog";
    private final static String DIRTY_FLAG_CLASS = "dirtyFlagClass";

    @Inject
    private PageBinder pageBinder;

    @Inject
    private PageElementFinder locator;

    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private TraceContext traceContext;

    private final String issueKey;

    private PageElement dialog;
    private String title;
    private String message;

    public DeleteLinkConfirmationDialog(String issueKey)
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
        title = dialog.find(By.className(HEADING_AREA_CLASS)).getText();
        message = dialog.find(By.className("aui-message")).getText();
    }

    public String getTitle()
    {
        return title;
    }

    public String getMessage()
    {
        return message;
    }

    public ViewIssuePage confirm()
    {
        PageElement deleteButton = locator.find(By.id("issue-link-delete-submit"));
        driver.executeScript("AJS.$('#" + DIALOG_ELEMENT_ID + "').addClass('" + DIRTY_FLAG_CLASS + "');");
        Tracer tracer = traceContext.checkpoint();
        deleteButton.click();
        Poller.waitUntilFalse(dialog.timed().hasClass(DIRTY_FLAG_CLASS));
        ViewIssuePage viewIssuePage = pageBinder.bind(ViewIssuePage.class, issueKey);
        return viewIssuePage.waitForAjaxRefresh(tracer);
    }

    public ViewIssuePage cancel()
    {
        PageElement cancelLink = locator.find(By.id("issue-link-delete-cancel"));
        cancelLink.click();
        return pageBinder.bind(ViewIssuePage.class, issueKey);
    }
}
