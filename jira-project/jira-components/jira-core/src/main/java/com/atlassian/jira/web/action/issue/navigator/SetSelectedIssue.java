package com.atlassian.jira.web.action.issue.navigator;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import com.atlassian.jira.web.session.SessionSelectedIssueManager;
import com.atlassian.jira.web.session.SessionSelectedIssueManager.SelectedIssueData;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

/**
 * Ajax action to update the selected issue in the issue navigator (for the current search).
 *
 * @since v4.2
 */
public class SetSelectedIssue extends JiraWebActionSupport
{
    private Long selectedIssueId;
    private Long nextIssueId;
    private int selectedIssueIndex;
    private String nextIssueKey;

    @Override
    @RequiresXsrfCheck
    public String execute()
    {
        if (selectedIssueId != null)
        {
            if (nextIssueKey != null)
            {
                IssueManager issueManager = ComponentAccessor.getComponentOfType(IssueManager.class);
                MutableIssue issueObject = issueManager.getIssueObject(nextIssueKey);
                nextIssueId = issueObject.getId();
            }

            final SessionSearchObjectManagerFactory factory = ComponentAccessor.getComponentOfType(SessionSearchObjectManagerFactory.class);
            final SessionSelectedIssueManager manager = factory.createSelectedIssueManager(ActionContext.getRequest());
            manager.setCurrentObject(new SelectedIssueData(selectedIssueId, selectedIssueIndex, nextIssueId));
        }
        /**
         * MAKE SURE WE NEVER, EVER, WRITE ANY DATA OTHERWISE ITS NOT A 204, OTHERWISE APACHE AJP I WILL HUNT YOU DOWN LIKE A DOG!
         */
        ServletActionContext.getResponse().setStatus(204);
        return NONE;
    }

    public Long getSelectedIssueId()
    {
        return selectedIssueId;
    }

    public void setSelectedIssueId(final Long selectedIssueId)
    {
        this.selectedIssueId = selectedIssueId;
    }

    public void setSelectedIssueIndex(final int selectedIssueIndex)
    {
        this.selectedIssueIndex = selectedIssueIndex;
    }

    public void setNextIssueId(final Long nextIssueId)
    {
        this.nextIssueId = nextIssueId;
    }

    public void setNextIssueKey(final String nextIssueKey)
    {
        this.nextIssueKey = nextIssueKey;
    }
}
