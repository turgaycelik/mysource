package com.atlassian.jira.event.listeners.history;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.AbstractIssueEventListener;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import org.apache.log4j.Logger;

/**
 * Keep a history of whom a user assigns issues to.
 *
 * @since v4.3
 */
public class IssueAssignHistoryListener extends AbstractIssueEventListener
{
    private static final Logger log = org.apache.log4j.Logger.getLogger(IssueAssignHistoryListener.class);

    @Override
    public void issueAssigned(final IssueEvent event)
    {
        User user = event.getUser();
        User assignee = event.getIssue().getAssignee();

        if (assignee != null && user != null)
        {
            UserHistoryManager userHistoryManager = ComponentAccessor.getComponent(UserHistoryManager.class);
            userHistoryManager.addUserToHistory(UserHistoryItem.ASSIGNEE, user, assignee);
            userHistoryManager.addUserToHistory(UserHistoryItem.USED_USER, user, assignee);
        }
    }

	@Override
    public boolean isInternal()
    {
	    return true;
    }
    
}

