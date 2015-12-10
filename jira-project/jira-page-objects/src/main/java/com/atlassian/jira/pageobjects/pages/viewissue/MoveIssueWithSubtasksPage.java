package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.navigator.BulkOperationProgressPage;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 *
 * @since v5.0
 */
public class MoveIssueWithSubtasksPage extends AbstractJiraPage
{
    private static final String URI = "/secure/MoveIssue!default.jspa";

    private final String issueKey;

    @ElementBy (id = "next")
    protected PageElement nextButton;

    @ElementBy (cssSelector = ".project-ss .text")
    protected PageElement chooseProject;

    @ElementBy (cssSelector = ".issuetype-ss .text")
    protected PageElement chooseIssueType;

    public MoveIssueWithSubtasksPage(String issueKey)
    {
        this.issueKey = issueKey;
    }

    @Override
    public TimedCondition isAt()
    {
        return nextButton.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return URI + "?key=" + issueKey;
    }

    public MoveIssueWithSubtasksPage setNewProject(String newProject)
    {
        chooseProject.clear();
        chooseProject.type(newProject);
        return this;
    }

    public MoveIssueWithSubtasksPage setIssueType(String issueType)
    {
        chooseIssueType.clear();
        chooseIssueType.type(issueType);
        return this;
    }

    public MoveIssueWithSubtasksPage next()
    {
        nextButton.click();

        return pageBinder.bind(MoveIssueWithSubtasksPage.class, issueKey);
    }

    public BulkOperationProgressPage move()
    {
        nextButton.click();

        return pageBinder.bind(BulkOperationProgressPage.class);
    }

    public MoveIssueUpdateStatus submitAndGoToSetNewIssueStatus(String issueID, String assignee)
    {
        nextButton.click();
        return pageBinder.bind(MoveIssueUpdateStatus.class, issueID, assignee);
    }

}
