package com.atlassian.jira.pageobjects.pages.viewissue.linkissue;

import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.utils.JavaScriptUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Represents web link section in the link issue dialog.
 *
 * @since v5.0
 */
public class WebLinkSection
{
    private final LinkIssueDialog linkIssueDialog;

    @Inject
    private PageElementFinder locator;

    @Inject
    private WebDriver driver;

    @Inject
    private TraceContext traceContext;

    @ElementBy(id = "web-issue-link")
    private PageElement form;

    @ElementBy(name = "url")
    private PageElement url;

    @ElementBy(name = "title")
    private PageElement linkText;

    @ElementBy(cssSelector = "#web-issue-link #comment")
    private PageElement comment;

    public WebLinkSection(LinkIssueDialog linkIssueDialog)
    {
        this.linkIssueDialog = linkIssueDialog;
    }

    @WaitUntil
    final public void waitUntil()
    {
         waitUntilTrue(form.timed().isPresent());
    }

    public WebLinkSection url(String url)
    {
        this.url.clear().type(url);
        JavaScriptUtils.dispatchMouseEvent("change", driver.findElement(By.name("url")), driver);
        return this;
    }

    public WebLinkSection linkText(String linkText)
    {
        this.linkText.clear().type(linkText);
        return this;
    }

    public WebLinkSection comment(String comment)
    {
        this.comment.clear().type(comment);
        return this;
    }

    public boolean errorsPresent()
    {
        return locator.find(By.className("error")).isPresent();
    }

    public ViewIssuePage submit()
    {
        // Wait for favicon loading to succeed or fail
        waitUntilFalse(url.timed().hasClass("loading"));

        Tracer tracer = traceContext.checkpoint();
        linkIssueDialog.clickLinkButton();
        return linkIssueDialog.bindViewIssuePage().waitForAjaxRefresh(tracer);
    }

    public WebLinkSection submitExpectingError()
    {
        linkIssueDialog.clickLinkButton();
        return this;
    }
}
