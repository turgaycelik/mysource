package com.atlassian.jira.user;

import java.util.List;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;

import com.google.common.collect.Lists;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.security.Permissions.BROWSE;
import static com.atlassian.jira.user.UserHistoryItem.ISSUE;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Default implementation of the UserHistoryManager.
 *
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultUserIssueHistoryManager
{
    private static final ApplicationUser ANONYMOUS = null;

    @Mock private UserHistoryManager historyManager;
    @Mock private IssueManager issueManager;
    @Mock private PermissionManager permissionManager;
    @Mock private Issue issue;

    private ApplicationUser user = new MockApplicationUser("Admin");
    private MockApplicationProperties applicationProperties = new MockApplicationProperties();
    private UserIssueHistoryManager issueHistoryManager;



    @Before
    public void setUp() throws Exception
    {
        issueHistoryManager = new DefaultUserIssueHistoryManager(historyManager, permissionManager, issueManager, applicationProperties);
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;
        historyManager = null;
        issueManager = null;
        permissionManager = null;
        applicationProperties = null;
        issueHistoryManager = null;
        issue = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddIssueToHistoryNoissue()
    {
        issueHistoryManager.addIssueToHistory(user, null);
    }

    @Test
    public void testAddIssueToHistoryNoUser()
    {
        when(issue.getId()).thenReturn(123L);

        issueHistoryManager.addIssueToHistory(ANONYMOUS, issue);

        verify(historyManager).addItemToHistory(ISSUE, ANONYMOUS, "123");
    }


    @Test
    public void testAddIssueToHistory()
    {
        when(issue.getId()).thenReturn(123L);

        issueHistoryManager.addIssueToHistory(user, issue);

        verify(historyManager).addItemToHistory(ISSUE, user, "123");
    }

    @Test
    public void testGetIssueHistoryNoPermission()
    {
        final UserHistoryItem item = new UserHistoryItem(ISSUE, "123");
        final UserHistoryItem item2 = new UserHistoryItem(ISSUE, "1234");
        final UserHistoryItem item3 = new UserHistoryItem(ISSUE, "1235");
        final UserHistoryItem item4 = new UserHistoryItem(ISSUE, "1236");

        final MutableIssue issue2 = new MockIssue(1234L);
        final MutableIssue issue3 = new MockIssue(1235L);
        final MutableIssue issue4 = new MockIssue(1236L);

        final List<UserHistoryItem> history = Lists.newArrayList(item, item2, item3, item4);
        when(historyManager.getHistory(ISSUE, user)).thenReturn(history);

        when(issueManager.getIssueObject(1234L)).thenReturn(issue2);
        when(issueManager.getIssueObject(1235L)).thenReturn(issue3);
        when(issueManager.getIssueObject(1236L)).thenReturn(issue4);

        assertThat(issueHistoryManager.hasIssueHistory(user), is(false));

        // Make sure that the permissions were actually checked rather than that no history was returned for some other reason
        verify(permissionManager).hasPermission(BROWSE, issue2, user);
        verify(permissionManager).hasPermission(BROWSE, issue3, user);
        verify(permissionManager).hasPermission(BROWSE, issue4, user);
    }

    @Test
    public void testGetIssueHistoryNoPermissionNoUser()
    {
        final UserHistoryItem item = new UserHistoryItem(ISSUE, "123");
        final UserHistoryItem item2 = new UserHistoryItem(ISSUE, "1234");
        final UserHistoryItem item3 = new UserHistoryItem(ISSUE, "1235");
        final UserHistoryItem item4 = new UserHistoryItem(ISSUE, "1236");

        final MutableIssue issue2 = new MockIssue(1234L);
        final MutableIssue issue3 = new MockIssue(1235L);
        final MutableIssue issue4 = new MockIssue(1236L);

        final List<UserHistoryItem> history = Lists.newArrayList(item, item2, item3, item4);
        when(historyManager.getHistory(ISSUE, ANONYMOUS)).thenReturn(history);

        when(issueManager.getIssueObject(1234L)).thenReturn(issue2);
        when(issueManager.getIssueObject(1235L)).thenReturn(issue3);
        when(issueManager.getIssueObject(1236L)).thenReturn(issue4);

        assertFalse(issueHistoryManager.hasIssueHistory(ANONYMOUS));

        // Make sure that the permissions were actually checked rather than that no history was returned for some other reason
        verify(permissionManager).hasPermission(BROWSE, issue2, ANONYMOUS);
        verify(permissionManager).hasPermission(BROWSE, issue3, ANONYMOUS);
        verify(permissionManager).hasPermission(BROWSE, issue4, ANONYMOUS);
    }

    @Test
    public void testGetIssueHistoryHasPermission()
    {
        final UserHistoryItem item = new UserHistoryItem(ISSUE, "123");
        final UserHistoryItem item2 = new UserHistoryItem(ISSUE, "1234");
        final UserHistoryItem item3 = new UserHistoryItem(ISSUE, "1235");
        final UserHistoryItem item4 = new UserHistoryItem(ISSUE, "1236");

        final MutableIssue issue2 = new MockIssue(1234L);

        final List<UserHistoryItem> history = Lists.newArrayList(item, item2, item3, item4);
        when(historyManager.getHistory(ISSUE, user)).thenReturn(history);

        when(issueManager.getIssueObject(1234L)).thenReturn(issue2);
        when(permissionManager.hasPermission(BROWSE, issue2, user)).thenReturn(true);

        assertTrue(issueHistoryManager.hasIssueHistory(user));

        verify(permissionManager).hasPermission(BROWSE, issue2, user);
    }

    @Test
    public void testGetIssueHistoryNoHistory()
    {
        assertFalse(issueHistoryManager.hasIssueHistory(user));

        verify(historyManager).getHistory(ISSUE, user);
    }

    @Test
    public void testGetIssueHistoryEmptyHistory()
    {
        final List<UserHistoryItem> history = Lists.newArrayList();
        when(historyManager.getHistory(ISSUE, user)).thenReturn(history);

        assertFalse(issueHistoryManager.hasIssueHistory(user));
    }

    @Test
    public void testFullHistoryNoChecks()
    {
        final UserHistoryItem item = new UserHistoryItem(ISSUE, "123");
        final UserHistoryItem item2 = new UserHistoryItem(ISSUE, "1234");
        final UserHistoryItem item3 = new UserHistoryItem(ISSUE, "1235");
        final UserHistoryItem item4 = new UserHistoryItem(ISSUE, "1236");

        final List<UserHistoryItem> history = Lists.newArrayList(item, item2, item3, item4);
        when(historyManager.getHistory(ISSUE, user)).thenReturn(history);

        assertThat(issueHistoryManager.getFullIssueHistoryWithoutPermissionChecks(user), equalTo(history));
    }

    @Test
    public void testFullHistoryNoChecksNoHistory()
    {
        assertThat(issueHistoryManager.getFullIssueHistoryWithoutPermissionChecks(user), emptyHistory());
    }

    @Test
    public void testFullHistoryWithChecks()
    {
        final UserHistoryItem item = new UserHistoryItem(ISSUE, "123", 41L);
        final UserHistoryItem item2 = new UserHistoryItem(ISSUE, "1234", 42L);
        final UserHistoryItem item3 = new UserHistoryItem(ISSUE, "1235", 43L);
        final UserHistoryItem item4 = new UserHistoryItem(ISSUE, "1236", 44L);

        final MutableIssue issue2 = new MockIssue(1234L);
        final MutableIssue issue3 = new MockIssue(1235L);
        final MutableIssue issue4 = new MockIssue(1236L);


        final List<UserHistoryItem> history = Lists.newArrayList(item, item2, item3, item4);
        when(historyManager.getHistory(ISSUE, user)).thenReturn(history);

        when(issueManager.getIssueObject(1234L)).thenReturn(issue2);
        when(issueManager.getIssueObject(1235L)).thenReturn(issue3);
        when(issueManager.getIssueObject(1236L)).thenReturn(issue4);

        when(permissionManager.hasPermission(BROWSE, issue2, user)).thenReturn(true);
        when(permissionManager.hasPermission(BROWSE, issue4, user)).thenReturn(true);

        assertThat(issueHistoryManager.getFullIssueHistoryWithPermissionChecks(user), contains(item2, item4));
    }

    @Test
    public void testFullHistoryWithChecksNoPerms()
    {
        final UserHistoryItem item = new UserHistoryItem(ISSUE, "123", 41L);
        final UserHistoryItem item2 = new UserHistoryItem(ISSUE, "1234", 42L);
        final UserHistoryItem item3 = new UserHistoryItem(ISSUE, "1235", 43L);
        final UserHistoryItem item4 = new UserHistoryItem(ISSUE, "1236", 44L);

        final MutableIssue issue2 = new MockIssue(1234L);
        final MutableIssue issue3 = new MockIssue(1235L);
        final MutableIssue issue4 = new MockIssue(1236L);

        final List<UserHistoryItem> history = Lists.newArrayList(item, item2, item3, item4);
        when(historyManager.getHistory(ISSUE, user)).thenReturn(history);

        when(issueManager.getIssueObject(1234L)).thenReturn(issue2);
        when(issueManager.getIssueObject(1235L)).thenReturn(issue3);
        when(issueManager.getIssueObject(1236L)).thenReturn(issue4);

        assertThat(issueHistoryManager.getFullIssueHistoryWithPermissionChecks(user), emptyHistory());
    }

    @Test
    public void testFullHistoryWithChecksNoHistory()
    {
        assertThat(issueHistoryManager.getFullIssueHistoryWithPermissionChecks(user), emptyHistory());
    }

    @Test
    public void testShortHistoryWithChecks()
    {
        applicationProperties.setString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS, "2");

        final UserHistoryItem item = new UserHistoryItem(ISSUE, "123", 41L);
        final UserHistoryItem item2 = new UserHistoryItem(ISSUE, "1234", 42L);
        final UserHistoryItem item3 = new UserHistoryItem(ISSUE, "1235", 43L);
        final UserHistoryItem item4 = new UserHistoryItem(ISSUE, "1236", 44L);

        final MutableIssue issue2 = new MockIssue(1234L);
        final MutableIssue issue3 = new MockIssue(1235L);
        final MutableIssue issue4 = new MockIssue(1236L);

        final List<UserHistoryItem> history = Lists.newArrayList(item, item2, item3, item4);
        when(historyManager.getHistory(ISSUE, user)).thenReturn(history);

        when(issueManager.getIssueObject(1234L)).thenReturn(issue2);
        when(issueManager.getIssueObject(1235L)).thenReturn(issue3);
        when(issueManager.getIssueObject(1236L)).thenReturn(issue4);

        when(permissionManager.hasPermission(BROWSE, issue2, user)).thenReturn(true);
        when(permissionManager.hasPermission(BROWSE, issue4, user)).thenReturn(true);

        assertThat(issueHistoryManager.getShortIssueHistory(user), issues(issue2, issue4));
    }

    @Test
    public void testShortHistoryWithChecksTooMany()
    {
        applicationProperties.setString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS, "2");

        final UserHistoryItem item = new UserHistoryItem(ISSUE, "123");
        final UserHistoryItem item2 = new UserHistoryItem(ISSUE, "1234");
        final UserHistoryItem item3 = new UserHistoryItem(ISSUE, "1235");
        final UserHistoryItem item4 = new UserHistoryItem(ISSUE, "1236");

        final MutableIssue issue = new MockIssue(123L);
        final MutableIssue issue2 = new MockIssue(1234L);
        final MutableIssue issue3 = new MockIssue(1235L);
        final MutableIssue issue4 = new MockIssue(1236L);

        final List<UserHistoryItem> history = Lists.newArrayList(item, item2, item3, item4);
        when(historyManager.getHistory(ISSUE, user)).thenReturn(history);

        when(issueManager.getIssueObject(123L)).thenReturn(issue);
        when(issueManager.getIssueObject(1234L)).thenReturn(issue2);
        when(issueManager.getIssueObject(1235L)).thenReturn(issue3);
        when(issueManager.getIssueObject(1236L)).thenReturn(issue4);

        when(permissionManager.hasPermission(BROWSE, issue, user)).thenReturn(true);
        when(permissionManager.hasPermission(BROWSE, issue2, user)).thenReturn(true);

        assertThat(issueHistoryManager.getShortIssueHistory(user), issues(issue, issue2));

        verify(issueManager, never()).getIssueObject(1235L);
        verify(issueManager, never()).getIssueObject(1236L);
        verify(permissionManager, never()).hasPermission(BROWSE, issue3, user);
        verify(permissionManager, never()).hasPermission(BROWSE, issue4, user);
    }

    @Test
    public void testShortHistoryWithChecksNotEnough()
    {
        applicationProperties.setString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS, "6");

        final UserHistoryItem item = new UserHistoryItem(ISSUE, "123", 41L);
        final UserHistoryItem item2 = new UserHistoryItem(ISSUE, "1234", 42L);
        final UserHistoryItem item3 = new UserHistoryItem(ISSUE, "1235", 43L);
        final UserHistoryItem item4 = new UserHistoryItem(ISSUE, "1236", 44L);

        final MutableIssue issue = new MockIssue(123L);
        final MutableIssue issue2 = new MockIssue(1234L);
        final MutableIssue issue3 = new MockIssue(1235L);
        final MutableIssue issue4 = new MockIssue(1236L);

        final List<UserHistoryItem> history = Lists.newArrayList(item, item2, item3, item4);

        when(historyManager.getHistory(ISSUE, user)).thenReturn(history);
        when(issueManager.getIssueObject(123L)).thenReturn(issue);
        when(issueManager.getIssueObject(1234L)).thenReturn(issue2);
        when(issueManager.getIssueObject(1235L)).thenReturn(issue3);
        when(issueManager.getIssueObject(1236L)).thenReturn(issue4);

        when(permissionManager.hasPermission(BROWSE, issue, user)).thenReturn(true);
        when(permissionManager.hasPermission(BROWSE, issue2, user)).thenReturn(true);
        when(permissionManager.hasPermission(BROWSE, issue3, user)).thenReturn(true);
        when(permissionManager.hasPermission(BROWSE, issue4, user)).thenReturn(true);

        assertThat(issueHistoryManager.getShortIssueHistory(user), issues(issue, issue2, issue3, issue4));
    }



    static Matcher<Iterable<UserHistoryItem>> emptyHistory()
    {
        return emptyIterable();
    }

    static Matcher<Iterable<Issue>> issues(Issue... issues)
    {
        return contains(issues);
    }
}
