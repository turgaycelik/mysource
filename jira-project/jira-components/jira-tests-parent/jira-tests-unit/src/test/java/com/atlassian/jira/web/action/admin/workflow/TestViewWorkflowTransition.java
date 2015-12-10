package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.web.action.admin.workflow.analytics.WorkflowTransitionTabEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith (MockitoJUnitRunner.class)
public class TestViewWorkflowTransition
{
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ViewWorkflowTransition viewWorkflowTransition;

    @Test
    public void shouldRaiseAnalyticsWhenTabIsLoadedInSetDescriptorTab()
    {
        viewWorkflowTransition.setDescriptorTab("triggers");
        verify(eventPublisher).publish(any(WorkflowTransitionTabEvent.class));
    }
}
