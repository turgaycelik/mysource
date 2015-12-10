package com.atlassian.jira.issue.status.category;

import java.util.Map;

import com.atlassian.jira.config.DefaultStatusCategoryManager;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.mock.MockFeatureManager;
import com.atlassian.jira.workflow.JiraWorkflow;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ResultDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class TestStatusCategoryMapper
{
    private StatusCategoryMapper mapper;
    private StatusCategoryManager statusCategoryManager;

    @Before
    public void setup()
    {
        statusCategoryManager = new DefaultStatusCategoryManager(new MockFeatureManager());
        mapper = new StatusCategoryMapper(statusCategoryManager);
    }

    @Test
    public void testWorkflowWithOneStepShouldBeInTheNewCategory() throws Exception
    {
        Status initialStatus = new MockStatus("1234", "Initial");
        JiraWorkflow workflow = createConfigurableWorkflow(initialStatus);

        Map<String, StatusCategory> results = mapper.mapCategoriesToStatuses(workflow);

        assertThat(results.size(), equalTo(1));
        assertThat(results.values(), Matchers.hasItem(statusCategoryManager.getStatusCategoryByKey(StatusCategory.TO_DO)));
    }

    @Test
    public void testAllStatusesExceptInitialShouldBeInTheIndeterminateCategory() throws Exception
    {
        Status initialStatus = new MockStatus("1234", "Initial");
        JiraWorkflow workflow = createConfigurableWorkflow(initialStatus);
        when(workflow.getLinkedStatusObjects()).thenReturn(Lists.<Status>newArrayList(initialStatus, new MockStatus("a", "One"), new MockStatus("b", "Two"), new MockStatus("c", "Three")));

        Map<String, StatusCategory> results = mapper.mapCategoriesToStatuses(workflow);
        final StatusCategory indeterminate = statusCategoryManager.getStatusCategoryByKey(StatusCategory.IN_PROGRESS);

        assertThat(results.size(), equalTo(4));
        for (Map.Entry<String,StatusCategory> entry : results.entrySet())
        {
            if (entry.getKey().equals(initialStatus.getId()))
            {
                assertThat(entry.getValue(), not(equalTo(indeterminate)));
            }
            else
            {
                assertThat(entry.getValue(), equalTo(indeterminate));
            }
        }
    }

    @Test
    public void testEmptyWorkflowReturnsIncompleteMap()
    {
        Map<String, StatusCategory> results = mapper.mapCategoriesToStatuses(Mockito.mock(JiraWorkflow.class));
        assertThat(results.size(), equalTo(0));
    }

    @Test
    public void testMisconfiguredWorkflowReturnsIncompleteMap()
    {
        Status initialStatus = new MockStatus("1234", "Initial");
        JiraWorkflow workflow = createConfigurableWorkflow(initialStatus);
        when(workflow.getDescriptor()).thenReturn(null);

        Map<String, StatusCategory> results = mapper.mapCategoriesToStatuses(workflow);
        assertThat(results.size(), equalTo(0));
    }

    private static JiraWorkflow createConfigurableWorkflow(Status initialStatus)
    {
        JiraWorkflow workflow = Mockito.mock(JiraWorkflow.class);
        WorkflowDescriptor descriptor = new WorkflowDescriptor();
        when(workflow.getDescriptor()).thenReturn(descriptor);

        final StepDescriptor step = Mockito.mock(StepDescriptor.class);
        when(step.getId()).thenReturn(1);
        when(step.getName()).thenReturn("Open");
        when(step.getMetaAttributes()).thenReturn(ImmutableMap.builder().put(JiraWorkflow.STEP_STATUS_KEY, initialStatus.getId()).build());
        when(step.getParent()).thenReturn(descriptor);
        descriptor.addStep(step);

        // create the initial action
        final ActionDescriptor initialAction = Mockito.mock(ActionDescriptor.class);
        when(initialAction.getId()).thenReturn(1);
        when(initialAction.getName()).thenReturn("Create");
        when(initialAction.getParent()).thenReturn(descriptor);
        descriptor.addInitialAction(initialAction);

        // setup result to always be step 1
        final ResultDescriptor resultDescriptor = Mockito.mock(ResultDescriptor.class);
        when(resultDescriptor.getStep()).thenReturn(1);
        when(resultDescriptor.getStatus()).thenReturn("open");
        when(initialAction.getUnconditionalResult()).thenReturn(resultDescriptor);

        when(workflow.getLinkedStatusObject(step)).thenReturn(initialStatus);
        return workflow;
    }
}
