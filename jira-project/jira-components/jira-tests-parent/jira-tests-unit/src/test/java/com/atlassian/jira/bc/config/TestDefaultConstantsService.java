package com.atlassian.jira.bc.config;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.config.DefaultConstantsService.IsStatusVisible.WorkflowFetcher;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;

import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.atlassian.jira.bc.config.DefaultConstantsService.IsStatusVisible;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SuppressWarnings({"unchecked", "deprecation"})
public class TestDefaultConstantsService
{
    @Mock private User user;
    @Mock private PermissionManager permissionManager;
    @Mock private WorkflowManager workflowManager;
    @Mock private WorkflowFetcher workflowFetcher;
    @Mock private Status status;
    @Mock private Status status2;
    @Mock private Project project;
    @Mock private Project project2;
    @Mock private JiraWorkflow jiraWorkflow;
    @Mock private JiraWorkflow jiraWorkflow2;
    @Mock private WorkflowDescriptor workflowDescriptor;
    @Mock private WorkflowDescriptor workflowDescriptor2;

    @Before
    public void setup()
    {
        initMocks(this);
    }


    @Test
    public void testStatusVisibility()
    {
        List<Project> projects = new ArrayList<Project>(1);
        projects.add(project);
        when(permissionManager.getProjectObjects(Permissions.BROWSE, user)).thenReturn(projects);

        String workflowName = "workflowName";
        List<String> workflowNames = initWorkflows(workflowName);
        when(workflowFetcher.getWorkflowNames(project)).thenReturn(workflowNames);
        when(workflowManager.getWorkflow(workflowName)).thenReturn(jiraWorkflow);
        when(jiraWorkflow.getDescriptor()).thenReturn(workflowDescriptor);

        List<StepDescriptor> steps = new ArrayList<StepDescriptor>();
        StepDescriptor stepDescriptor = DescriptorFactory.getFactory().createStepDescriptor();
        steps.add(stepDescriptor);
        when(workflowDescriptor.getSteps()).thenReturn(steps);

        when(status.getId()).thenReturn("1");
        stepDescriptor.getMetaAttributes().put("jira.status.id", "1");

        IsStatusVisible isStatusVisible = new IsStatusVisible(user, permissionManager, workflowManager, workflowFetcher);
        boolean apply = isStatusVisible.apply(status);
        assertEquals(apply, true);
    }

    @Test
    public void testStatusInVisibility()
    {
        List<Project> projects = new ArrayList<Project>(1);
        projects.add(project);
        when(permissionManager.getProjectObjects(Permissions.BROWSE, user)).thenReturn(projects);

        String workflowName = "workflowName";
        List<String> workflowNames = initWorkflows(workflowName);
        when(workflowFetcher.getWorkflowNames(project)).thenReturn(workflowNames);
        when(workflowManager.getWorkflow(workflowName)).thenReturn(jiraWorkflow);
        when(jiraWorkflow.getDescriptor()).thenReturn(workflowDescriptor);

        List<StepDescriptor> steps = new ArrayList<StepDescriptor>();
        StepDescriptor stepDescriptor = DescriptorFactory.getFactory().createStepDescriptor();
        steps.add(stepDescriptor);
        when(workflowDescriptor.getSteps()).thenReturn(steps);

        when(status.getId()).thenReturn("1");
        stepDescriptor.getMetaAttributes().put("jira.status.id", "2");

        IsStatusVisible isStatusVisible = new IsStatusVisible(user, permissionManager, workflowManager, workflowFetcher);
        boolean apply = isStatusVisible.apply(status);
        assertEquals(apply, false);
    }

    @Test
    public void testNoProjectsWithThatStatus()
    {
        List<Project> projects = new ArrayList<Project>(0);
        when(permissionManager.getProjectObjects(Permissions.BROWSE, user)).thenReturn(projects);

        String workflowName = "workflowName";
        List<String> workflowNames = initWorkflows(workflowName);
        when(workflowFetcher.getWorkflowNames(project)).thenReturn(workflowNames);
        when(workflowManager.getWorkflow(workflowName)).thenReturn(jiraWorkflow);
        when(jiraWorkflow.getDescriptor()).thenReturn(workflowDescriptor);

        List<StepDescriptor> steps = new ArrayList<StepDescriptor>();
        StepDescriptor stepDescriptor = DescriptorFactory.getFactory().createStepDescriptor();
        steps.add(stepDescriptor);
        when(workflowDescriptor.getSteps()).thenReturn(steps);

        when(status.getId()).thenReturn("1");
        stepDescriptor.getMetaAttributes().put("jira.status.id", "2");

        IsStatusVisible isStatusVisible = new IsStatusVisible(user, permissionManager, workflowManager, workflowFetcher);
        boolean apply = isStatusVisible.apply(status);
        assertEquals(apply, false);
    }

    @Test
    public void testTwoProjectsWithThatStatus()
    {
        List<Project> projects = new ArrayList<Project>(1);
        projects.add(project);
        projects.add(project2);
        when(permissionManager.getProjectObjects(Permissions.BROWSE, user)).thenReturn(projects);

        String workflowName = "workflowName";
        List<String> workflowNames = initWorkflows(workflowName);
        when(workflowFetcher.getWorkflowNames(project)).thenReturn(workflowNames);
        when(workflowFetcher.getWorkflowNames(project2)).thenReturn(workflowNames);
        when(workflowManager.getWorkflow(workflowName)).thenReturn(jiraWorkflow);
        when(jiraWorkflow.getDescriptor()).thenReturn(workflowDescriptor);

        List<StepDescriptor> steps = new ArrayList<StepDescriptor>();
        StepDescriptor stepDescriptor = DescriptorFactory.getFactory().createStepDescriptor();
        steps.add(stepDescriptor);
        when(workflowDescriptor.getSteps()).thenReturn(steps);

        when(status.getId()).thenReturn("1");
        stepDescriptor.getMetaAttributes().put("jira.status.id", "1");

        IsStatusVisible isStatusVisible = new IsStatusVisible(user, permissionManager, workflowManager, workflowFetcher);
        boolean apply = isStatusVisible.apply(status);
        assertEquals(apply, true);

    }

    @Test
    public void testOtherProjectNonInterference()
    {
        List<Project> projects = new ArrayList<Project>(1);
        projects.add(project);
        projects.add(project2);
        when(permissionManager.getProjectObjects(Permissions.BROWSE, user)).thenReturn(projects);

        String workflowName = "workflowName";
        List<String> workflowNames = initWorkflows(workflowName);
        when(workflowFetcher.getWorkflowNames(project)).thenReturn(workflowNames);
        when(workflowManager.getWorkflow(workflowName)).thenReturn(jiraWorkflow);
        when(jiraWorkflow.getDescriptor()).thenReturn(workflowDescriptor);

        String workflowName2 = "workflowName2";
        List<String> workflowNames2 = initWorkflows(workflowName2);
        when(workflowFetcher.getWorkflowNames(project2)).thenReturn(workflowNames2);
        when(workflowManager.getWorkflow(workflowName2)).thenReturn(jiraWorkflow2);
        when(jiraWorkflow2.getDescriptor()).thenReturn(workflowDescriptor2);

        StepDescriptor stepDescriptor = DescriptorFactory.getFactory().createStepDescriptor();
        List<StepDescriptor> steps = packSteps(stepDescriptor);
        when(workflowDescriptor.getSteps()).thenReturn(steps);

        StepDescriptor stepDescriptor2 = DescriptorFactory.getFactory().createStepDescriptor();
        List<StepDescriptor> steps2 = packSteps(stepDescriptor2);
        when(workflowDescriptor2.getSteps()).thenReturn(steps2);

        when(status.getId()).thenReturn("1");
        stepDescriptor.getMetaAttributes().put("jira.status.id", "1");

        when(status2.getId()).thenReturn("2");
        stepDescriptor2.getMetaAttributes().put("jira.status.id", "2");

        IsStatusVisible isStatusVisible = new IsStatusVisible(user, permissionManager, workflowManager, workflowFetcher);
        boolean apply = isStatusVisible.apply(status);
        assertEquals(apply, true);

        verifyNoMoreInteractions(status2);
    }

    private List<StepDescriptor> packSteps(StepDescriptor stepDescriptor)
    {
        List<StepDescriptor> steps = new ArrayList<StepDescriptor>();
        steps.add(stepDescriptor);
        return steps;
    }

    private List<String> initWorkflows(String workflowName)
    {
        List<String> workflowNames = new ArrayList<String>(1);
        workflowNames.add(workflowName);
        return workflowNames;
    }
}
