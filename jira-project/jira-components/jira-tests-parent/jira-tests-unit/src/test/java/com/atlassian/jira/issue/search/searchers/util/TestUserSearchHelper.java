package com.atlassian.jira.issue.search.searchers.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserFilter;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;

import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.commons.collections.CollectionUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.when;


/**
 * Tests for {@link com.atlassian.jira.issue.search.searchers.renderer.AbstractUserSearchRenderer}.
 *
 * @since v5.2
 */
@RunWith (MockitoJUnitRunner.class)
public class TestUserSearchHelper
{

    private UserSearcherHelperImpl concreteUserSearchHelper;
    @Mock private GroupManager groupManager;
    @Mock private PermissionManager permissionManager;
    @Mock private UserHistoryManager userHistoryManager;
    @Mock private User user;
    @Mock private UserUtil userUtil;
    @Mock private UserManager userManager;
    @Mock private UserPickerSearchService userPickerSearchService;

    @Rule public MockitoContainer mockitoContainer = MockitoMocksInContainer.rule(this);

    @Mock @AvailableInContainer private I18nHelper.BeanFactory beanFactory;
    private MockI18nHelper i18nHelper = new MockI18nHelper();

    @Before
    public void setUp()
    {
        concreteUserSearchHelper = new UserSearcherHelperImpl(groupManager, permissionManager, userUtil, userManager, userHistoryManager, userPickerSearchService);
        when(user.getName()).thenReturn("user");
        when(beanFactory.getInstance(any(User.class))).thenReturn(i18nHelper);
    }

    /**
     * If the user is logged in, we should suggest groups they're a member of.
     */
    @Test
    public void testGetSuggestedGroups()
    {
        List<Group> groups = Lists.<Group>newArrayList(
                new MockGroup("1"), new MockGroup("2"), new MockGroup("3"),
                new MockGroup("4"), new MockGroup("5"), new MockGroup("6"));

        when(groupManager.getGroupsForUser(user)).thenReturn(groups);

        assertEquals(groups.subList(0, 5), concreteUserSearchHelper.getSuggestedGroups(user));
    }

    /**
     * If the user isn't logged in and anonymous users don't have browse
     * permission, we shouldn't suggest any groups.
     */
    @Test
    public void testGetSuggestedGroupsAnonymous()
    {
        //noinspection deprecation
        when(permissionManager.hasPermission(Permissions.USER_PICKER, (User) null)).thenReturn(false);
        assertNull(concreteUserSearchHelper.getSuggestedGroups(null));
    }

    /**
     * If the user isn't logged in but anonymous users have browse permission,
     * we should suggest the first 5 groups in the system sorted alphabetically.
     */
    @Test
    public void testGetSuggestedGroupsAnonymousBrowsePermission()
    {
        List<Group> groups = Lists.<Group>newArrayList(
                new MockGroup("1"), new MockGroup("2"), new MockGroup("3"),
                new MockGroup("4"), new MockGroup("5"), new MockGroup("6"));

        when(groupManager.getAllGroups()).thenReturn(groups);
        when(permissionManager.hasPermission(Permissions.USER_PICKER, (ApplicationUser) null)).thenReturn(true);
        assertEquals(groups.subList(0, 5), concreteUserSearchHelper.getSuggestedGroups(null));
    }

    /**
     * If the user has browse permission, we should suggest the first 5 users in
     * the system, sorted alphabetically.
     */
    @Test
    public void testGetSuggestedUsers()
    {
        List<User> users = Lists.<User>newArrayList(
                new MockUser("1"), new MockUser("2"), new MockUser("3"),
                new MockUser("4"), new MockUser("5"), new MockUser("6"));

        when(permissionManager.hasPermission(Permissions.USER_PICKER, ApplicationUsers.from(user))).thenReturn(true);
        when(userUtil.getActiveUserCount()).thenReturn(users.size()); // return <=10 to make sure that allUsers are used
        when(userPickerSearchService.findUsers(eq(""), any(UserSearchParams.class))).thenReturn(users);
        when(userPickerSearchService.userMatches(any(ApplicationUser.class), any(UserSearchParams.class))).thenReturn(true);
        assertEquals(ApplicationUsers.from(users.subList(0, 5)), concreteUserSearchHelper.getSuggestedUsers(user, new ArrayList<String>(), UserSearchParams.ACTIVE_USERS_ALLOW_EMPTY_QUERY));
    }

    /**
     * If the user has USED_USER history, then we should expect to see that in the results
     */
    @Test
    public void testGetSuggestedUsersWithUsedUserHistory()
    {
        MockUser u1 = new MockUser("1");
        MockUser u2 = new MockUser("2");
        MockUser u3 = new MockUser("3");
        MockUser u4 = new MockUser("4");
        MockUser u5 = new MockUser("5");
        MockUser u6 = new MockUser("6");
        MockUser u7 = new MockUser("7");
        List<User> allUsers = Lists.<User>newArrayList(u1, u2, u3, u4, u5, u6, u7);

        when(permissionManager.hasPermission(Permissions.USER_PICKER, ApplicationUsers.from(user))).thenReturn(true);
        List<UserHistoryItem> usedusers = Lists.newArrayList(
                new UserHistoryItem(UserHistoryItem.USED_USER, "2"), new UserHistoryItem(UserHistoryItem.USED_USER, "5"));
        when(userManager.getUserByName("2")).thenReturn(ApplicationUsers.from(u2));
        when(userManager.getUserByName("5")).thenReturn(ApplicationUsers.from(u5));

        when(userHistoryManager.getHistory(UserHistoryItem.USED_USER, ApplicationUsers.from(user))).thenReturn(usedusers);
        when(userUtil.getActiveUserCount()).thenReturn(7); // return <=10 to make sure that allUsers are used
        when(userPickerSearchService.findUsers(eq(""), any(UserSearchParams.class))).thenReturn(allUsers);
        when(userPickerSearchService.userMatches(any(ApplicationUser.class), any(UserSearchParams.class))).thenReturn(true);

        assertThat(concreteUserSearchHelper.getSuggestedUsers(user, newArrayList("5", "4"), UserSearchParams.ACTIVE_USERS_ALLOW_EMPTY_QUERY),
                equalTo(ApplicationUsers.from(ImmutableList.<User>of(u2, u1, u3, u6, u7))));
    }

    /**
     * If the user has USED_USER history, then we should expect to see that in the results, but filtered by group restrictions
     */
    @Test
    public void testGetSuggestedUsersWithUsedUserHistoryWithGroupFilters()
    {
        MockUser u1 = new MockUser("1");
        MockUser u2 = new MockUser("2");
        MockUser u3 = new MockUser("3");
        MockUser u4 = new MockUser("4");
        MockUser u5 = new MockUser("5");
        MockUser u6 = new MockUser("6");
        MockUser u7 = new MockUser("7");
        MockUser u8 = new MockUser("8");
        List<User> allUsers = Lists.<User>newArrayList(u1, u2, u3, u4, u5, u6, u7, u8);

        when(permissionManager.hasPermission(Permissions.USER_PICKER, ApplicationUsers.from(user))).thenReturn(true);
        List<UserHistoryItem> usedusers = Lists.newArrayList(
                new UserHistoryItem(UserHistoryItem.USED_USER, "2"),
                new UserHistoryItem(UserHistoryItem.USED_USER, "3"),
                new UserHistoryItem(UserHistoryItem.USED_USER, "5"));
        when(userManager.getUserByName("2")).thenReturn(ApplicationUsers.from(u2));
        when(userManager.getUserByName("3")).thenReturn(ApplicationUsers.from(u3));
        when(userManager.getUserByName("5")).thenReturn(ApplicationUsers.from(u5));

        when(userHistoryManager.getHistory(UserHistoryItem.USED_USER, ApplicationUsers.from(user))).thenReturn(usedusers);
        when(userUtil.getActiveUserCount()).thenReturn(7); // return <=10 to make sure that allUsers are used
        when(userPickerSearchService.findUsers(eq(""), any(UserSearchParams.class))).thenReturn(allUsers);
        when(userPickerSearchService.userMatches(any(ApplicationUser.class), eqGroup("group1"))).thenReturn(true); // group1 is considered matched
        when(userPickerSearchService.userMatches(eq(ApplicationUsers.from(u3)), any(UserSearchParams.class))).thenReturn(false); // u3 not matched
        when(userPickerSearchService.userMatches(eq(ApplicationUsers.from(u2)), any(UserSearchParams.class))).thenReturn(true); // u2 matched

        // only u2 is returned, u3 fitlered by group
        assertThat(concreteUserSearchHelper.getSuggestedUsers(user, newArrayList("5", "4"),
                UserSearchParams.builder(UserSearchParams.ACTIVE_USERS_ALLOW_EMPTY_QUERY).filter(new UserFilter(true, null, ImmutableSet.of("group1"))).build()),
                equalTo(ApplicationUsers.from(ImmutableList.<User>of(u2, u1, u6, u7, u8))));
    }

    /**
     * If the user isn't logged in and anonymous users don't have browse
     * permission, we shouldn't suggest any users.
     */
    @Test
    public void testGetSuggestedUsersAnonymous()
    {
        //noinspection deprecation
        when(permissionManager.hasPermission(Permissions.USER_PICKER, (User) null)).thenReturn(false);
        assertNull(concreteUserSearchHelper.getSuggestedUsers(null, Collections.<String>emptyList(), UserSearchParams.ACTIVE_USERS_ALLOW_EMPTY_QUERY));
    }

    /**
     * If the user isn't logged in, but anonymous users have browse permission,
     * we should suggest the first 5 users in the system, sorted alphabetically.
     */
    @Test
    public void testGetSuggestedUsersAnonymousBrowsePermission()
    {
        List<User> users = Lists.<User>newArrayList(
                new MockUser("1"), new MockUser("2"), new MockUser("3"),
                new MockUser("4"), new MockUser("5"), new MockUser("6"));

        when(permissionManager.hasPermission(eq(Permissions.USER_PICKER), isNull(ApplicationUser.class))).thenReturn(true);
        when(userUtil.getActiveUserCount()).thenReturn(users.size()); // return <=10 to make sure that allUsers are used
        when(userPickerSearchService.findUsers(eq(""), any(UserSearchParams.class))).thenReturn(users);
        when(userPickerSearchService.userMatches(any(ApplicationUser.class), emptyGroup())).thenReturn(true); // all null groups are considered matched
        assertEquals(ApplicationUsers.from(users.subList(0, 5)), concreteUserSearchHelper.getSuggestedUsers(null, Collections.<String>emptyList(), UserSearchParams.ACTIVE_USERS_ALLOW_EMPTY_QUERY));
    }

    @Test
    public void testAddToSuggestList_Empty()
    {
        LinkedHashSet<ApplicationUser> suggestedUsers = Sets.newLinkedHashSet(ApplicationUsers.from(ImmutableList.<User>of(new MockUser("1"))));
        Set<ApplicationUser> originalSuggestedUsers = ImmutableSet.copyOf(suggestedUsers);
        concreteUserSearchHelper.addToSuggestList(10, suggestedUsers, ImmutableList.<ApplicationUser>of(), ImmutableList.<String>of(), UserSearchParams.ACTIVE_USERS_ALLOW_EMPTY_QUERY);
        assertThat(suggestedUsers, equalTo(originalSuggestedUsers));
    }

    @Test
    public void testAddToSuggestList_Inactive()
    {
        LinkedHashSet<ApplicationUser> suggestedUsers = Sets.newLinkedHashSet(ApplicationUsers.from(ImmutableList.<User>of(new MockUser("1"))));
        Set<ApplicationUser> originalSuggestedUsers = ImmutableSet.copyOf(suggestedUsers);
        ApplicationUser user1 = Mockito.mock(ApplicationUser.class);
        when(userPickerSearchService.userMatches(any(ApplicationUser.class), emptyGroup())).thenReturn(true); // all null groups are considered matched
        when(userPickerSearchService.userMatches(eq(user1), emptyGroup())).thenReturn(false); // return false for user1
        concreteUserSearchHelper.addToSuggestList(10, suggestedUsers, ImmutableList.of(user1), ImmutableList.<String>of(), UserSearchParams.ACTIVE_USERS_ALLOW_EMPTY_QUERY);
        assertThat(suggestedUsers, equalTo(originalSuggestedUsers));
    }

    @Test
    public void testAddToSuggestList_Active()
    {
        LinkedHashSet<ApplicationUser> suggestedUsers = Sets.newLinkedHashSet(ApplicationUsers.from(ImmutableList.<User>of(new MockUser("1"))));
        Set<ApplicationUser> originalSuggestedUsers = ImmutableSet.copyOf(suggestedUsers);
        ApplicationUser user1 = Mockito.mock(ApplicationUser.class);
        when(user1.isActive()).thenReturn(true);
        when(userPickerSearchService.userMatches(any(ApplicationUser.class), emptyGroup())).thenReturn(true); // all null groups are considered matched
        concreteUserSearchHelper.addToSuggestList(10, suggestedUsers, ImmutableList.of(user1), ImmutableList.<String>of(), UserSearchParams.ACTIVE_USERS_ALLOW_EMPTY_QUERY);
        assertThat(suggestedUsers.size(), equalTo(originalSuggestedUsers.size() + 1));
        assertThat(suggestedUsers, hasItem(user1));
    }

    @Test
    public void testAddToSuggestList_InactiveButAllowed()
    {
        LinkedHashSet<ApplicationUser> suggestedUsers = Sets.newLinkedHashSet(ApplicationUsers.from(ImmutableList.<User>of(new MockUser("1"))));
        Set<ApplicationUser> originalSuggestedUsers = ImmutableSet.copyOf(suggestedUsers);
        ApplicationUser user1 = Mockito.mock(ApplicationUser.class);
        when(user1.isActive()).thenReturn(false);
        when(userPickerSearchService.userMatches(any(ApplicationUser.class), emptyGroup())).thenReturn(true); // all null groups are considered matched
        concreteUserSearchHelper.addToSuggestList(10, suggestedUsers, ImmutableList.of(user1), ImmutableList.<String>of(), UserSearchParams.builder().includeInactive(true).build());
        assertThat(suggestedUsers.size(), equalTo(originalSuggestedUsers.size() + 1));
        assertThat(suggestedUsers, hasItem(user1));
    }

    @Test
    public void testAddToSuggestList_InSuggested()
    {
        LinkedHashSet<ApplicationUser> suggestedUsers = Sets.newLinkedHashSet(ApplicationUsers.from(ImmutableList.<User>of(new MockUser("1"))));
        Set<ApplicationUser> originalSuggestedUsers = ImmutableSet.copyOf(suggestedUsers);
        ApplicationUser user1 = Mockito.mock(ApplicationUser.class);
        when(user1.isActive()).thenReturn(false);
        when(userPickerSearchService.userMatches(any(ApplicationUser.class), emptyGroup())).thenReturn(true); // all null groups are considered matched
        concreteUserSearchHelper.addToSuggestList(10, suggestedUsers, ImmutableList.copyOf(suggestedUsers), ImmutableList.<String>of(), UserSearchParams.ACTIVE_USERS_ALLOW_EMPTY_QUERY);
        assertThat(suggestedUsers, equalTo(originalSuggestedUsers));
    }

    @Test
    public void testAddToSuggestList_AlreadySelected()
    {
        LinkedHashSet<ApplicationUser> suggestedUsers = Sets.newLinkedHashSet(ApplicationUsers.from(ImmutableList.<User>of(new MockUser("1"))));
        Set<ApplicationUser> originalSuggestedUsers = ImmutableSet.copyOf(suggestedUsers);
        ApplicationUser user1 = Mockito.mock(ApplicationUser.class);
        when(user1.getName()).thenReturn("username 1");
        when(userPickerSearchService.userMatches(any(ApplicationUser.class), emptyGroup())).thenReturn(true); // all null groups are considered matched
        concreteUserSearchHelper.addToSuggestList(10, suggestedUsers, ImmutableList.of(user1), ImmutableList.of(user1.getName()), UserSearchParams.ACTIVE_USERS_ALLOW_EMPTY_QUERY);
        assertThat(suggestedUsers, equalTo(originalSuggestedUsers));
    }

    @Test
    public void testAddToSuggestList_NeitherGroupNorRole()
    {
        LinkedHashSet<ApplicationUser> suggestedUsers = Sets.newLinkedHashSet(ApplicationUsers.from(ImmutableList.<User>of(new MockUser("1"))));
        Set<ApplicationUser> originalSuggestedUsers = ImmutableSet.copyOf(suggestedUsers);
        ApplicationUser user1 = Mockito.mock(ApplicationUser.class);
        when(user1.isActive()).thenReturn(true);
        concreteUserSearchHelper.addToSuggestList(10, suggestedUsers, ImmutableList.of(user1), ImmutableList.<String>of(),
                UserSearchParams.builder().includeInactive(true).filter(new UserFilter(true, null, ImmutableSet.of("group1"))).build());
        assertThat(suggestedUsers, equalTo(originalSuggestedUsers));
    }

    @Test
    public void testAddToSuggestList_ByGroup()
    {
        LinkedHashSet<ApplicationUser> suggestedUsers = Sets.newLinkedHashSet(ApplicationUsers.from(ImmutableList.<User>of(new MockUser("1"))));
        Set<ApplicationUser> originalSuggestedUsers = ImmutableSet.copyOf(suggestedUsers);
        ApplicationUser user1 = Mockito.mock(ApplicationUser.class);
        when(user1.isActive()).thenReturn(true);
        when(userPickerSearchService.userMatches(any(ApplicationUser.class), eqGroup("group1"))).thenReturn(true);
        concreteUserSearchHelper.addToSuggestList(10, suggestedUsers, ImmutableList.of(user1), ImmutableList.<String>of(),
                UserSearchParams.builder().includeInactive(true).filter(new UserFilter(true, null, ImmutableSet.of("group1"))).build());
        assertThat(suggestedUsers.size(), equalTo(originalSuggestedUsers.size() + 1));
        assertThat(suggestedUsers, hasItem(user1));
    }

    @Test
    public void testAddToSuggestList_ByRole()
    {
        LinkedHashSet<ApplicationUser> suggestedUsers = Sets.newLinkedHashSet(ApplicationUsers.from(ImmutableList.<User>of(new MockUser("1"))));
        Set<ApplicationUser> originalSuggestedUsers = ImmutableSet.copyOf(suggestedUsers);
        ApplicationUser user1 = Mockito.mock(ApplicationUser.class);
        when(user1.isActive()).thenReturn(true);
        when(userPickerSearchService.userMatches(any(ApplicationUser.class), eqProjectIdAndRoleId(101L, 1001L))).thenReturn(true);
        concreteUserSearchHelper.addToSuggestList(10, suggestedUsers, ImmutableList.of(user1), ImmutableList.<String>of(),
                UserSearchParams.builder().includeInactive(true).filter(new UserFilter(true, ImmutableSet.of(1001L), null))
                        .filterByProjectIds(ImmutableSet.of(101L)).build());
        assertThat(suggestedUsers.size(), equalTo(originalSuggestedUsers.size()+1));
        assertThat(suggestedUsers, hasItem(user1));
    }

    @Test
    public void testAddToSuggestList_ByGroupAndRole()
    {
        LinkedHashSet<ApplicationUser> suggestedUsers = Sets.newLinkedHashSet(ApplicationUsers.from(ImmutableList.<User>of(new MockUser("1"))));
        Set<ApplicationUser> originalSuggestedUsers = ImmutableSet.copyOf(suggestedUsers);
        ApplicationUser user1 = Mockito.mock(ApplicationUser.class);
        when(user1.isActive()).thenReturn(true);
        when(userPickerSearchService.userMatches(any(ApplicationUser.class), eqGroupAndProjectIdAndRoleId("group1", 101L, 1001L))).thenReturn(true);
        concreteUserSearchHelper.addToSuggestList(10, suggestedUsers, ImmutableList.of(user1), ImmutableList.<String>of(),
                UserSearchParams.builder().includeInactive(true).filter(new UserFilter(true, ImmutableSet.of(1001L), ImmutableSet.of("group1")))
                        .filterByProjectIds(ImmutableSet.of(101L)).build());
        assertThat(suggestedUsers.size(), equalTo(originalSuggestedUsers.size()+1));
        assertThat(suggestedUsers, hasItem(user1));
    }

    @Test
    public void addUserGroupSuggestionParamsReturnsOtherFieldsWhenSearchParamsIsNull() throws Exception
    {
        final ImmutableList<String> selectedUsers = ImmutableList.of();
        final Map<String,Object> params = Maps.newHashMap();
        concreteUserSearchHelper.addUserGroupSuggestionParams(null, selectedUsers, null, params);
        assertThat(params, hasKey("hasPermissionToPickUsers"));
        assertThat(params, not(hasKey("suggestedUsers")));
        assertThat(params, hasKey("suggestedGroups"));
        assertThat(params, hasKey("placeholderText"));
    }

    @Test
    public void addUserGroupSuggestionParamsReturnsAllFieldsWhenSearchParamsIsNotNull() throws Exception
    {
        final ImmutableList<String> selectedUsers = ImmutableList.of();
        final Map<String,Object> params = Maps.newHashMap();
        concreteUserSearchHelper.addUserGroupSuggestionParams(null, selectedUsers, UserSearchParams.ACTIVE_USERS_ALLOW_EMPTY_QUERY, params);
        assertThat(params, hasKey("hasPermissionToPickUsers"));
        assertThat(params, hasKey("suggestedUsers"));
        assertThat(params, hasKey("suggestedGroups"));
        assertThat(params, hasKey("placeholderText"));
    }

    private UserSearchParams emptyGroup()
    {
        return argThat(new TypeSafeMatcher<UserSearchParams>()
        {
            @Override
            protected boolean matchesSafely(final UserSearchParams userSearchParams)
            {
                return userSearchParams.getUserFilter() == null || CollectionUtils.isEmpty(userSearchParams.getUserFilter().getGroups());
            }

            @Override
            public void describeTo(final Description description)
            {
            }
        });
    }

    private UserSearchParams eqGroup(final String... groupNames)
    {
        return argThat(new TypeSafeMatcher<UserSearchParams>()
        {
            @Override
            protected boolean matchesSafely(final UserSearchParams userSearchParams)
            {
                return userSearchParams.getUserFilter().getGroups().equals(ImmutableSet.copyOf(groupNames));
            }

            @Override
            public void describeTo(final Description description)
            {
            }
        });
    }

    private UserSearchParams eqProjectIdAndRoleId(final Long projectId, final Long roleId)
    {
        return argThat(new TypeSafeMatcher<UserSearchParams>()
        {
            @Override
            protected boolean matchesSafely(final UserSearchParams userSearchParams)
            {
                return userSearchParams.getUserFilter().getRoleIds().equals(ImmutableSet.of(roleId))
                        && userSearchParams.getProjectIds().equals(ImmutableSet.of(projectId));
            }

            @Override
            public void describeTo(final Description description)
            {
            }
        });
    }

    private UserSearchParams eqGroupAndProjectIdAndRoleId(final String groupName, final long projectId, final long roleId)
    {
        return argThat(new TypeSafeMatcher<UserSearchParams>()
        {
            @Override
            protected boolean matchesSafely(final UserSearchParams userSearchParams)
            {
                return userSearchParams.getUserFilter().getGroups().equals(ImmutableSet.of(groupName))
                        && userSearchParams.getUserFilter().getRoleIds().equals(ImmutableSet.of(roleId))
                        && userSearchParams.getProjectIds().equals(ImmutableSet.of(projectId));
            }

            @Override
            public void describeTo(final Description description)
            {
            }
        });
    }
}
