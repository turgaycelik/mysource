package com.atlassian.jira.security.type;

import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.permission.PermissionContextImpl;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;

import com.google.common.collect.ImmutableSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestCase for {@link CurrentAssigneeHasAssignablePermission}.
 *
 * @since v3.12
 */
public class TestCurrentAssigneeHasAssignablePermission
{
    @Mock @AvailableInContainer
    private PluginAccessor pluginAccessor;
    @Mock @AvailableInContainer
    private PluginEventManager pluginEventManager;
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    private MockPermissionManager mockPermissionManager;

    @Before
    public void setUp() throws Exception
    {
        mockPermissionManager = new MockPermissionManager();
    }

    @Test
    public void usersReturnsTheAssigneeOfTheIssueInContextGivenTheUserCanBeAssignedToIssuesAndTheIssueInContextIsNotNull()
    {
        final User issueAssignee = new MockUser("assignee");

        final MockIssue issueInContext = new MockIssue();
        issueInContext.setAssignee(issueAssignee);
        issueInContext.setProjectObject(new MockProject());
        issueInContext.setStatusObject(new MockStatus("1", "Open"));

        final PermissionContext permissionContext =
                new PermissionContextImpl(issueInContext, issueInContext.getProjectObject(), issueInContext.getStatusObject());

        final PermissionManager permissionManager = mock(PermissionManager.class);
        when(permissionManager.hasPermission(Permissions.ASSIGNABLE_USER, issueAssignee)).thenReturn(true);

        final CurrentAssigneeHasAssignablePermission currentAssigneeHasAssignablePermission =
                new CurrentAssigneeHasAssignablePermission(mock(JiraAuthenticationContext.class))
                {
                    @Override
                    PermissionManager getPermissionManager()
                    {
                        return permissionManager;
                    }
                };

        final Set<User> actualUsers = currentAssigneeHasAssignablePermission.getUsers(permissionContext, "ignored");

        assertEquals(ImmutableSet.of(issueAssignee), actualUsers);
    }

    @Test
    public void usersReturnsAnEmptySetGivenTheIssueAssigneeCanNotBeAssignedToIssuesAndTheIssueInContextIsNotNull()
    {
        final User issueAssignee = new MockUser("assignee");

        final MockIssue issueInContext = new MockIssue();
        issueInContext.setAssignee(issueAssignee);
        issueInContext.setProjectObject(new MockProject());
        issueInContext.setStatusObject(new MockStatus("1", "Open"));

        final PermissionContext permissionContext =
                new PermissionContextImpl(issueInContext, issueInContext.getProjectObject(), issueInContext.getStatusObject());

        final PermissionManager permissionManager = mock(PermissionManager.class);
        when(permissionManager.hasPermission(Permissions.ASSIGNABLE_USER, issueAssignee)).thenReturn(false);

        final CurrentAssigneeHasAssignablePermission currentAssigneeHasAssignablePermission =
                new CurrentAssigneeHasAssignablePermission(mock(JiraAuthenticationContext.class))
                {
                    @Override
                    PermissionManager getPermissionManager()
                    {
                        return permissionManager;
                    }
                };

        final Set<User> actualUsers = currentAssigneeHasAssignablePermission.getUsers(permissionContext, "ignored");

        assertEquals(ImmutableSet.<User>of(), actualUsers);
    }

    @Test
    public void usersReturnsAnEmptySetGivenANullIssueInThePermissionContext()
    {
        final PermissionManager permissionManager = mock(PermissionManager.class);

        final PermissionContext permissionContext = new PermissionContextImpl(null, null, null);

        final CurrentAssigneeHasAssignablePermission currentAssigneeHasAssignablePermission =
                new CurrentAssigneeHasAssignablePermission(mock(JiraAuthenticationContext.class))
                {
                    @Override
                    PermissionManager getPermissionManager()
                    {
                        return permissionManager;
                    }
                };

        final Set<User> actualUsers = currentAssigneeHasAssignablePermission.getUsers(permissionContext, "ignored");

        assertEquals(ImmutableSet.<User>of(), actualUsers);
    }

    @Test
    public void usersReturnsAnEmptySetGivenANullIssueAssigneeInThePermissionContext()
    {
        final MockIssue issueInContext = new MockIssue();
        issueInContext.setAssignee(null);
        issueInContext.setProjectObject(new MockProject());
        issueInContext.setStatusObject(new MockStatus("1", "Open"));

        final PermissionManager permissionManager = mock(PermissionManager.class);

        final PermissionContext permissionContext =
                new PermissionContextImpl(issueInContext, issueInContext.getProjectObject(), issueInContext.getStatusObject());

        final CurrentAssigneeHasAssignablePermission currentAssigneeHasAssignablePermission =
                new CurrentAssigneeHasAssignablePermission(mock(JiraAuthenticationContext.class))
                {
                    @Override
                    PermissionManager getPermissionManager()
                    {
                        return permissionManager;
                    }
                };

        final Set<User> actualUsers = currentAssigneeHasAssignablePermission.getUsers(permissionContext, "ignored");

        assertEquals(ImmutableSet.<User>of(), actualUsers);
    }

    @Test
    public void testHasProjectPermission()
    {
        CurrentAssigneeHasAssignablePermission currentAssigneeHasAssignablePermission = createCurrentAssigneeHasAssignablePermission();

        mockPermissionManager.setDefaultPermission(true);
        assertTrue(currentAssigneeHasAssignablePermission.hasProjectPermission(null, false, (GenericValue) null));
        mockPermissionManager.setDefaultPermission(false);
        assertFalse(currentAssigneeHasAssignablePermission.hasProjectPermission(null, false, (GenericValue) null));
    }

    @Test
    public void testIsValidForPermission()
    {
        CurrentAssigneeHasAssignablePermission currentAssigneeHasAssignablePermission = createCurrentAssigneeHasAssignablePermission();
        assertTrue(currentAssigneeHasAssignablePermission.isValidForPermission(ProjectPermissions.ASSIGN_ISSUES));
        assertTrue(currentAssigneeHasAssignablePermission.isValidForPermission(ProjectPermissions.BROWSE_PROJECTS));
        assertTrue(currentAssigneeHasAssignablePermission.isValidForPermission(ProjectPermissions.CREATE_ISSUES));

        // "Assignable" is the only Permission we can't be used in because otherwise we have a circular dependency
        assertFalse(currentAssigneeHasAssignablePermission.isValidForPermission(ProjectPermissions.ASSIGNABLE_USER));
    }

    private CurrentAssigneeHasAssignablePermission createCurrentAssigneeHasAssignablePermission()
    {
        return new CurrentAssigneeHasAssignablePermission(null)
        {

            PermissionManager getPermissionManager()
            {
                return mockPermissionManager;
            }
        };
    }

    @After
    public void tearDown() throws Exception
    {
        // Just to make sure we don't use too much memory.
        mockPermissionManager = null;
    }

}
