package com.atlassian.jira.bc.user.search;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.MockUserKeyService;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.util.UserManager;

import com.google.common.collect.Sets;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DefaultAssigneeService}.
 *
 * @since v5.0
 */
@RunWith (ListeningMockitoRunner.class)
public class TestDefaultAssigneeService
{
    @Mock PermissionContextFactory permissionContextFactory;
    @Mock ChangeHistoryManager changeHistoryManager;
    @Mock UserHistoryManager userHistoryManager;
    @Mock UserManager userManager;
    @Mock PermissionSchemeManager permissionSchemeManager;
    @Mock FeatureManager featureManager;
    @Mock JiraAuthenticationContext authenticationContext;

    private DefaultAssigneeService service;

    @AvailableInContainer
    private MockUserKeyService userKeyService = new MockUserKeyService();
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Before
    public void setUp() throws Exception
    {
        service = new DefaultAssigneeService(permissionContextFactory, permissionSchemeManager, userHistoryManager, featureManager, authenticationContext, userKeyService);
        service.setChangeHistoryManager(changeHistoryManager);
    }

    @Test
    public void testFindAssignableUsers() throws Exception
    {
        Issue issue = new MockIssue(12345);
        List<User> users = makeUsers(11);
        when(permissionSchemeManager.getUsers(anyLong(), Matchers.<PermissionContext>any())).thenReturn(users);

        when(authenticationContext.getLocale()).thenReturn(Locale.getDefault());

        Collection<User> foundUsers = service.findAssignableUsers("5", issue, null);
        assertEquals(1, foundUsers.size());
        assertTrue(foundUsers.contains(users.get(5)));  // User *5* matches

        foundUsers = service.findAssignableUsers("1", issue, null);
        assertEquals(2, foundUsers.size());
        assertTrue(foundUsers.contains(users.get(1)));  // User *1* matches
        assertTrue(foundUsers.contains(users.get(10))); // User *1*0 matches

        foundUsers = service.findAssignableUsers("11", issue, null);
        assertEquals(0, foundUsers.size());

        foundUsers = service.findAssignableUsers("USER 6", issue, null);
        assertEquals(1, foundUsers.size());
        assertTrue("Should have found User 6", foundUsers.contains(users.get(6)));  // User *6* matches once USER is lowercased

        foundUsers = service.findAssignableUsers("", issue, null);
        assertEquals("All assignable users should have been returned", 11, foundUsers.size());
    }

    @Test
    public void testGetSuggestedAssignees() throws Exception
    {
        MockIssue issue = new MockIssue(12345);
        User user = new MockUser("fzappa");
        List<User> assignableUsers = makeUsers(10);

        issue.setReporter(assignableUsers.get(2)); // user2
        List<UserHistoryItem> userHistoryItems = makeUserHistoryItems(3);
        when(userHistoryManager.getHistory(UserHistoryItem.ASSIGNEE, user)).thenReturn(userHistoryItems.subList(0, 2));  // user0,1

        List<ChangeItemBean> changeItemsForField = makeChangeItems(6);
        when(changeHistoryManager.getChangeItemsForField(issue, "assignee")).thenReturn(changeItemsForField.subList(3, 5)); // user3,4

        List<User> suggestedAssignees = service.getSuggestedAssignees(issue, user, assignableUsers);
        assertEquals(5, suggestedAssignees.size());
        for (int i = 0; i < 5; i++)
        {
            // The correct users should be present and the order should be correct.
            assertEquals(suggestedAssignees.get(i), assignableUsers.get(i));
        }
    }

    @Test
    public void testGetSuggestedAssigneesWithRenamedAndRecycledUsers() throws Exception
    {
        MockIssue issue = new MockIssue(12345);
        User user = new MockUser("fzappa");
        List<User> assignableUsers = makeUsers(10);

        ((MockUserKeyService) userKeyService).setMapping("frankz", "fzappa");
        ((MockUserKeyService) userKeyService).setMapping("foo", "user0");
        ((MockUserKeyService) userKeyService).setMapping("ID1001", "user1");
        ((MockUserKeyService) userKeyService).setMapping("user0", "user3");

        issue.setReporter(assignableUsers.get(2)); // user2
        List<UserHistoryItem> userHistoryItems = makeUserHistoryItems(3);
        when(userHistoryManager.getHistory(UserHistoryItem.ASSIGNEE, user)).thenReturn(userHistoryItems.subList(0, 2));  // user0,1

        List<ChangeItemBean> changeItemsForField = makeChangeItems(6);
        when(changeHistoryManager.getChangeItemsForField(issue, "assignee")).thenReturn(changeItemsForField.subList(3, 5)); // user3,4

        List<User> suggestedAssignees = service.getSuggestedAssignees(issue, user, assignableUsers);
        assertEquals(5, suggestedAssignees.size());
        for (int i = 0; i < 5; i++)
        {
            // The correct users should be present and the order should be correct.
            assertEquals(suggestedAssignees.get(i), assignableUsers.get(i));
        }
    }

    @Test
    public void testGetSuggestedAssigneesForNames() throws Exception
    {
        List<User> users = makeUsers(10);
        List<User> suggestedAssignees = service.getSuggestedAssignees(Sets.newHashSet("user2", "user5", "user3"), users);
        assertEquals(3, suggestedAssignees.size());
        assertTrue(suggestedAssignees.contains(users.get(2)));
        assertTrue(suggestedAssignees.contains(users.get(5)));
        assertTrue(suggestedAssignees.contains(users.get(3)));
    }

    @Test
    public void testGetSuggestedAssigneesForNamesWithRenamedAndRecycledUsers() throws Exception
    {
        List<User> users = makeUsers(10);

        ((MockUserKeyService) userKeyService).setMapping("foo", "user2");
        ((MockUserKeyService) userKeyService).setMapping("ID1001", "user5");
        ((MockUserKeyService) userKeyService).setMapping("user2", "user3");

        List<User> suggestedAssignees = service.getSuggestedAssignees(Sets.newHashSet("user2", "user5", "user3"), users);
        assertEquals(3, suggestedAssignees.size());
        assertTrue(suggestedAssignees.contains(users.get(2)));
        assertTrue(suggestedAssignees.contains(users.get(5)));
        assertTrue(suggestedAssignees.contains(users.get(3)));
    }

    @Test
    public void testGetAssignableUsers() throws Exception
    {
        List<User> users = makeUsers(10);
        MockUser realUser5 = new MockUser("user5", "Dupe User 5", null);
        users.add(realUser5);  // this duped user should only be counted once
        when(permissionSchemeManager.getUsers(anyLong(), Matchers.<PermissionContext>any())).thenReturn(users);
        when(userManager.getUser("user5")).thenReturn(realUser5);
        when(authenticationContext.getLocale()).thenReturn(Locale.getDefault());

        ActionDescriptor actionDescriptor = DescriptorFactory.getFactory().createActionDescriptor();
        MockIssue issue = new MockIssue(12345);
        List<User> assignableUsers = service.getAssignableUsers(issue, actionDescriptor);
        assertEquals(10, assignableUsers.size());
        assertTrue(assignableUsers.contains(realUser5));
    }

    @Test
    public void testGetRecentAssigneeNamesForUser()
    {
        User user = new MockUser("fzappa");
        List<UserHistoryItem> userHistoryItems = makeUserHistoryItems(20);
        when(userHistoryManager.getHistory(UserHistoryItem.ASSIGNEE, user)).thenReturn(userHistoryItems);

        final Set<String> results = service.getRecentAssigneeNamesForUser(user);
        assertNotNull(results);
        assertEquals("Number of results should be limited to 5", 5, results.size());
        for (int i = 19; i >= 15; i--)
        {
            assertTrue("Check that result contains user" + i + ". Results contained :" + results, results.contains("user" + i));
        }
    }

    @Test
    public void testGetRecentAssigneeNamesForIssue()
    {
        userKeyService.setMapping("currentassignee", "currentAssignee");
        ApplicationUser currentAssignee = new MockApplicationUser("currentassignee", "currentAssignee");

        MockIssue issue = new MockIssue(12345);
        issue.setAssignee(currentAssignee.getDirectoryUser());

        List<ChangeItemBean> changeItemsForField = makeChangeItems(7);
        when(changeHistoryManager.getChangeItemsForField(issue, "assignee")).thenReturn(changeItemsForField);

        final Set<String> results = service.getRecentAssigneeNamesForIssue(issue);
        assertNotNull(results);
        assertEquals("Number of results should be limited to 5 plus current assignee", 6, results.size());
        for (int i = 2; i < 6; i++)
        {
            assertTrue(results.contains("user" + i));
        }
        assertTrue(results.contains("currentAssignee"));
    }
    @Test
    public void testGetRecentAssigneeKeysForIssue()
    {
        ApplicationUser currentAssignee = new MockApplicationUser("currentassignee", "currentAssignee");

        MockIssue issue = new MockIssue(12345);
        issue.setAssignee(currentAssignee.getDirectoryUser());

        List<ChangeItemBean> changeItemsForField = makeChangeItems(7);
        when(changeHistoryManager.getChangeItemsForField(issue, "assignee")).thenReturn(changeItemsForField);

        final Set<String> results = service.getRecentAssigneeNamesForIssue(issue);
        assertNotNull(results);
        assertEquals("Number of results should be limited to 5 plus current assignee", 6, results.size());
        for (int i = 2; i < 6; i++)
        {
            assertTrue(results.contains("user" + i));
        }
        assertTrue(results.contains("currentassignee"));
    }

    @Test
    public void testMakeUniqueFullNamesMap() throws Exception
    {
        service = new DefaultAssigneeService(null, null, null, null, null, userKeyService);
        List<User> users = new ArrayList<User>();
        String nonUniqueName = "Non-Unique Name";
        users.add(new MockUser("uneek", nonUniqueName, null));
        users.add(new MockUser("uneek2", nonUniqueName, null));

        Map<String,Boolean> map = service.makeUniqueFullNamesMap(users);
        assertEquals("Map should only have one full name", 1, map.size());
        assertTrue("Map should have entry for Non-unique name", map.containsKey(nonUniqueName));
        assertFalse("Non-unique name should have Boolean.FALSE value", map.get(nonUniqueName));
    }

    private List<ChangeItemBean> makeChangeItems(final int numItems)
    {
        List<ChangeItemBean> changeItemsForField = new ArrayList<ChangeItemBean>();
        long time = System.currentTimeMillis();
        for (int i = 0; i < numItems; i++)
        {
            // ChangeItems store user keys, so use UserKeyService in case the test set up some mappings
            String username = "user" + i;
            String userKey = userKeyService.getKeyForUsername(username);
            changeItemsForField.add(new ChangeItemBean("assignee", "assignee", "fakeOldAssignee", "fakeOldAssignee", userKey, "user" + i, new Timestamp(time + i)));
        }
        return changeItemsForField;
    }

    private List<User> makeUsers(final int numUsers)
    {
        List<User> users = new ArrayList<User>();
        for (int i = 0; i < numUsers; i++)
        {
            users.add(new MockUser("user" + i, "User " + i, null));
        }
        return users;
    }

    private List<UserHistoryItem> makeUserHistoryItems(final int numItems)
    {
        List<UserHistoryItem> userHistoryItems = new ArrayList<UserHistoryItem>();
        for (int i = 0; i < numItems; i++)
        {
            // HistoryItems store user keys, so use UserKeyService in case the test set up some mappings
            String username = "user" + i;
            String userKey = userKeyService.getKeyForUsername(username);
            userHistoryItems.add(new UserHistoryItem(UserHistoryItem.ASSIGNEE, userKey));
            //need to wait a little to make sure the timestamps are different. Otherwise the sorting could result
            //in flaky tests
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }
        return userHistoryItems;
    }
}