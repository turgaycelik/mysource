package com.atlassian.jira.pageobjects.pages.viewissue.linkissue;

import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Represents "Wiki Page" section in the link issue dialog.
 * (to add pages to linked Confluence instances)
 * @since v5.0
 */

public class LinkConfluenceSection
{
    private final LinkIssueDialog linkIssueDialog;

    @Inject
    private PageBinder pageBinder;

    @Inject
    private PageElementFinder locator;

    @Inject
    private WebDriver driver;

    @ElementBy (id = "confluence-page-link")
    private PageElement form;

    @ElementBy (id = "confluence-page-url")
    private PageElement pageUrl;

    @ElementBy (id = "confluence-page-search")
    private PageElement searchLink;

    @ElementBy (cssSelector= "#confluence-page-link #comment")
    private PageElement comment;

    public LinkConfluenceSection(LinkIssueDialog linkIssueDialog)
    {
        this.linkIssueDialog = linkIssueDialog;
    }

    @WaitUntil
    final public void waitUntil()
    {
         waitUntilTrue(form.timed().isPresent());
    }

    public LinkConfluenceSection pageUrl(String url)
    {
        this.pageUrl.clear().type(url);
        return this;
    }

    public LinkConfluenceSection comment(String comment)
    {
        this.comment.clear().type(comment);
        return this;
    }

    public SearchConfluenceDialog searchForPage()
    {
        this.searchLink.click();
        return pageBinder.bind(SearchConfluenceDialog.class, this);
    }

    public boolean errorsPresent()
    {
        return locator.find(By.className("error")).isPresent();
    }

    public ViewIssuePage submit()
    {
        linkIssueDialog.clickLinkButton();
        return linkIssueDialog.bindViewIssuePage();
    }

    public LinkConfluenceSection submitExpectingError()
    {
        linkIssueDialog.clickLinkButton();
        return this;
    }

    public LinkIssueDialog getLinkIssueDialog()
    {
        return linkIssueDialog;
    }

    public PageElement getForm()
    {
        return form;
    }
}
