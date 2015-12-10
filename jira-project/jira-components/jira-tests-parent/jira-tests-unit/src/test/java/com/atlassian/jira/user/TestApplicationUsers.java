package com.atlassian.jira.user;

import java.util.Collection;
import java.util.Collections;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @since v5.2.1
 */
public class TestApplicationUsers
{
    @Rule
    public TestRule testRule = new MockComponentContainer(this);

    @Test
    public void testNullSafety()
    {
        final Collection<User> hasNullUsers = Collections.singletonList((User)null);
        final Collection<ApplicationUser> hasNullApplicationUsers = Collections.singletonList((ApplicationUser)null);

        assertNull("getKeyFor(User)", ApplicationUsers.getKeyFor((User)null));
        assertNull("getKeyFor(ApplicationUser)", ApplicationUsers.getKeyFor((ApplicationUser)null));
        assertNull("from(User)", ApplicationUsers.from((User)null));
        assertNull("from(Collection<User> that is null)", ApplicationUsers.from((Collection<User>)null));
        assertEquals("from(Collection<User> containing a null)", hasNullApplicationUsers, ApplicationUsers.from(hasNullUsers));
        assertNull("toDirectoryUser(ApplicationUser)", ApplicationUsers.toDirectoryUser(null));
        assertNull("toDirectoryUsers(Collection<ApplicationUser> that is null)", ApplicationUsers.toDirectoryUser(null));
        assertEquals("toDirectoryUsers(Collection<ApplicationUser> containing a null)", hasNullUsers, ApplicationUsers.toDirectoryUsers(hasNullApplicationUsers));
        assertNull("byKey(String)", ApplicationUsers.byKey(null));
    }

    @Test
    public void testFromUsingBridge() throws Exception
    {
        final User user = new MockUser("def", "Fred Smith", "fred@example.com");
        final ApplicationUser appUser = new DelegatingApplicationUser("key", user);

        // The bridged user is not necessarily == the original
        final User bridged = appUser.getDirectoryUser();
        assertTrue("Should be a BridgedDirectoryUser", bridged instanceof BridgedDirectoryUser);
        assertEquals(bridged, user);

        // But thereafter, the interconversion should be stable to ensure that we
        // are reusing the existing objects.
        assertSame(appUser, ApplicationUsers.from(bridged));
        assertSame(bridged, ApplicationUsers.toDirectoryUser(appUser));
    }

    @Test
    public void testFromUsingBridgeEvenWhenUnknown() throws Exception
    {
        assertEquals(null, ApplicationUsers.from((User)null));

        final User user = new ImmutableUser(-1, "Nobody", "Nobody", "?", false);
        final ApplicationUser appUser = new DelegatingApplicationUser("Key", user);

        // The bridged user is not necessarily == the original
        final User bridged = appUser.getDirectoryUser();
        assertTrue("Should be a BridgedDirectoryUser", bridged instanceof BridgedDirectoryUser);
        assertEquals(bridged, user);

        // But thereafter, the interconversion should be stable to ensure that we
        // are reusing the existing objects.
        assertSame(appUser, ApplicationUsers.from(bridged));
        assertSame(bridged, ApplicationUsers.toDirectoryUser(appUser));
    }

    @Test
    public void testFromUsingKeyService() throws Exception
    {
        final MockUserKeyService userKeyService = new MockUserKeyService();
        userKeyService.setMapping("key", "def");
        new MockComponentWorker().addMock(UserKeyService.class, userKeyService).init();

        final User user = new MockUser("def", "Fred Smith", "fred@example.com");
        final ApplicationUser appUser = ApplicationUsers.from(user);
        assertEquals("key", appUser.getKey());

        // The bridged user is not necessarily == the original
        final User bridged = appUser.getDirectoryUser();
        assertTrue("Should be a BridgedDirectoryUser", bridged instanceof BridgedDirectoryUser);
        assertEquals(bridged, user);

        // But thereafter, the interconversion should be stable to ensure that we
        // are reusing the existing objects.
        assertSame(appUser, ApplicationUsers.from(bridged));
        assertSame(bridged, ApplicationUsers.toDirectoryUser(appUser));
    }

    @Test(expected = IllegalStateException.class)
    public void testFromUsingKeyServiceWithUnmappedUser() throws Exception
    {
        final UserKeyService userKeyService = mock(UserKeyService.class);
        new MockComponentWorker().addMock(UserKeyService.class, userKeyService).init();
        final User user = new MockUser("def", "Fred Smith", "fred@example.com");
        ApplicationUsers.from(user);
    }

    @Test
    public void testFromUsingKeyServiceAndSpecialUnknownUserDirectory() throws Exception
    {
        final UserKeyService userKeyService = mock(UserKeyService.class);  // Will return null
        new MockComponentWorker().addMock(UserKeyService.class, userKeyService).init();

        final User user = new ImmutableUser(-1, "Nobody", "Nobody", "?", false);
        final ApplicationUser appUser = ApplicationUsers.from(user);
        assertEquals("Nobody", appUser.getKey());

        // The bridged user is not necessarily == the original
        final User bridged = appUser.getDirectoryUser();
        assertTrue("Should be a BridgedDirectoryUser", bridged instanceof BridgedDirectoryUser);
        assertEquals(bridged, user);

        // But thereafter, the interconversion should be stable to ensure that we
        // are reusing the existing objects.
        assertSame(appUser, ApplicationUsers.from(bridged));
        assertSame(bridged, ApplicationUsers.toDirectoryUser(appUser));
    }


    @Test
    public void testFromWorksForEditedUser() throws Exception
    {
        final User user = new ImmutableUser(1, "def", "Fred Smith", "fred@example.com", true);
        final MockUserManager mockUserManager = new MockUserManager();
        mockUserManager.addUser(new ImmutableUser(1, "def", "Freddo Smith", "freddo@example.com", true));
        new MockComponentWorker().addMock(UserManager.class, mockUserManager).init();

        final ApplicationUser applicationUser = ApplicationUsers.from(user);
        assertEquals("def", applicationUser.getKey());
        assertEquals("def", applicationUser.getUsername());
        assertEquals("Fred Smith", applicationUser.getDisplayName());
        assertEquals("fred@example.com", applicationUser.getEmailAddress());
    }

    @Test
    public void testGetKeyFor() throws Exception
    {
        ApplicationUser applicationUser = new DelegatingApplicationUser("abc", new MockUser("def", "Fred Smith", "fred@example.com"));
        assertEquals("abc", ApplicationUsers.getKeyFor(applicationUser));
        assertEquals("abc", ApplicationUsers.getKeyFor(applicationUser.getDirectoryUser()));
        assertEquals("fred", ApplicationUsers.getKeyFor(new MockUser("Fred", "Fred Smith", "fred@example.com")));
    }

    @Test
    public void testGetKeyForWorksForEditedUser() throws Exception
    {
        final User user = new ImmutableUser(1, "def", "Fred Smith", "fred@example.com", true);
        final MockUserManager mockUserManager = new MockUserManager();
        mockUserManager.addUser(new ImmutableUser(1, "def", "Freddo Smith", "freddo@example.com", true));
        new MockComponentWorker().addMock(UserManager.class, mockUserManager).init();

        final ApplicationUser applicationUser = ApplicationUsers.from(user);
        assertEquals("def", applicationUser.getKey());
    }
}
