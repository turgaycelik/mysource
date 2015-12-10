package com.atlassian.jira.plugin.profile;

import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.plugin.userformat.UserNameUserFormat;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v6.0
 */
public class TestUserNameUserFormat
{
    @Test
    public void testUserNameIsBlank()
    {
        final UserNameUserFormat userFormatterUtil = new UserNameUserFormat(null);

        assertNull(userFormatterUtil.format(null, "someid"));
        assertNull(userFormatterUtil.format("", "someid"));
        assertNull(userFormatterUtil.format("    ", "someid"));
    }

    @Test
    public void testRegularCase()
    {
        final ApplicationUser dude = new MockApplicationUser("DudeKey", "DudeUserName", "Dude Full Name", "dude@example.com");

        final UserManager userManager = mock(UserManager.class);
        when(userManager.getUserByKeyEvenWhenUnknown("DudeKey")).thenReturn(dude);

        final UserNameUserFormat userFormatterUtil = new UserNameUserFormat(userManager);
        assertEquals("DudeUserName", userFormatterUtil.format("DudeKey", "username"));
    }

    @Test
    public void testHtmlEncoding()
    {
        final ApplicationUser dude = new MockApplicationUser("Dude O'Key", "Dude O'User", "Dude O'User Full Name", "dude@example.com");

        final UserManager userManager = mock(UserManager.class);
        when(userManager.getUserByKeyEvenWhenUnknown("Dude O'Key")).thenReturn(dude);

        final UserNameUserFormat userFormatterUtil = new UserNameUserFormat(userManager);
        assertEquals("Dude O&#39;User", userFormatterUtil.format("Dude O'Key", "username"));
    }

    @Test
    public void testUserNameDoesntExist()
    {
        final ApplicationUser unknown = new DelegatingApplicationUser("TheKey", new ImmutableUser(-1, "TheUserName", "TheFullName", "?", false));
        final UserManager userManager = mock(UserManager.class);
        when(userManager.getUserByKeyEvenWhenUnknown("TheKey")).thenReturn(unknown);

        final UserNameUserFormat userFormatterUtil = new UserNameUserFormat(userManager);
        assertEquals("TheUserName", userFormatterUtil.format("TheKey", "someid"));
    }
}
