package com.atlassian.jira.plugin.profile;

import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.plugin.userformat.FullNameUserFormat;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestFullNameUserFormat
{
    @Test
    public void testUserNameIsBlank()
    {
        final FullNameUserFormat userFormatterUtil = new FullNameUserFormat(null, null);

        assertNull(userFormatterUtil.format(null, "someid"));
        assertNull(userFormatterUtil.format("", "someid"));
        assertNull(userFormatterUtil.format("    ", "someid"));
    }

    @Test
    public void testRegularCase()
    {
        final ApplicationUser dude = new MockApplicationUser("dude");

        final UserManager userManager = mock(UserManager.class);
        when(userManager.getUserByKeyEvenWhenUnknown("dude")).thenReturn(dude);

        final UserUtil userUtil = mock(UserUtil.class);
        when(userUtil.getDisplayableNameSafely(dude)).thenReturn("Mr Mock");

        final FullNameUserFormat userFormatterUtil = new FullNameUserFormat(userManager, userUtil);
        assertEquals("Mr Mock", userFormatterUtil.format("dude", "fullname"));
    }

    @Test
    public void testHtmlEncoding()
    {
        final ApplicationUser dude = new MockApplicationUser("Dude", "dude", "Mr Mock<script>alert('owned')</script>", "dude@example.com");

        final UserManager userManager = mock(UserManager.class);
        when(userManager.getUserByKeyEvenWhenUnknown("Dude")).thenReturn(dude);

        final UserUtil userUtil = mock(UserUtil.class);
        when(userUtil.getDisplayableNameSafely(dude)).thenReturn(dude.getDisplayName());

        final FullNameUserFormat userFormatterUtil = new FullNameUserFormat(userManager, userUtil);
        assertEquals("Mr Mock&lt;script&gt;alert(&#39;owned&#39;)&lt;/script&gt;", userFormatterUtil.format("Dude", "fullname"));
    }

    @Test
    public void testUserNameDoesntExist()
    {
        final ApplicationUser unknown = new DelegatingApplicationUser("TheKey", new ImmutableUser(-1, "TheUserName", "TheFullName", "?", false));
        final UserManager userManager = mock(UserManager.class);
        when(userManager.getUserByKeyEvenWhenUnknown("TheKey")).thenReturn(unknown);

        final UserUtil userUtil = mock(UserUtil.class);
        when(userUtil.getDisplayableNameSafely(unknown)).thenReturn(unknown.getDisplayName());

        final FullNameUserFormat userFormatterUtil = new FullNameUserFormat(userManager, userUtil);
        assertEquals("TheFullName", userFormatterUtil.format("TheKey", "someid"));
    }
}
