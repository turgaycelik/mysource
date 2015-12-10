/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.condition;

import com.atlassian.fugue.Option;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
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

import java.util.Map;

import static com.atlassian.fugue.Option.option;
import static com.atlassian.jira.permission.ProjectPermissions.DELETE_OWN_COMMENTS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class TestPermissionCondition
{
    private PermissionCondition condition;
    private Map<String, Object> transientVars;
    private Map<String, Object> args;

    @Mock
    private WorkflowContext workflowContext;

    @Mock
    @AvailableInContainer
    private UserManager userManager;

    @Mock
    @AvailableInContainer
    private PermissionManager permissionManager;

    @Mock
    private ProjectPermission permission;

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

        when(workflowContext.getCaller()).thenReturn(caller.getKey());

        condition = new PermissionCondition();
    }

    @Test
    public void returnsTrueIfUserHasPermissionWithLegacyConfiguration()
    {
        args.put("permission", Permissions.getShortName(Permissions.COMMENT_DELETE_OWN));

        when(permissionManager.getProjectPermission(DELETE_OWN_COMMENTS)).thenReturn(option(permission));
        when(permissionManager.hasPermission(DELETE_OWN_COMMENTS, issue, caller)).thenReturn(true);
        assertThat(condition.passesCondition(transientVars, args, null), is(true));
    }

    @Test
    public void returnsTrueIfUserHasPermission()
    {
        args.put("permissionKey", DELETE_OWN_COMMENTS.permissionKey());

        when(permissionManager.getProjectPermission(DELETE_OWN_COMMENTS)).thenReturn(option(permission));
        when(permissionManager.hasPermission(DELETE_OWN_COMMENTS, issue, caller)).thenReturn(true);
        assertThat(condition.passesCondition(transientVars, args, null), is(true));
    }

    @Test
    public void returnsTrueIfPermissionDoesNotExistWithLegacyConfiguration()
    {
        args.put("permission", "notexisting");

        when(permissionManager.getProjectPermission(new ProjectPermissionKey("notexisting"))).thenReturn(Option.<ProjectPermission>none());
        assertThat(condition.passesCondition(transientVars, args, null), is(true));
    }

    @Test
    public void returnsTrueIfPermissionDoesNotExist()
    {
        args.put("permissionKey", "notexisting");

        when(permissionManager.getProjectPermission(new ProjectPermissionKey("notexisting"))).thenReturn(Option.<ProjectPermission>none());
        assertThat(condition.passesCondition(transientVars, args, null), is(true));
    }

    @Test
    public void returnsFalseIfUserDoesNotHavePermissionWithLegacyConfiguration()
    {
        args.put("permission", Permissions.getShortName(Permissions.COMMENT_DELETE_OWN));

        when(permissionManager.getProjectPermission(DELETE_OWN_COMMENTS)).thenReturn(option(permission));
        when(permissionManager.hasPermission(DELETE_OWN_COMMENTS, issue, caller)).thenReturn(false);
        assertThat(condition.passesCondition(transientVars, args, null), is(false));
    }

    @Test
    public void returnsFalseIfUserDoesNotHavePermission()
    {
        args.put("permissionKey", DELETE_OWN_COMMENTS.permissionKey());

        when(permissionManager.getProjectPermission(DELETE_OWN_COMMENTS)).thenReturn(option(permission));
        when(permissionManager.hasPermission(DELETE_OWN_COMMENTS, issue, caller)).thenReturn(false);
        assertThat(condition.passesCondition(transientVars, args, null), is(false));
    }
}
