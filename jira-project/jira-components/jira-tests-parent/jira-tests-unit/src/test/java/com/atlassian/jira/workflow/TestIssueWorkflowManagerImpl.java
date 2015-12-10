package com.atlassian.jira.workflow;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginAccessor;

import com.google.common.collect.ImmutableList;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v6.2.2
 */
public class TestIssueWorkflowManagerImpl
{
    public static final long WORKFLOW_ID = 1L;
    public static final String TST_PROJECT_KEY = "TST";
    public static final String ISSUE_TEST_ONE_KEY = "TST-1";
    public static final long ISSUE_TEST_ONE_ID = 1L;
    public static final int NOT_EXISTENT_ACTION_ID = 9999;
    public static final int START_PROGRESS_ACTION_ID = 1;

    @Rule
    public final InitMockitoMocks initMockitoMocks = new InitMockitoMocks(this);

    @Mock
    private IssueManager issueManager;

    @Mock
    private WorkflowManager workflowManager;

    @Mock
    private JiraAuthenticationContext authenticationContext;

    @Mock
    private PluginAccessor pluginAccessor;

    @Mock
    private ApplicationUser currentUser;

    @Mock
    private Issue issueTestOne;

    @Mock
    private MutableIssue mutableIssueTestOne;

    @Mock
    private PermissionManager permissionManager;

    private final ActionDescriptor startProgressAction = mock(ActionDescriptor.class, "Start progress action descriptor");

    @Before
    public void setUp() throws Exception
    {
        // create TST project
        final Project project = mock(Project.class);
        when(project.getKey()).thenReturn(TST_PROJECT_KEY);

        // create TST-1 issue
        when(issueTestOne.getProjectObject()).thenReturn(project);
        when(issueTestOne.getKey()).thenReturn(ISSUE_TEST_ONE_KEY);
        when(issueTestOne.getId()).thenReturn(ISSUE_TEST_ONE_ID);
        when(issueTestOne.getWorkflowId()).thenReturn(WORKFLOW_ID);

        // init issue manager, auth context, user permission
        when(issueManager.getIssueObject(ISSUE_TEST_ONE_ID)).thenReturn(mutableIssueTestOne);
        when(authenticationContext.getUser()).thenReturn(currentUser);
        setHasTransitionPermission(true);

        // matcher for crazy map that needs to be passed into workflow#getAvailableActions
        //noinspection unchecked
        final Matcher<Map<?, ?>> expectedInputMapMatcher = allOf(
                hasEntry("pkey", TST_PROJECT_KEY),
                hasEntry("issue", issueTestOne),
                hasEntry(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, mutableIssueTestOne)
        );

        // prepare workflow with one action and bind it to managers
        final Workflow workflow = mock(Workflow.class);
        when(workflow.getAvailableActions(eq(WORKFLOW_ID), argThat(expectedInputMapMatcher))).thenReturn(new int[] { START_PROGRESS_ACTION_ID });
        when(workflowManager.makeWorkflow(currentUser)).thenReturn(workflow);

        final WorkflowDescriptor jiraWorkflowDescriptor = mock(WorkflowDescriptor.class);
        when(jiraWorkflowDescriptor.getAction(START_PROGRESS_ACTION_ID)).thenReturn(startProgressAction);

        final JiraWorkflow jiraWorkflow = mock(JiraWorkflow.class);
        when(jiraWorkflow.getDescriptor()).thenReturn(jiraWorkflowDescriptor);

        when(workflowManager.getWorkflow(issueTestOne)).thenReturn(jiraWorkflow);
    }

    private void setHasTransitionPermission(final boolean hasPermission)
    {
        when(permissionManager.hasPermission(eq(ProjectPermissions.TRANSITION_ISSUES), eq(issueTestOne), eq(currentUser))).thenReturn(hasPermission);
    }

    @Test
    public void testGetAvailableActions() throws Exception
    {
        final IssueWorkflowManagerImpl issueWorkflowManager = new IssueWorkflowManagerImpl(issueManager, workflowManager, null, permissionManager);
        final Collection<ActionDescriptor> availableActions = issueWorkflowManager.getAvailableActions(issueTestOne, currentUser);
        Assert.assertThat(availableActions, Matchers.containsInAnyOrder(startProgressAction));
    }

    @Test
    public void testGetAvailableActionIdsWhenUserHasTransitionPermission() throws Exception
    {
        final IssueWorkflowManagerImpl issueWorkflowManager = new IssueWorkflowManagerImpl(issueManager, workflowManager, null, permissionManager);
        final Iterable<Integer> availableActionIds = toIterable(issueWorkflowManager.getAvailableActionIds(issueTestOne, TransitionOptions.defaults(), currentUser));

        Assert.assertThat(availableActionIds, Matchers.hasItem(START_PROGRESS_ACTION_ID));
    }

    @Test
    public void testGetAvailableActionIdsWhenUserDoesntHaveTransitionPermission() throws Exception
    {
        setHasTransitionPermission(false);
        final IssueWorkflowManagerImpl issueWorkflowManager = new IssueWorkflowManagerImpl(issueManager, workflowManager, null, permissionManager);
        final Iterable<Integer> availableActionIds = toIterable(issueWorkflowManager.getAvailableActionIds(issueTestOne, TransitionOptions.defaults(), currentUser));

        Assert.assertThat(availableActionIds, Matchers.<Integer>emptyIterable());
    }

    @Test
    public void testIsValidActionWhenUserHasTransitionPermission() throws Exception
    {
        final IssueWorkflowManagerImpl issueWorkflowManager = new IssueWorkflowManagerImpl(issueManager, workflowManager, null, permissionManager);

        Assert.assertTrue("Action from workflow should be valid as transition permission was granted",
                issueWorkflowManager.isValidAction(issueTestOne, START_PROGRESS_ACTION_ID, currentUser));

        Assert.assertFalse("Action which is not in workflow should not be valid",
                issueWorkflowManager.isValidAction(issueTestOne, NOT_EXISTENT_ACTION_ID, currentUser));
    }

    @Test
    public void testIsValidActionWhenUserDoesntHaveTransitionPermission() throws Exception
    {
        setHasTransitionPermission(false);
        final IssueWorkflowManagerImpl issueWorkflowManager = new IssueWorkflowManagerImpl(issueManager, workflowManager, null, permissionManager);

        Assert.assertFalse("Any action from workflow should be invalid as transition permission was NOT granted",
                issueWorkflowManager.isValidAction(issueTestOne, START_PROGRESS_ACTION_ID, currentUser));

        Assert.assertFalse("Action which is not in workflow should not be valid",
                issueWorkflowManager.isValidAction(issueTestOne, NOT_EXISTENT_ACTION_ID, currentUser));
    }

    @Test
    public void testPermissionsCheckSkippedWhenAsked() throws Exception
    {
        setHasTransitionPermission(false);
        final IssueWorkflowManagerImpl issueWorkflowManager = new IssueWorkflowManagerImpl(issueManager, workflowManager, null, permissionManager);

        // Verify that the transition permission is in place
        Assert.assertFalse("Any action from workflow should be invalid as transition permission was NOT granted",
                issueWorkflowManager.isValidAction(issueTestOne, START_PROGRESS_ACTION_ID, currentUser));

        final TransitionOptions transitionOptions = new TransitionOptions.Builder().skipPermissions().build();

        Assert.assertTrue("Transition permission shouldn't be checked if we ask to skip it",
                issueWorkflowManager.isValidAction(issueTestOne, START_PROGRESS_ACTION_ID, transitionOptions, currentUser));
    }

    @Test
    public void testDeprecatedMethodsDelegatesCallToNewMethodsWithUserFromAuthenticationContext() throws Exception
    {
        final List<ActionDescriptor> expectedResult = ImmutableList.of();

        // deprecated methods should delegate query (after getting current user from authentication context) to overriden methods
        final IssueWorkflowManagerImpl issueWorkflowManager = new IssueWorkflowManagerImpl(issueManager, workflowManager, authenticationContext, permissionManager) {
            @Override
            public Collection<ActionDescriptor> getAvailableActions(final Issue issue, final TransitionOptions transitionOptions, final ApplicationUser user)
            {
                assertEquals("Expected to get currently logged in user", currentUser, user);
                return expectedResult;
            }

            @Override
            public List<ActionDescriptor> getSortedAvailableActions(final Issue issue, final TransitionOptions transitionOptions, final ApplicationUser user)
            {
                assertEquals("Expected to get currently logged in user", currentUser, user);
                return expectedResult;
            }
        };

        // check if deprecated methods returns exactly the same object that we do return in overriden methods above
        assertTrue("Method did not return expected value from delegated method",
                expectedResult == issueWorkflowManager.getSortedAvailableActions(issueTestOne));

        assertTrue("Method did not return expected value from delegated method",
                expectedResult == issueWorkflowManager.getAvailableActions(issueTestOne));

    }

    private Matcher<Map<?, ?>> hasEntry(final Object k, final Object v)
    {
        return Matchers.hasEntry(k, v);
    }

    private Iterable<Integer> toIterable(final int[] intArray)
    {
        final Integer[] integerArray = new Integer[intArray.length];
        for (int i = 0; i < intArray.length; i++)
        {
            integerArray[i] = intArray[i];
        }
        return ImmutableList.copyOf(integerArray);
    }
}
