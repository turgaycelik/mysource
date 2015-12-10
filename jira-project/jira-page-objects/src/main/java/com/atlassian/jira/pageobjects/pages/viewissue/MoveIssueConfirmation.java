package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

/**
 * @since v5.0
 */
public class MoveIssueConfirmation extends AbstractJiraPage
{
    private static final String URI = "/secure/MoveIssueUpdateFields.jspa";

    @ElementBy (id = "move_submit")
    PageElement moveButton;

    @Override
    public TimedCondition isAt()
    {
        return moveButton.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return URI;
    }

    public ViewIssuePage move()
    {
        moveButton.click();

        final String issueKey = elementFinder.find(By.id("key-val")).getText();

        return pageBinder.bind(ViewIssuePage.class, issueKey);
    }
}
