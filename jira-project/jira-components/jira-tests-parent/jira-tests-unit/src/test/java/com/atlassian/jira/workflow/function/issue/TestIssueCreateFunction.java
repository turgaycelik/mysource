/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import java.sql.Timestamp;
import java.util.Map;

import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.workflow.MockWorkflowEntry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.spi.Step;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.opensymphony.workflow.spi.WorkflowStore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.InOrder;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestIssueCreateFunction
{
    private IssueCreateFunction icf;
    private Map<String, Object> transientVars;
    @Mock
    private MutableIssue issue;

    @Mock
    @AvailableInContainer
    private FieldManager fieldManager;

    @Mock
    @AvailableInContainer
    private ProjectManager projectManager;

    @Rule
    public RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);
    private MockProject project;

    @Before
    public void setUp() throws Exception
    {

        icf = new IssueCreateFunction();


        WorkflowEntry wfe = new MockWorkflowEntry(123, "test workflowName", true);
        Step mockStep = mock(Step.class);
        when(mockStep.getStepId()).thenReturn(456);
        WorkflowStore mockStore = mock(WorkflowStore.class);
        when(mockStore.findCurrentSteps(123)).thenReturn(ImmutableList.of(mockStep));


        final StepDescriptor stepDescriptor = DescriptorFactory.getFactory().createStepDescriptor();
        stepDescriptor.setId(456);
        stepDescriptor.setMetaAttributes(ImmutableMap.of("jira.status.id", "TestStatus"));
        final WorkflowDescriptor workflowDescriptor = DescriptorFactory.getFactory().createWorkflowDescriptor();
        workflowDescriptor.addStep(stepDescriptor);

        transientVars = ImmutableMap.of(
                "issue", issue,
                "entry", wfe,
                "store", mockStore,
                "descriptor", workflowDescriptor

        );

        project = new MockProject(77L, "PRO");
        when(issue.getProjectObject()).thenReturn(project);
    }


    @Test
    public void shouldInitializeTimestampsAndVotesWhenTheyAreNull() throws WorkflowException
    {
        when(issue.getCreated()).thenReturn(null);
        when(issue.getUpdated()).thenReturn(null);
        when(issue.getVotes()).thenReturn(null);
        when(issue.getWatches()).thenReturn(null);

        icf.execute(transientVars, null, null);
        verify(issue).setCreated(any(Timestamp.class));
        verify(issue).setUpdated(any(Timestamp.class));
        verify(issue).setVotes(0L);
        verify(issue).setWatches(0L);
    }

    @Test
    public void shouldDoNotTouchGivenTimestampsAndVotes() throws WorkflowException
    {
        when(issue.getCreated()).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(issue.getUpdated()).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(issue.getVotes()).thenReturn(12L);
        when(issue.getWatches()).thenReturn(42L);

        icf.execute(transientVars, null, null);
        verify(issue, never()).setCreated(any(Timestamp.class));
        verify(issue, never()).setUpdated(any(Timestamp.class));
        verify(issue, never()).setVotes(anyLong());
        verify(issue, never()).setWatches(anyLong());
    }

    @Test
    public void shouldSetIssueAndWorklfowParamsAndThenSaveAndResetStatus() throws WorkflowException
    {
        when(projectManager.getNextId(project)).thenReturn(321L);
        icf.execute(transientVars, null, null);


        InOrder setStoreResetOrder = inOrder(issue);

        // these three should not be checked "in order" :(
        // but there is no way to check calls "at first all of these and then..."
        setStoreResetOrder.verify(issue).setKey("PRO-321");
        setStoreResetOrder.verify(issue).setWorkflowId(123L);
        setStoreResetOrder.verify(issue).setStatusId("TestStatus");

        setStoreResetOrder.verify(issue).store();
        setStoreResetOrder.verify(issue).resetModifiedFields();
    }


    @Test
    public void shouldPersistUpdateForEachField() throws WorkflowException
    {

        OrderableField abstractField = mock(OrderableField.class);
        OrderableField dummyField = mock(OrderableField.class);

        when(issue.getModifiedFields()).thenReturn(ImmutableMap.of(
                "abstractField", new ModifiedValue(null, "aaa"),
                "dummyField", new ModifiedValue(null, "bbb")
        ));
        when(fieldManager.getOrderableField("abstractField")).thenReturn(abstractField);
        when(fieldManager.isOrderableField("abstractField")).thenReturn(true);
        when(fieldManager.getOrderableField("dummyField")).thenReturn(dummyField);
        when(fieldManager.isOrderableField("dummyField")).thenReturn(true);

        icf.execute(transientVars, null, null);

        verify(abstractField).createValue(issue, "aaa");
        verify(dummyField).createValue(issue, "bbb");
    }

}


