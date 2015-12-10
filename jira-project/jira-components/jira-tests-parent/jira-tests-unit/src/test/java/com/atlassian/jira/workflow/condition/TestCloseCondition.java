/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.condition;

import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.WorkflowFunctionUtils;

import com.google.common.collect.Maps;
import com.opensymphony.workflow.WorkflowContext;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestCloseCondition
{
    private CloseCondition condition;
    private Map<String, Object> transientVars;

    @Mock
    private WorkflowContext workflowContext;

    @Mock
    @AvailableInContainer
    private UserManager userManager;

    @Mock
    @AvailableInContainer
    private PermissionManager permissionManager;

    private MockIssue issue;

    @Rule
    public MockitoContainer init = MockitoMocksInContainer.rule(this);
    private ApplicationUser caller;


    @Before
    public void setUp() throws Exception
    {
        caller = new MockApplicationUser("fred");
        when(userManager.getUserObject("fred")).thenReturn(caller.getDirectoryUser());

        issue = new MockIssue(123, "ISS-34");

        transientVars = Maps.newHashMap();
        transientVars.put("context", workflowContext);
        transientVars.put(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, issue);
        transientVars.put("close", true);

        when(workflowContext.getCaller()).thenReturn(caller.getKey());

        condition = new CloseCondition();
    }

    @Test
    public void shouldReturnFalseWhenThereIsClosePropertyDefinedAsFalseDisregardingPermissionStatus(){
        when(permissionManager.hasPermission(Permissions.CLOSE_ISSUE, issue, caller.getDirectoryUser())).thenReturn(true);

        transientVars.put("close", false);
        assertFalse(condition.passesCondition(transientVars, null, null));

        transientVars.put("close", true);
        assertTrue(condition.passesCondition(transientVars, null, null));
    }

    @Test
    public void shouldCheckUsersPermissionForClosingIssue(){
        condition.passesCondition(transientVars, null, null);
        verify(permissionManager).hasPermission(Permissions.CLOSE_ISSUE, issue, caller.getDirectoryUser());
    }

    @Test
    public void shouldCheckForNullUserWhenThereIsNoCallerGiven(){
        when(workflowContext.getCaller()).thenReturn(null);
        condition.passesCondition(transientVars, null, null);
        verify(permissionManager).hasPermission(Permissions.CLOSE_ISSUE, issue, (User)null);
    }

}
