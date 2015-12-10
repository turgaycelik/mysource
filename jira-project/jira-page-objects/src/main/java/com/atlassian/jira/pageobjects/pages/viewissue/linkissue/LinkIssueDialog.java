package com.atlassian.jira.pageobjects.pages.viewissue.linkissue;

import com.atlassian.jira.pageobjects.components.IssuePicker;
import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Represents the link issue dialog shown on the issue page.
 *
 * @since v5.0
 */
public class LinkIssueDialog extends FormDialog
{
    private final String issueKey;

    @Inject
    private PageBinder pageBinder;

    @ElementBy(id = "add-jira-issue-link-link")
    private PageElement jiraLink;

    @ElementBy(id = "add-confluence-page-link-link")
    private PageElement confluenceLink;

    @ElementBy(id = "add-web-link-link")
    private PageElement webLinkLink;

    public LinkIssueDialog(String issueKey)
    {
        super("link-issue-dialog");
        this.issueKey = issueKey;
    }

    public IssuePicker issuePicker()
    {
        return pageBinder.bind(IssuePicker.class, "jira-issue-keys");
    }

    public boolean hasJiraLink()
    {
        return jiraLink.isPresent();
    }

    public LinkJiraSection gotoJiraLink()
    {
        jiraLink.click();
        return pageBinder.bind(LinkJiraSection.class, this);
    }

    public boolean hasConfluenceLink()
    {
        return confluenceLink.isPresent();
    }

    public LinkConfluenceSection gotoConfluenceLink()
    {
        confluenceLink.click();
        return pageBinder.bind(LinkConfluenceSection.class, this);
    }

    public WebLinkSection gotoWebLink()
    {
        webLinkLink.click();
        return pageBinder.bind(WebLinkSection.class, this);
    }

    @Override
    protected boolean submit(By locator)
    {
        return super.submit(locator);
    }

    protected ViewIssuePage bindViewIssuePage()
    {
        return pageBinder.bind(ViewIssuePage.class, issueKey);
    }

    public void clickLinkButton()
    {
        submit(By.name("Link"));
    }

    public PageElement getErrorPageElement()
    {
        return this.find(By.className("error"));
    }


}
