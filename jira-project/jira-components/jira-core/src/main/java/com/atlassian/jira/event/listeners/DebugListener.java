/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.listeners;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.JiraEvent;
import com.atlassian.jira.event.issue.AbstractIssueEventListener;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.user.UserEvent;
import com.atlassian.jira.event.user.UserEventListener;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A listener for debugging - prints everything it 'hears' to System.err
 * <p>
 * Useful for debuggin when certain events are fired, and the content of those
 * events.
 */
public class DebugListener extends AbstractIssueEventListener implements UserEventListener
{
    public void init(Map params)
    {
        log("DebugListener.init");
        log(params.toString());
    }

    public String[] getAcceptedParams()
    {
        return new String[0];
    }

    // IssueEventListener implementation
    public void issueAssigned(IssueEvent event)
    {
        log("DebugListener.issueAssigned");
        logEvent(event);
    }

    public void issueClosed(IssueEvent event)
    {
        log("DebugListener.issueClosed");
        logEvent(event);
    }

    public void issueCreated(IssueEvent event)
    {
        log("DebugListener.issueCreated");
        logEvent(event);
    }

    public void issueResolved(IssueEvent event)
    {
        log("DebugListener.issueResolved");
        logEvent(event);
    }

    public void issueReopened(IssueEvent event)
    {
        log("DebugListener.issueReopened");
        logEvent(event);
    }

    public void issueUpdated(IssueEvent event)
    {
        log("DebugListener.issueUpdated");

        try
        {
            List changeItems = event.getChangeLog().getRelated("ChildChangeItem");
            Iterator iter = changeItems.iterator();
            while (iter.hasNext())
            {
                GenericValue changeItem = (GenericValue) iter.next();
                log("\t"+changeItem.get("field") + " changed from '"+changeItem.get("oldstring")+"' to '"+changeItem.get("newstring") + "'");
            }
        }
        catch (GenericEntityException e)
        {
            log("Error: "+e);
        }
        logEvent(event);
    }

    public void issueCommented(IssueEvent event)
    {
        log("DebugListener.issueCommented");
        logEvent(event);
    }

    public void issueDeleted(IssueEvent event)
    {
        log("DebugListener.issueCommented");
        logEvent(event);
    }

    public void issueMoved(IssueEvent event)
    {
        log("DebugListener.issueMoved");
        logEvent(event);
    }

    public void issueWorkLogged(IssueEvent event)
    {
        log("DebugListener.issueWorkLogged");
        logEvent(event);
    }

    public void issueGenericEvent(IssueEvent event)
    {
        log("DebugListener.issueGenericEvent");
        logEvent(event);
    }

    public void customEvent(IssueEvent event)
    {
        log("DebugListener.customEvent");
        logEvent(event);
    }

    // UserEventListener implementation
    public void userSignup(UserEvent event)
    {
        log("DebugListener.userSignup");
        logEvent(event);
    }

    public void userCreated(UserEvent event)
    {
        log("DebugListener.userCreated");
        logEvent(event);
    }

    public void userForgotPassword(UserEvent event)
    {
        log("DebugListener.userForgotPassword");
        logEvent(event);
    }

    public void userForgotUsername(UserEvent event)
    {
        log("DebugListener.userForgotUsername");
        logEvent(event);
    }

    public void userCannotChangePassword(UserEvent event)
    {
        log("DebugListener.userCannotChangePassword");
        logEvent(event);
    }

    private void logEvent(JiraEvent event)
    {
        try
        {
            if (event instanceof IssueEvent)
            {
                IssueEvent issueEvent = (IssueEvent) event;
                log("Issue: [#" + issueEvent.getIssue().getLong("id") + "] " + issueEvent.getIssue().getString("summary"));
                log("Comment: " + issueEvent.getComment());
                log("Change Group: " + issueEvent.getChangeLog());
                log("Event Type: " + ComponentAccessor.getEventTypeManager().getEventType(issueEvent.getEventTypeId()).getName());
            }
            else if (event instanceof UserEvent)
            {
                UserEvent userEvent = (UserEvent) event;
                log("User: " + userEvent.getUser().getName() + " (" + userEvent.getUser().getEmailAddress() + ")");
            }

            log(" Time: " + event.getTime());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void log(String msg)
    {
        System.err.println("[DebugListener]: " + msg);
    }

    public boolean isInternal()
    {
        return false;
    }

    public boolean isUnique()
    {
        return false;
    }

    public String getDescription()
    {
        return getI18NBean().getText("admin.listener.debug.desc");
    }
}
