package com.atlassian.jira.bc.issue.events;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@PrivacyPolicySafe
public class WorkflowManualTransitionExecutionEvent
{
    private static final String BASE_EVENT_NAME = "workflow.manual.issue.transition.execution";
    /**
     * issueKey is for debugging purposes, not sent to analytics server
     */
    @PrivacyPolicySafe (false)
    private final String issueKey;

    @PrivacyPolicySafe (false)
    private final int actionId;

    private final boolean successful;

    public WorkflowManualTransitionExecutionEvent(final String issueKey, final int actionId, final boolean successful)
    {
        this.issueKey = issueKey;
        this.actionId = actionId;
        this.successful = successful;
    }

    public String getIssueKey()
    {
        return issueKey;
    }

    public int getActionId()
    {
        return actionId;
    }

    @EventName
    public String buildEventName()
    {
        return String.format("%s.%s", BASE_EVENT_NAME, (successful ? "successful" : "failed"));
    }
}
