package com.atlassian.jira.bc.issue.watcher;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.lang.Pair;
import com.google.common.collect.ImmutableList;
import org.easymock.internal.matchers.Any;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Iterables.concat;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDefaultWatcherService
{
    private ApplicationProperties applicationProperties;
    private PermissionManager permissionManager;

    @Before
    public void setUp() throws Exception
    {
        applicationProperties = mock(ApplicationProperties.class);
        permissionManager = mock(PermissionManager.class);
    }

    @Test
    public void anUserShouldNotBeAbleToWatchAListOfIssuesWhenWatchingIsDisabledAndAllUsersCanBrowseTheIssues()
    {
        whenWatchingIsDisabled();
        whenAnyUserIsAbleToBrowseAnyIssue();

        final WatcherService watcherService = new DefaultWatcherService
                (
                        applicationProperties, mock(I18nHelper.BeanFactory.class),
                        permissionManager, mock(WatcherManager.class), mock(UserManager.class)
                );

        assertFalse
                (
                        "If watching is disabled no user should be able to watch any issue",
                        watcherService.canWatchAll(ImmutableList.of(mock(Issue.class)), new MockApplicationUser("test-user"))
                );
    }

    private void whenAnyUserIsAbleToBrowseAnyIssue()
    {
        when(permissionManager.hasPermission(eq(Permissions.BROWSE), Matchers.<Issue>any(), Matchers.<User>any())).
                thenReturn(true);
    }

    @Test
    public void anUserShouldNotBeAbleToWatchAListOfIssuesIfLackingBrowseIssuePermissionOnAnyOfTheIssues()
    {
        final Issue unBrowsableByUser = mock(Issue.class);
        final Iterable <Issue> browsableIssues = ImmutableList.of(mock(Issue.class), mock(Issue.class));
        final ApplicationUser user = new MockApplicationUser("test-user");

        whenWatchingIsEnabled();
        whenLackingPermissionToBrowse(unBrowsableByUser, user);
        whenGrantedPermissionToBrowse(browsableIssues, user);

        final WatcherService watcherService = new DefaultWatcherService
                (
                        applicationProperties, mock(I18nHelper.BeanFactory.class),
                        permissionManager, mock(WatcherManager.class), mock(UserManager.class)
                );

        assertFalse
                (
                        "To watch a list of issues it is neccesary to have browse permission on all of them",
                        watcherService.canWatchAll(concat(browsableIssues, singletonList(unBrowsableByUser)), user)
                );
    }

    @Test
    public void anUserShouldBeAbleToWatchAListOfIssuesIfGrantedBrowseIssuePermissionOnAllTheIssues()
    {
        final Iterable<Issue> browsableIssues = ImmutableList.of(mock(Issue.class), mock(Issue.class));
        final ApplicationUser user = new MockApplicationUser("test-user");

        whenWatchingIsEnabled();
        whenGrantedPermissionToBrowse(browsableIssues, user);

        final WatcherService watcherService = new DefaultWatcherService
                (
                        applicationProperties, mock(I18nHelper.BeanFactory.class),
                        permissionManager, mock(WatcherManager.class), mock(UserManager.class)
                );

        assertTrue
                (
                        "It should be possible to watch a list of issues when you have browse permission on all of them",
                        watcherService.canWatchAll(browsableIssues, user)
                );
    }

    @Test
    public void anUserShouldBeAbleToUnwatchAnyListOfIssuesWhenWatchingIsEnabled()
    {
        whenWatchingIsEnabled();

        final WatcherService watcherService = new DefaultWatcherService
                (
                        applicationProperties, mock(I18nHelper.BeanFactory.class),
                        permissionManager, mock(WatcherManager.class), mock(UserManager.class)
                );

        assertTrue
                (
                        "If watching is enabled any user should be able to unwatch any issue(s)",
                        watcherService.canUnwatchAll(ImmutableList.of(mock(Issue.class)), new MockApplicationUser("test-user"))
                );
    }

    @Test
    public void anUserShouldNotBeAbleToUnwatchAnyListOfIssuesWhenWatchingIsDisabled()
    {
        whenWatchingIsDisabled();

        final WatcherService watcherService = new DefaultWatcherService
                (
                        applicationProperties, mock(I18nHelper.BeanFactory.class),
                        permissionManager, mock(WatcherManager.class), mock(UserManager.class)
                );

        assertFalse
                (
                        "If watching is disabled no user should be able to unwatch any issue(s)",
                        watcherService.canUnwatchAll(ImmutableList.of(mock(Issue.class)), new MockApplicationUser("test-user"))
                );
    }

    @Test
    public void bulkWatchingAListOfIssuesShouldReturnTheFailures()
    {
        whenWatchingIsEnabled();

        WatcherManager watcherManager = mock(WatcherManager.class);
        MockApplicationUser user = new MockApplicationUser("test-user", "Test User", "pwyatt@atlassian.com");
        MockApplicationUser watcher = new MockApplicationUser("test-user-2", "Test Watcher", "pwyatt@atlassian.com");

        // Mock out the i18n services (they're used to build the error messages when we don't have permission
        // to see an issue).
        final String failureMessage = "cannot see issue";
        I18nHelper.BeanFactory i18nBeanFactory = mock(I18nHelper.BeanFactory.class);
        I18nHelper i18nHelper = mock(I18nHelper.class);
        when(i18nBeanFactory.getInstance(Matchers.<ApplicationUser>anyObject())).thenReturn(i18nHelper);
        when(i18nHelper.getText(anyString(), anyString(), anyString()))
                .thenReturn(failureMessage);

        final DefaultWatcherService watcherService = new DefaultWatcherService
                (
                        applicationProperties, i18nBeanFactory,
                        permissionManager, watcherManager, mock(UserManager.class)
                );

        // Create a list of issues. Half of them, the user will be able to see, the other half, they won't.
        List<Issue> issues = new ArrayList<Issue> ();
        List<Pair<Issue, String>> expectedFailureDetails = new ArrayList<Pair<Issue, String>> ();
        List<Issue> expectedSuccessfulIssues = new ArrayList<Issue> ();

        for (int i = 0; i < 9; i++)
        {
            MockIssue issue = new MockIssue(new Long (i));
            issue.setKey("TEST-" + i);
            if (i % 2 == 0)
            {
                // The watcher can't see the even issues, though the current user can.
                whenLackingPermissionToBrowse(issue, watcher);
                whenGrantedPermissionToBrowse(issue, user);
                Pair<Issue, String> failureDetail = Pair.nicePairOf((Issue) issue, failureMessage);
                expectedFailureDetails.add(failureDetail);
            }
            else
            {
                // Both can see the odd issues.
                whenGrantedPermissionToBrowse(issue, watcher);
                whenGrantedPermissionToBrowse(issue, user);
                expectedSuccessfulIssues.add(issue);
            }
            // The current user is allowed to modify watchers on all the issues
            whenGrantedPermissionToEditWatchers(issue, user);

            issues.add(issue);
        }

        // Try the operation
        WatcherService.BulkWatchResult result = watcherService.addWatcherToAll(issues, user, watcher);

        // Validate that we got only the failed ones back
        assertArrayEquals
        (
                "If a user can only see half of the issues in a bulk watch operation, the half they couldn't see " +
                "should be returned",
                expectedFailureDetails.toArray(),
                result.getFailedIssues().toArray()
        );

        // Validate that we only tried to watch the successful ones
        verify(watcherManager).startWatching(watcher, expectedSuccessfulIssues, Contexts.nullContext());
    }

    @Test
    public void bulkUnwatchingAListOfIssuesShouldReturnTheFailures()
    {
        whenWatchingIsEnabled();

        WatcherManager watcherManager = mock(WatcherManager.class);
        MockApplicationUser user = new MockApplicationUser("test-user", "Test User", "pwyatt@atlassian.com");
        MockApplicationUser watcher = new MockApplicationUser("test-user-2", "Test Watcher", "pwyatt@atlassian.com");

        // Mock out the i18n services (they're used to build the error messages when we don't have permission
        // to see an issue).
        final String failureMessage = "cannot see issue";
        I18nHelper.BeanFactory i18nBeanFactory = mock(I18nHelper.BeanFactory.class);
        I18nHelper i18nHelper = mock(I18nHelper.class);
        when(i18nBeanFactory.getInstance(Matchers.<ApplicationUser>anyObject())).thenReturn(i18nHelper);
        when(i18nHelper.getText(anyString(), anyString(), anyString()))
                .thenReturn(failureMessage);

        final DefaultWatcherService watcherService = new DefaultWatcherService
                (
                        applicationProperties, i18nBeanFactory,
                        permissionManager, watcherManager, mock(UserManager.class)
                );

        // Create a list of issues. Half of them, the user will be able to edit watchers on, the other half, they won't.
        List<Issue> issues = new ArrayList<Issue> ();
        List<Pair<Issue, String>> expectedFailureDetails = new ArrayList<Pair<Issue, String>> ();
        List<Issue> expectedSuccessfulIssues = new ArrayList<Issue> ();

        for (int i = 0; i < 9; i++)
        {
            MockIssue issue = new MockIssue(new Long (i));
            issue.setKey("TEST-" + i);
            if (i % 2 == 0)
            {
                // We can't modify watchers on the even issues.
                whenLackingPermissionToEditWatchers(issue, user);
                Pair<Issue, String> failureDetail = Pair.nicePairOf((Issue) issue, failureMessage);
                expectedFailureDetails.add(failureDetail);
            }
            else
            {
                // We can modify watches on the odd issues.
                whenGrantedPermissionToEditWatchers(issue, user);
                expectedSuccessfulIssues.add(issue);
            }

            issues.add(issue);
        }

        // Try the operation.
        WatcherService.BulkWatchResult result = watcherService.removeWatcherFromAll(issues, user, watcher);

        // Validate that we got only the failed ones back

        assertArrayEquals
        (
                "If a user can only change the watchers of half of the issues in a bulk unwatch operation, the " +
                "issues they couldn't edit should be returned",
                expectedFailureDetails.toArray(),
                result.getFailedIssues().toArray()
        );

        // Validate that we only tried to watch the successful ones
        verify(watcherManager).stopWatching(watcher, expectedSuccessfulIssues, Contexts.nullContext());
    }

    private void whenGrantedPermissionToEditWatchers(final Issue issue, ApplicationUser user)
    {
        when(permissionManager.hasPermission(Permissions.MANAGE_WATCHER_LIST, issue, user)).
            thenReturn(true);
    }

    private void whenLackingPermissionToEditWatchers(final Issue issue, ApplicationUser user)
    {
        when(permissionManager.hasPermission(Permissions.MANAGE_WATCHER_LIST, issue, user)).
            thenReturn(false);
    }

    private void whenGrantedPermissionToBrowse(final Iterable<Issue> issues, final User user)
    {
        for (final Issue issue : issues)
        {
            whenGrantedPermissionToBrowse(issue, user);
        }
    }

    private void whenGrantedPermissionToBrowse(final Iterable<Issue> issues, final ApplicationUser user)
    {
        for (final Issue issue : issues)
        {
            whenGrantedPermissionToBrowse(issue, user);
        }
    }

    private void whenGrantedPermissionToBrowse(final Issue issue, final ApplicationUser user)
    {
        when(permissionManager.hasPermission(Permissions.BROWSE, issue, user)).
                thenReturn(true);
    }

    private void whenGrantedPermissionToBrowse(final Issue issue, final User user)
    {
        when(permissionManager.hasPermission(Permissions.BROWSE, issue, user)).
                thenReturn(true);
    }

    private void whenLackingPermissionToBrowse(final Issue issue, final ApplicationUser user)
    {
        when(permissionManager.hasPermission(Permissions.BROWSE, issue, user)).
                thenReturn(false);
    }


    private void whenLackingPermissionToBrowse(final Issue issue, final User user)
    {
        when(permissionManager.hasPermission(Permissions.BROWSE, issue, user)).
                thenReturn(false);
    }

    private void whenWatchingIsDisabled()
    {
        when(applicationProperties.getOption(APKeys.JIRA_OPTION_WATCHING)).thenReturn(false);
    }

    private void whenWatchingIsEnabled()
    {
        when(applicationProperties.getOption(APKeys.JIRA_OPTION_WATCHING)).thenReturn(true);
    }
}
