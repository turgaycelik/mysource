package com.atlassian.jira.web.action.admin.workflow.analytics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestWorkflowTransitionTabEvent
{
    @Test
    public void testBuildEventName()
    {
        WorkflowTransitionTabEvent event = new WorkflowTransitionTabEvent("triggers");
        assertEquals(WorkflowTransitionTabEvent.EVENT_BASE_NAME + ".triggers", event.buildEventName());
    }
}
