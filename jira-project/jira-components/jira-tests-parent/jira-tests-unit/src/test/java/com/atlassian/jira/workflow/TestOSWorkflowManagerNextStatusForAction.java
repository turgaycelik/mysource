package com.atlassian.jira.workflow;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.names.WorkflowCopyNameFactory;

import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.config.Configuration;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ResultDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link OSWorkflowManager#getNextStatusIdForAction(com.atlassian.jira.issue.Issue, int)}
 */
@RunWith (MockitoJUnitRunner.class)
public class TestOSWorkflowManagerNextStatusForAction
{
    @Mock
    private ProjectManager projectManager;
    @Mock
    private WorkflowSchemeManager workflowSchemeManager;
    @Mock
    private Configuration configuration;

    private OSWorkflowManager manager;

    @Before
    public void setUp()
    {
        new MockComponentWorker()
                .addMock(ProjectManager.class, projectManager)
                .addMock(WorkflowSchemeManager.class, workflowSchemeManager)
                .init();
        manager = new OSWorkflowManager(
                configuration,
                mock(DraftWorkflowStore.class),
                mock(EventPublisher.class),
                mock(WorkflowsRepository.class),
                mock(WorkflowCopyNameFactory.class),
                mock(JiraAuthenticationContext.class)
        );
    }

    @Test
    public void getNextStatusIdForAction() throws Exception
    {
        int actionId = 1;
        long projectId = 1L;
        int issueTypeId = 1;
        int stepId = 1;
        String statusId = "statusId";

        StepDescriptor step = mockStepDescriptorWith(statusId);
        ActionDescriptor actionDescriptor = mockActionDescriptorWith(stepId);
        WorkflowDescriptor workflowDescriptor = mockWorkflowDescriptorWith(actionDescriptor, actionId, step, stepId);

        setWorkflowDescriptorFor(projectId, issueTypeId, workflowDescriptor);

        Issue issue = issueWith(projectId, issueTypeId);
        String status = manager.getNextStatusIdForAction(issue, actionId);

        assertThat(status, is("statusId"));
    }
    
    @Test (expected = IllegalStateException.class)
    public void getNextStatusIdForActionWhenTheWorkflowIsNullThrowsIllegalStateException() throws Exception
    {
        int actionId = 1;
        long projectId = 1L;
        int issueTypeId = 1;

        when(configuration.getWorkflow(anyString())).thenThrow(new FactoryException());

        Status expectedStatus = new MockStatus("", "");
        Issue issue = issueWith(projectId, issueTypeId, expectedStatus);

        manager.getNextStatusIdForAction(issue, actionId);
    }
    
    @Test (expected = IllegalStateException.class)
    public void getNextStatusIdForActionWhenThereIsNoActionWithTheGivenIdThrowsIllegalStateException() throws Exception
    {
        int actionId = 1;
        long projectId = 1L;
        int issueTypeId = 1;

        ActionDescriptor actionDescriptor = null;
        WorkflowDescriptor workflowDescriptor = mockWorkflowDescriptorWith(actionDescriptor, actionId);

        setWorkflowDescriptorFor(projectId, issueTypeId, workflowDescriptor);

        Status expectedStatus = new MockStatus("", "");
        Issue issue = issueWith(projectId, issueTypeId, expectedStatus);

        manager.getNextStatusIdForAction(issue, actionId);
    }
    
    @Test
    public void getNextStatusIdForActionWhenStepIdIndicatesThatStatusWillNotChangeReturnsIssueStatus() throws Exception
    {
        int actionId = 1;
        long projectId = 1L;
        int issueTypeId = 1;
        int stepId = JiraWorkflow.ACTION_ORIGIN_STEP_ID;
        String statusId = "statusId";

        StepDescriptor step = mockStepDescriptorWith(statusId);
        ActionDescriptor actionDescriptor = mockActionDescriptorWith(stepId);
        WorkflowDescriptor workflowDescriptor = mockWorkflowDescriptorWith(actionDescriptor, actionId, step, stepId);

        setWorkflowDescriptorFor(projectId, issueTypeId, workflowDescriptor);

        Issue issue = issueWith(projectId, issueTypeId, new MockStatus("statusId", "open"));
        String status = manager.getNextStatusIdForAction(issue, actionId);

        assertThat(status, is("statusId"));
    }
    
    private ActionDescriptor mockActionDescriptorWith(int stepId)
    {
        ActionDescriptor actionDescriptor = mock(ActionDescriptor.class);
        ResultDescriptor resultDescriptor = mockResultDescriptorWith(stepId);
        when(actionDescriptor.getUnconditionalResult()).thenReturn(resultDescriptor);
        return actionDescriptor;
    }

    private ResultDescriptor mockResultDescriptorWith(int stepId)
    {
        ResultDescriptor resultDescriptor = mock(ResultDescriptor.class);
        when(resultDescriptor.getStep()).thenReturn(stepId);
        return resultDescriptor;
    }

    private StepDescriptor mockStepDescriptorWith(String status)
    {
        StepDescriptor step = mock(StepDescriptor.class);
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put(JiraWorkflow.STEP_STATUS_KEY, status);
        when(step.getMetaAttributes()).thenReturn(meta);
        return step;
    }

    private Project mockProjectObjectFor(long projectId)
    {
        Project projectObj = mock(Project.class);
        when(projectManager.getProjectObj(projectId)).thenReturn(projectObj);
        return projectObj;
    }

    private WorkflowDescriptor mockWorkflowDescriptorWith(ActionDescriptor actionDescriptor, int actionId, StepDescriptor step, int stepId)
    {
        WorkflowDescriptor workflowDescriptor = mockWorkflowDescriptorWith(actionDescriptor, actionId);
        when(workflowDescriptor.getStep(stepId)).thenReturn(step);
        return workflowDescriptor;
    }

    private WorkflowDescriptor mockWorkflowDescriptorWith(final ActionDescriptor actionDescriptor, final int actionId)
    {
        WorkflowDescriptor workflowDescriptor = mock(WorkflowDescriptor.class);
        when(workflowDescriptor.getAction(actionId)).thenReturn(actionDescriptor);
        return workflowDescriptor;
    }

    private WorkflowDescriptor setWorkflowDescriptorFor(long projectId, int issueTypeId, final WorkflowDescriptor workflowDescriptor) throws Exception
    {
        Project project = mockProjectObjectFor(projectId);
        String workflowName = "workflow";
        when(workflowSchemeManager.getWorkflowName(project, "" + issueTypeId)).thenReturn(workflowName);
        when(configuration.getWorkflow(workflowName)).thenReturn(workflowDescriptor);
        return workflowDescriptor;
    }

    private Issue issueWith(long projectId, int issueTypeId, final Status status)
    {
        Issue issue = issueWith(projectId, issueTypeId);
        when(issue.getStatusObject()).thenReturn(status);
        return issue;
    }

    private Issue issueWith(long projectId, int issueTypeId)
    {
        Issue issue = mock(Issue.class);
        MockGenericValue project = new MockGenericValue("Project");
        project.set("id", projectId);
        when(issue.getProject()).thenReturn(project);
        IssueType issueType = new MockIssueType(issueTypeId, "Bug");
        when(issue.getIssueTypeObject()).thenReturn(issueType);
        return issue;
    }
}
