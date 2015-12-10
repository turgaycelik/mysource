package com.atlassian.jira.security.type;

import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.permission.GlobalPermissionType;
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
 * Tests the {@link CurrentReporterHasCreatePermission }class.
 *
 * @since v3.12
 */
public class TestCurrentReporterHasCreatePermission
{
    @Mock @AvailableInContainer
    private PluginAccessor pluginAccessor;
    @Mock @AvailableInContainer
    private PluginEventManager pluginEventManager;
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    private MockPermissionManager permissionManager;

    @Before
    public void setUp() throws Exception
    {
        permissionManager = new MockPermissionManager();
    }

    @Test
    public void usersReturnsTheReporterOfTheIssueInContextGivenTheUserCanCreateIssuesAndTheIssueInContextIsNotNull()
    {
        final User issueReporter = new MockUser("reporter");

        final MockIssue issueInContext = new MockIssue();
        issueInContext.setReporter(issueReporter);
        issueInContext.setProjectObject(new MockProject());
        issueInContext.setStatusObject(new MockStatus("1", "Open"));

        final PermissionContext permissionContext =
                new PermissionContextImpl(issueInContext, issueInContext.getProjectObject(), issueInContext.getStatusObject());

        final PermissionManager permissionManager = mock(PermissionManager.class);
        when(permissionManager.hasPermission(Permissions.CREATE_ISSUE, issueReporter)).thenReturn(true);

        final CurrentReporterHasCreatePermission currentReporterHasCreatePermission =
                new CurrentReporterHasCreatePermission(mock(JiraAuthenticationContext.class))
                {
                    @Override
                    PermissionManager getPermissionManager()
                    {
                        return permissionManager;
                    }
                };

        final Set<User> actualUsers = currentReporterHasCreatePermission.getUsers(permissionContext, "ignored");

        assertEquals(ImmutableSet.of(issueReporter), actualUsers);
    }

    @Test
    public void usersReturnsAnEmptySetGivenTheIssueReporterCanNotCreateIssuesAndTheIssueInContextIsNotNull()
    {
        final User issueReporter = new MockUser("reporter");

        final MockIssue issueInContext = new MockIssue();
        issueInContext.setReporter(issueReporter);
        issueInContext.setProjectObject(new MockProject());
        issueInContext.setStatusObject(new MockStatus("1", "Open"));

        final PermissionContext permissionContext =
                new PermissionContextImpl(issueInContext, issueInContext.getProjectObject(), issueInContext.getStatusObject());

        final PermissionManager permissionManager = mock(PermissionManager.class);
        when(permissionManager.hasPermission(Permissions.CREATE_ISSUE, issueReporter)).thenReturn(false);

        final CurrentReporterHasCreatePermission currentReporterHasCreatePermission =
                new CurrentReporterHasCreatePermission(mock(JiraAuthenticationContext.class))
                {
                    @Override
                    PermissionManager getPermissionManager()
                    {
                        return permissionManager;
                    }
                };

        final Set<User> actualUsers = currentReporterHasCreatePermission.getUsers(permissionContext, "ignored");

        assertEquals(ImmutableSet.<User>of(), actualUsers);
    }

    @Test
    public void usersReturnsAnEmptySetGivenANullIssueInThePermissionContext()
    {
        final PermissionManager permissionManager = mock(PermissionManager.class);

        final PermissionContext permissionContext = new PermissionContextImpl(null, null, null);

        final CurrentReporterHasCreatePermission currentReporterHasCreatePermission =
                new CurrentReporterHasCreatePermission(mock(JiraAuthenticationContext.class))
                {
                    @Override
                    PermissionManager getPermissionManager()
                    {
                        return permissionManager;
                    }
                };

        final Set<User> actualUsers = currentReporterHasCreatePermission.getUsers(permissionContext, "ignored");

        assertEquals(ImmutableSet.<User>of(), actualUsers);
    }

    @Test
    public void usersReturnsAnEmptySetGivenANullIssueReporterInThePermissionContext()
    {
        final MockIssue issueInContext = new MockIssue();
        issueInContext.setReporter(null);
        issueInContext.setProjectObject(new MockProject());
        issueInContext.setStatusObject(new MockStatus("1", "Open"));

        final PermissionManager permissionManager = mock(PermissionManager.class);

        final PermissionContext permissionContext =
                new PermissionContextImpl(issueInContext, issueInContext.getProjectObject(), issueInContext.getStatusObject());

        final CurrentReporterHasCreatePermission currentReporterHasCreatePermission =
                new CurrentReporterHasCreatePermission(mock(JiraAuthenticationContext.class))
                {
                    @Override
                    PermissionManager getPermissionManager()
                    {
                        return permissionManager;
                    }
                };

        final Set<User> actualUsers = currentReporterHasCreatePermission.getUsers(permissionContext, "ignored");

        assertEquals(ImmutableSet.<User>of(), actualUsers);
    }

    @Test
    public void testHasProjectPermission()
    {
        CurrentReporterHasCreatePermission currentReporterHasCreatePermission = createCurrentReporterHasCreatePermission();

        permissionManager.setDefaultPermission(true);
        assertTrue(currentReporterHasCreatePermission.hasProjectPermission(null, false, (GenericValue) null));
        permissionManager.setDefaultPermission(false);
        assertFalse(currentReporterHasCreatePermission.hasProjectPermission(null, false, (GenericValue) null));
    }

    @Test
    public void testIsValidForPermission()
    {
        CurrentReporterHasCreatePermission currentReporterHasCreatePermission = createCurrentReporterHasCreatePermission();
        assertTrue(currentReporterHasCreatePermission.isValidForPermission(ProjectPermissions.ASSIGN_ISSUES));
        assertTrue(currentReporterHasCreatePermission.isValidForPermission(ProjectPermissions.BROWSE_PROJECTS));

        // Create issue is the only Permission we can't be used in because otherwise we have a circular dependency
        assertFalse(currentReporterHasCreatePermission.isValidForPermission(ProjectPermissions.CREATE_ISSUES));
    }

    private CurrentReporterHasCreatePermission createCurrentReporterHasCreatePermission()
    {
        return new CurrentReporterHasCreatePermission(null)
        {

            PermissionManager getPermissionManager()
            {
                return permissionManager;
            }
        };
    }

    @After
    public void tearDown() throws Exception
    {
        // Just to make sure we don't use too much memory.
        permissionManager = null;
    }

}
