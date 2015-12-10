package com.atlassian.jira.web.action.admin.workflow.analytics;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;
import com.google.common.annotations.VisibleForTesting;

@PrivacyPolicySafe
public class WorkflowTransitionTabEvent
{
    @VisibleForTesting
    static final String EVENT_BASE_NAME = "administration.workflow.transition.tab.loaded";

    private final String tabName;

    public WorkflowTransitionTabEvent(final String tabName)
    {
        this.tabName = tabName;
    }

    @EventName
    public String buildEventName()
    {
        return String.format("%s.%s", EVENT_BASE_NAME, tabName);
    }

}
