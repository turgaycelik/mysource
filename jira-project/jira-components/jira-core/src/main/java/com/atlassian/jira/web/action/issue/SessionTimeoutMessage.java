package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import webwork.action.ActionContext;

/**
 * This is a simple error screen which displays a message about a session timeout.
 */
public class SessionTimeoutMessage extends JiraWebActionSupport
{
    public String getErrorMessage()
    {
        return (String) ActionContext.getSession().get(SessionKeys.SESSION_TIMEOUT_MESSAGE);
    }
}
