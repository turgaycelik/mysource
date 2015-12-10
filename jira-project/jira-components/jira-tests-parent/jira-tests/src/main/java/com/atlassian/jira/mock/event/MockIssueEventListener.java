/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock.event;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventListener;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.mock.MockListenerManager;

import java.util.ArrayList;
import java.util.Collection;

public class MockIssueEventListener extends MockListener implements IssueEventListener
{
    Collection calls = new ArrayList();

    public MockIssueEventListener(MockListenerManager mlm)
    {
        mlm.addListener("mock", this);
    }

    public void issueCreated(IssueEvent event)
    {
        calls.add(new Object[]{"issueCreated", event});
    }

    public void issueUpdated(IssueEvent event)
    {
        throw new UnsupportedOperationException();
    }

    public void issueAssigned(IssueEvent event)
    {
        calls.add(new Object[]{"issueAssigned", event});
    }

    public void issueResolved(IssueEvent event)
    {
        calls.add(new Object[]{"issueResolved", event});
    }

    public void issueClosed(IssueEvent event)
    {
        calls.add(new Object[]{"issueClosed", event});
    }

    public void issueCommented(IssueEvent event)
    {
        throw new UnsupportedOperationException();
    }

    public void issueReopened(IssueEvent event)
    {
        calls.add(new Object[]{"issueReopened", event});
    }

    public void issueDeleted(IssueEvent event)
    {
        calls.add(new Object[]{"issueDeleted", event});
    }

    public void issueMoved(IssueEvent event)
    {
        calls.add(new Object[]{"issueMoved", event});
    }

    public void issueWorkLogged(IssueEvent event)
    {
        throw new UnsupportedOperationException();
    }

    public Collection getCalls()
    {
        return calls;
    }

    public void issueStarted(IssueEvent event)
    {
        calls.add(new Object[]{"issueStarted", event});
    }

    public void issueStopped(IssueEvent event)
    {
        calls.add(new Object[]{"issueStopped", event});
    }

    public void issueGenericEvent(IssueEvent event)
    {
        calls.add(new Object[]{"issueGenericEvent", event});
    }

    public void workflowEvent(IssueEvent event)
    {
        EventTypeManager eventTypeManager = ComponentAccessor.getEventTypeManager();

        Long eventTypeId = event.getEventTypeId();
        EventType eventType = eventTypeManager.getEventType(eventTypeId);

        if (eventType == null)
        {
            throw new RuntimeException("Event Type with ID '" + eventTypeId + "' is not recognised.");
        }

        if (eventTypeId.equals(EventType.ISSUE_CREATED_ID))
            issueCreated(event);
        else if (eventTypeId.equals(EventType.ISSUE_UPDATED_ID))
            issueUpdated(event);
        else if (eventTypeId.equals(EventType.ISSUE_ASSIGNED_ID))
            issueAssigned(event);
        else if (eventTypeId.equals(EventType.ISSUE_RESOLVED_ID))
            issueResolved(event);
        else if (eventTypeId.equals(EventType.ISSUE_COMMENTED_ID))
            issueCommented(event);
        else if (eventTypeId.equals(EventType.ISSUE_CLOSED_ID))
            issueClosed(event);
        else if (eventTypeId.equals(EventType.ISSUE_REOPENED_ID))
            issueReopened(event);
        else if (eventTypeId.equals(EventType.ISSUE_DELETED_ID))
            issueDeleted(event);
        else if (eventTypeId.equals(EventType.ISSUE_MOVED_ID))
            issueMoved(event);
        else if (eventTypeId.equals(EventType.ISSUE_WORKLOGGED_ID))
            issueWorkLogged(event);
        else if (eventTypeId.equals(EventType.ISSUE_WORKSTARTED_ID))
            issueStarted(event);
        else if (eventTypeId.equals(EventType.ISSUE_WORKSTOPPED_ID))
            issueStopped(event);
        else if (eventTypeId.equals(EventType.ISSUE_GENERICEVENT_ID))
            issueGenericEvent(event);
        else
            customEvent(event);
    }

    public void customEvent(IssueEvent event)
    {
        calls.add(new Object[]{"customEvent", event});
    }
}
