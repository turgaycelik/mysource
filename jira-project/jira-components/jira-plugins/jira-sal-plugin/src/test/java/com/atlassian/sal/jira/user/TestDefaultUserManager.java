package com.atlassian.sal.jira.user;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.Query;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.search.query.entity.GroupQuery;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestDefaultUserManager
{
    @Mock
    private UserManager userManager;
    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock
    private CrowdService crowdService;
    @Mock
    private GlobalPermissionManager globalPermissionManager;
    @Mock
    private AvatarService avatarService;

    private DefaultUserManager defaultUserManager;

    @Before
    public void setUp() throws Exception
    {
        defaultUserManager = new DefaultUserManager(globalPermissionManager, jiraAuthenticationContext, crowdService, avatarService, userManager);
    }

    @Test
    public void testGetRemoteUsername()
    {
        ApplicationUser mockUser = new MockApplicationUser("tommy");

        when(jiraAuthenticationContext.getUser()).thenReturn(null).thenReturn(mockUser);

        String username = defaultUserManager.getRemoteUsername();
        assertNull(username);

        username = defaultUserManager.getRemoteUsername();
        assertEquals("tommy", username);

        verify(jiraAuthenticationContext, times(2)).getUser();
    }

    @Test
    public void testIsSystemAdminNoUser()
    {
        when(userManager.getUserByName("tommy")).thenReturn(null);


        boolean systemAdmin = defaultUserManager.isSystemAdmin("tommy");
        assertFalse(systemAdmin);

        verify(userManager).getUserByName("tommy");
    }

    @Test
    public void testIsSystemAdminNoPermissions()
    {
        final ApplicationUser mockUser = new MockApplicationUser("tommy");

        when(globalPermissionManager.hasPermission(44, mockUser)).thenReturn(false);

        when(userManager.getUserByName("tommy")).thenReturn(mockUser);

        boolean systemAdmin = defaultUserManager.isSystemAdmin("tommy");
        assertFalse(systemAdmin);

        verify(userManager).getUserByName("tommy");
        verify(globalPermissionManager).hasPermission(44, mockUser);
    }

    @Test
    public void testIsSystemAdmin()
    {
        final ApplicationUser mockUser = new MockApplicationUser("tommy");

        when(globalPermissionManager.hasPermission(44, mockUser)).thenReturn(true);

        when(userManager.getUserByName("tommy")).thenReturn(mockUser);

        boolean systemAdmin = defaultUserManager.isSystemAdmin("tommy");
        assertTrue(systemAdmin);

        verify(userManager).getUserByName("tommy");
        verify(globalPermissionManager).hasPermission(44, mockUser);
    }

    @Test
    public void testGetRemoteUserRequest()
    {
        final ApplicationUser mockUser = new MockApplicationUser("tommy");

        when(jiraAuthenticationContext.getUser()).thenReturn(mockUser);

        final HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);

        final String remoteUsername = defaultUserManager.getRemoteUsername(mockHttpServletRequest);
        assertEquals("tommy", remoteUsername);

        verify(jiraAuthenticationContext).getUser();
    }

    @Test
    public void testGetRemoteUserRequestNoUser()
    {
        when(jiraAuthenticationContext.getUser()).thenReturn(null);

        final HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);

        final String remoteUsername = defaultUserManager.getRemoteUsername(mockHttpServletRequest);
        assertNull(remoteUsername);

        verify(jiraAuthenticationContext).getUser();
    }

    @Test
    public void testUserProfile_userdoesntexist() throws Exception
    {
        when(crowdService.getUser("tommy")).thenReturn(null);

        final UserProfile profile = defaultUserManager.getUserProfile("tommy");
        assertNull(profile);
    }

    @Test
    public void testUserProfile() throws Exception
    {
        final MockApplicationUser mockUser = new MockApplicationUser("tommy", "Tommy Golightly", "tommy@example.com");

        when(userManager.getUserByName("tommy")).thenReturn(mockUser);

        final UserProfile profile = defaultUserManager.getUserProfile("tommy");
        assertEquals("tommy@example.com", profile.getEmail());
        assertEquals("tommy", profile.getUsername());
        assertEquals("Tommy Golightly", profile.getFullName());
        assertEquals("/secure/ViewProfile.jspa?name=tommy", profile.getProfilePageUri().toString());
    }

    @Test
    public void testUserProfile_crazyName() throws Exception
    {
        final String username = "=?&!; #";
        final ApplicationUser mockUser = new MockApplicationUser(username);
        when(userManager.getUserByName(username)).thenReturn(mockUser);

        final UserProfile profile = defaultUserManager.getUserProfile(username);
        assertEquals(username, profile.getUsername());
        assertEquals("/secure/ViewProfile.jspa?name=%3D%3F%26%21%3B+%23", profile.getProfilePageUri().toString());
    }

    @Test
    public void testUserProfile_noAvatar() throws Exception
    {
        final ApplicationUser mockUser = new MockApplicationUser("tommy");

        when(userManager.getUserByName("tommy")).thenReturn(mockUser);

        final ApplicationUser remoteUser = mock(ApplicationUser.class);

        when(jiraAuthenticationContext.getUser()).thenReturn(remoteUser);

        when(avatarService.getAvatarURL(remoteUser, mockUser, Avatar.Size.LARGE)).thenReturn(null);

        final UserProfile profile = defaultUserManager.getUserProfile("tommy");

        final URI picture = profile.getProfilePictureUri();
        assertNull(picture);
    }

    @Test
    public void testUserProfile_avatar() throws Exception
    {
        final ApplicationUser mockUser = new MockApplicationUser("tommy");

        when(userManager.getUserByName("tommy")).thenReturn(mockUser);

        when(jiraAuthenticationContext.getUser()).thenReturn(mockUser);

        final URI avatarUri = new URI("http://example.invalid/secure/useravatar?avatarId=2000");

        when(avatarService.getAvatarURL(mockUser, mockUser, Avatar.Size.LARGE)).thenReturn(avatarUri);
//        when(avatarService.hasCustomUserAvatar(mockUser, "tommy")).thenReturn(false);

        final UserProfile profile = defaultUserManager.getUserProfile("tommy");

        final URI picture = profile.getProfilePictureUri();
        assertEquals(avatarUri, picture);
    }

    @Test
    public void testUserProfile_avatarServiceGetsLoggedInUser()
    {
        final ApplicationUser avatarUser = new MockApplicationUser("tommy");

        when(userManager.getUserByName("tommy")).thenReturn(avatarUser);


        final ApplicationUser remoteUser = mock(ApplicationUser.class);

        when(jiraAuthenticationContext.getUser()).thenReturn(remoteUser);

        defaultUserManager.getUserProfile("tommy").getProfilePictureUri(48, 48);
        verify(avatarService).getAvatarURL(remoteUser, avatarUser, Avatar.Size.LARGE);
    }

    @Test
    public void testAuthenticate_goodUser()
            throws FailedAuthenticationException
    {
        final UserTemplate mockUser = new UserTemplate("user1");

        when(crowdService.authenticate("user1", "password1")).thenReturn(mockUser);

        assertTrue(defaultUserManager.authenticate("user1", "password1"));

        verify(crowdService).authenticate("user1", "password1");
    }

    @Test
    public void testAuthenticate_nonExistingUser()
            throws FailedAuthenticationException
    {
        when(crowdService.authenticate("user1", "password1")).thenThrow(new FailedAuthenticationException("username/password is incorrect"));

        assertFalse(defaultUserManager.authenticate("user1", "password1"));

        verify(crowdService).authenticate("user1", "password1");
    }

    @Test
    public void testIsUserInGroup_goodUser()
    {
        final User mockUser = mock(User.class);
        final Group mockGroup = mock(Group.class);

        when(crowdService.getUser("user1")).thenReturn(mockUser);
        when(crowdService.getGroup("group1")).thenReturn(mockGroup);
        when(crowdService.isUserMemberOfGroup(mockUser, mockGroup)).thenReturn(true);

        assertTrue(defaultUserManager.isUserInGroup("user1", "group1"));

        verify(crowdService).getUser("user1");
        verify(crowdService).getGroup("group1");
        verify(crowdService).isUserMemberOfGroup(mockUser, mockGroup);
    }

    @Test
    public void testIsUserInGroup_nonExistingUser()
    {
        final Group mockGroup = mock(Group.class);

        when(crowdService.getUser("user1")).thenReturn(null);
        when(crowdService.getGroup("group1")).thenReturn(mockGroup);

        assertFalse(defaultUserManager.isUserInGroup("user1", "group1"));

        verify(crowdService).getUser("user1");
        verify(crowdService).getGroup("group1");
    }

    @Test
    public void testIsUserInGroup_nonExistingGroup()
    {
        final User mockUser = mock(User.class);

        when(crowdService.getUser("user1")).thenReturn(mockUser);
        when(crowdService.getGroup("group1")).thenReturn(null);

        assertFalse(defaultUserManager.isUserInGroup("user1", "group1"));

        verify(crowdService).getUser("user1");
        verify(crowdService).getGroup("group1");
    }

    @Test
    public void testfindGroupNamesByPrefixQuery()
    {
        String prefix = "g";
        int startIndex = 0;
        int maxResults = 100;

        when(crowdService.search(any(Query.class))).thenReturn(Lists.newArrayList("group", "group2"));

        assertThat(defaultUserManager.findGroupNamesByPrefix(prefix, startIndex, maxResults), hasItems("group", "group2"));

        ArgumentCaptor argument = ArgumentCaptor.forClass(GroupQuery.class);
        verify(crowdService).search((Query<Object>) argument.capture());
        Query<String> actualQuery = (Query<String>) argument.getValue();

        assertEquals(prefix, ((TermRestriction)actualQuery.getSearchRestriction()).getValue());
        assertEquals(startIndex, actualQuery.getStartIndex());
        assertEquals(maxResults, actualQuery.getMaxResults());
    }
}
