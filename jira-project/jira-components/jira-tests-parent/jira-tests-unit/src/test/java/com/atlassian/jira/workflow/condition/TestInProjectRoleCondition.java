package com.atlassian.jira.workflow.condition;

import java.util.Map;

import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.WorkflowFunctionUtils;

import com.google.common.collect.Maps;
import com.opensymphony.workflow.WorkflowContext;
import com.opensymphony.workflow.WorkflowException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link InProjectRoleCondition}
 */
public class TestInProjectRoleCondition
{
    private InProjectRoleCondition condition;
    private Map<String, Object> transientVars;
    private Map<String, Object> args;

    @Mock
    private WorkflowContext workflowContext;

    @Mock
    @AvailableInContainer
    private UserManager userManager;

    @Mock
    @AvailableInContainer
    private ProjectRoleManager projectRoleManager;

    private MockIssue issue;

    @Rule
    public MockitoContainer init = MockitoMocksInContainer.rule(this);
    private ApplicationUser caller;


    @Before
    public void setUp() throws Exception
    {
        caller = new MockApplicationUser("fred");
        when(userManager.getUserByKey("fred")).thenReturn(caller);

        issue = new MockIssue(123, "ISS-34");

        transientVars = Maps.newHashMap();
        transientVars.put("context", workflowContext);
        transientVars.put(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, issue);

        args = Maps.newHashMap();
        args.put(InProjectRoleCondition.KEY_PROJECT_ROLE_ID, "123");

        when(workflowContext.getCaller()).thenReturn(caller.getKey());

        condition = new InProjectRoleCondition();
    }

    @Test
    public void shouldReturnFalseWhenNoProjectRoleGiven() throws WorkflowException {
        args.put(InProjectRoleCondition.KEY_PROJECT_ROLE_ID, "");
        assertFalse(condition.passesCondition(transientVars, args, null));
    }

    @Test
    public void shouldReturnDefaultWhenNoProjectRoleForGivenIdWasConfigured() throws WorkflowException {
        when(projectRoleManager.getProjectRole(123L)).thenReturn(null);
        assertEquals(false, condition.passesCondition(transientVars, args, null));
    }

    @Test
    public void shouldDelegateToProjectRoleWhenIsSet() throws WorkflowException {
        Project project = new MockProject();
        issue.setProjectObject(project);
        ProjectRole role = Mockito.mock(ProjectRole.class);
        when(projectRoleManager.getProjectRole(123L)).thenReturn(role);

        when(projectRoleManager.isUserInProjectRole(caller, role, project)).thenReturn(true);
        assertEquals(true, condition.passesCondition(transientVars, args, null));

        when(projectRoleManager.isUserInProjectRole(caller, role, project)).thenReturn(false);
        assertEquals(false, condition.passesCondition(transientVars, args, null));

        verify(projectRoleManager, times(2)).isUserInProjectRole(caller, role, project);
    }


}
