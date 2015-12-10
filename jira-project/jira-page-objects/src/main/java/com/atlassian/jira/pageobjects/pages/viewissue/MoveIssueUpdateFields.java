package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

/**
 * @since v5.0
 */
public class MoveIssueUpdateFields extends AbstractJiraPage
{
    private static String URI = "/secure/MoveIssueUpdateFields.jspa!default.jspa";

    @ElementBy (id = "next_submit")
    private PageElement nextButton;

    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.cssSelector("form[action='MoveIssueUpdateFields.jspa']")).timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return URI;
    }

    public MoveIssueUpdateFields()
    {
        // Do nothing
    }
    
    public MoveIssueUpdateFields(String issueID)
    {
        URI += "?id=" + issueID;
    }
    
    public MoveIssueConfirmation next()
    {
        nextButton.click();

        final String issueKey = (String) driver.executeScript("AJS.Meta.get('issue-key')");

        return pageBinder.bind(MoveIssueConfirmation.class);
    }
}
