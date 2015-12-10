package com.atlassian.jira.pageobjects.pages.viewissue.linkissue;

import com.atlassian.jira.pageobjects.components.fields.MultiSelect;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.CheckboxElement;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Option;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.SelectElement;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Represents jira section in the link issue dialog.
 *
 * @since v5.0
 */
public class LinkJiraSection
{
    private final LinkIssueDialog linkIssueDialog;

    private MultiSelect issuePicker;

    @Inject
    private PageElementFinder locator;

    @Inject
    private PageBinder pageBinder;

    @ElementBy(id = "link-jira-issue")
    private PageElement form;

    @ElementBy(id = "link-type")
    private SelectElement linkTypeSelect;

    @ElementBy(id = "jira-app-link")
    private SelectElement serverSelect;

    @ElementBy(id = "create-reciprocal")
    private CheckboxElement createReciprocalCheckbox;

    @ElementBy(cssSelector = "#web-issue-link #comment")
    private PageElement commentTextArea;

    public LinkJiraSection(LinkIssueDialog linkIssueDialog)
    {
         this.linkIssueDialog = linkIssueDialog;
    }

    @WaitUntil
    final public void waitUntil()
    {
         waitUntilTrue(form.timed().isPresent());
    }

    @Init
    private void initialize()
    {
        issuePicker = pageBinder.bind(MultiSelect.class, "jira-issue-keys");
    }

    public LinkJiraSection selectServer(final String appId)
    {
        final Option option = Options.value(appId);
        serverSelect.select(option);
        return this;
    }

    public LinkJiraSection setIssueKey(String issueKey)
    {
        issuePicker.clearAllItems();
        issuePicker.add(issueKey);
        return this;
    }

    public LinkJiraSection clearIssueKeys()
    {
        issuePicker.clearAllItems();
        return this;
    }

    public LinkJiraSection addIssueKey(String issueKey)
    {
        issuePicker.add(issueKey);
        return this;
    }

    public LinkJiraSection createReciprocal()
    {
        createReciprocalCheckbox.check();
        return this;
    }

    public LinkJiraSection comment(String comment)
    {
        commentTextArea.clear().type(comment);
        return this;
    }

    public boolean errorsPresent()
    {
        return locator.find(By.className("error")).isPresent();
    }

    public int getErrorCount()
    {
        int count = 0;
        for (final PageElement errorContainer : locator.findAll(By.className("error")))
        {
            if (errorContainer.isPresent())
            {
                // An error container may contain several messages, each in a <p> tag
                final List<PageElement> elements = errorContainer.findAll(By.tagName("p"));
                if (elements == null || elements.isEmpty())
                {
                    count++;
                }
                else
                {
                    count += elements.size();
                }
            }
        }
        
        return count;
    }

    public boolean requireCredentials()
    {
        return form.find(By.className("applinks-auth-request")).isPresent();
    }

    /**
     * Starts the OAuth dance.
     *
     * TODO: Should be changed to return the AppLink OAuthConfirmPage when JIRA upgrades to APL 3.7.
     */
    public void startOAuthDance()
    {
        form.find(By.className("applink-authenticate")).click();
    }

    public ViewIssuePage submit()
    {
        linkIssueDialog.clickLinkButton();
        if (linkIssueDialog.isOpen().now() && locator.find(By.name("atl_token_retry_button")).isPresent())
        {
            linkIssueDialog.submit(By.name("atl_token_retry_button"));
        }
        return linkIssueDialog.bindViewIssuePage();
    }

    public LinkJiraSection submitExpectingError()
    {
        linkIssueDialog.clickLinkButton();
        return this;
    }
}
