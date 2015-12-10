package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Author: Geoffrey Wong
 * Page to select new status of a JIRA issue being moved
 */
public class MoveIssueUpdateStatus extends AbstractJiraPage
{

    private String URI = "secure/MoveIssueUpdateWorkflow!default.jspa";
    private String issueID = null;
    private String assignee = null;

    @ElementBy (id = "next_submit")
    private PageElement nextButton;

    @ElementBy (name = "beanTargetStatusId")
    private SelectElement statusSelect;
    
    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return nextButton.timed().isPresent();
    }
    
    public MoveIssueUpdateStatus(String issueID, String assignee)
    {

        this.issueID = issueID;

        if (assignee == null)
        {
            URI += "id=" + issueID + "&assignee=null";
        }

        else
        {
            URI += "id=" + issueID + "&assignee=" + assignee;
            this.assignee = assignee;
        }
    }
    
    public MoveIssueUpdateStatus setNewStatus(String newStatus)
    {
        statusSelect.select(Options.text(newStatus));
        return this;
    }
    
    public MoveIssueUpdateFields submitAndGoToUpdateFields()
    {
        nextButton.click();
        return pageBinder.bind(MoveIssueUpdateFields.class, issueID);
    }
    
}
