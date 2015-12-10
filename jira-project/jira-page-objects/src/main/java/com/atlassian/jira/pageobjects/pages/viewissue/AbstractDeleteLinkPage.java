package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.openqa.selenium.By;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Base class of a delete link page. Normally the user would not see this page since clicking the delete icon on the
 * view issue page will show an inline dialog.
 *
 * @since v5.0
 */
public abstract class AbstractDeleteLinkPage extends AbstractJiraPage
{
    private final String issueKey;
    private final Long issueId;

    @ElementBy (className = "dialog-title")
    private PageElement titleElement;

    @ElementBy (id = "issue-link-delete-submit")
    private PageElement deleteButton;

    @ElementBy (id = "issue-link-delete-cancel")
    private PageElement cancelLink;

    protected AbstractDeleteLinkPage(String issueKey, Long issueId)
    {
        this.issueKey = notNull(issueKey);
        this.issueId = notNull(issueId);
    }

    @Override
    public TimedCondition isAt()
    {
        TimedCondition dialogTitleStartsWithDeleteLink = Conditions.forMatcher(elementFinder.find(By.className("dialog-title")).timed().getText(), Matchers.startsWith("Delete Link:"));
        return Conditions.or(dialogTitleStartsWithDeleteLink,
                             elementFinder.find(By.className("form-body")).timed().isPresent());
    }

    public ViewIssuePage confirm()
    {
        deleteButton.click();
        return pageBinder.bind(ViewIssuePage.class, issueKey);
    }

    public ViewIssuePage cancel()
    {
        cancelLink.click();
        return pageBinder.bind(ViewIssuePage.class, issueKey);
    }

    public String getTitle()
    {
        return titleElement.getText();
    }

    public String getMessage()
    {
        return elementFinder.find(By.className("aui-message")).getText();
    }

    /**
     * Returns the error messages on the page. An empty list is returned if there are no error messages.
     *
     * @return error messages on the page. An empty list is returned if there are no error messages.
     */
    public List<String> getErrorMessages()
    {
        List<PageElement> auiMessages = elementFinder.findAll(By.className("aui-message"));
        List<String> errorMessages = Lists.newArrayList();
        for (PageElement auiMessage : auiMessages)
        {
            if (auiMessage.hasClass("error"))
            {
                errorMessages.add(auiMessage.getText());
            }
        }
        return errorMessages;
    }

    /**
     * Returns the issue ID.
     *
     * @return issue ID
     */
    public Long getIssueId()
    {
        return issueId;
    }
}
