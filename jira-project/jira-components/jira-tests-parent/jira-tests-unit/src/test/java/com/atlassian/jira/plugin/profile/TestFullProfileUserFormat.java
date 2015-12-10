package com.atlassian.jira.plugin.profile;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.plugin.userformat.FullProfileUserFormat;
import com.atlassian.jira.plugin.userformat.UserFormatModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @since v3.13
 */
@RunWith (ListeningMockitoRunner.class)
public class TestFullProfileUserFormat
{
    @Mock private UserManager userManager;
    @Mock private UserKeyService userKeyService;
    @Mock private UserFormatModuleDescriptor userFormatModuleDescriptor;
    @Mock private UserPropertyManager userPropertyManager;

    @Test
    public void testGetHtmlNullUser()
    {
        assertGetHtml(null, null, null, null);

        verifyZeroInteractions(userManager, userKeyService);
    }

    @Test
    public void testGetHtmlUnknownUser()
    {
        assertGetHtml("UnknownUser", "UnknownUser", null, null);

        verify(userManager).getUserByKey("UnknownUser");
        verify(userKeyService).getUsernameForKey("UnknownUser");
        verifyNoMoreInteractions(userManager, userKeyService);
    }

    @Test
    public void testGetHtmlDeletedUser()
    {
        when(userKeyService.getUsernameForKey("DeletedKey")).thenReturn("DeletedName");

        assertGetHtml("DeletedKey", "DeletedName", null, null);

        verify(userManager).getUserByKey("DeletedKey");
        verify(userKeyService).getUsernameForKey("DeletedKey");
        verifyNoMoreInteractions(userManager, userKeyService);
    }

    @Test
    public void testGetHtmlKnownUser()
    {
        final ApplicationUser admin = new MockApplicationUser("adminKey", "admin", "Administrator", "admin@example.com");
        when(userManager.getUserByKey(admin.getKey())).thenReturn(admin);

        assertGetHtml(admin, null);

        verify(userManager).getUserByKey(admin.getKey());
        verifyNoMoreInteractions(userManager, userKeyService);
    }

    @Test
    public void testGetHtmlKnownUserWithParams()
    {
        final ApplicationUser admin = new MockApplicationUser("adminKey", "admin", "Administrator", "admin@example.com");
        when(userManager.getUserByKey(admin.getKey())).thenReturn(admin);

        assertGetHtml(admin, EasyMap.build());

        verify(userManager).getUserByKey(admin.getKey());
        verifyNoMoreInteractions(userManager, userKeyService);
    }



    private void assertGetHtml(final ApplicationUser expectedUser, final Map params)
    {
        assertGetHtml(expectedUser.getKey(), expectedUser.getUsername(), expectedUser, params);
    }

    private void assertGetHtml(final String key, final String expectedUsername, final ApplicationUser expectedUser, final Map params)
    {
        final FullProfileUserFormat userFormat = new FullProfileUserFormat(null, null, null, null, null, userManager, userKeyService, userFormatModuleDescriptor, userPropertyManager, null);
        final String resourceName = "view";
        final Map<String, ?> startingParams = EasyMap.build(
                "username", expectedUsername,
                "user", ApplicationUsers.toDirectoryUser(expectedUser),
                "action", userFormat,
                "navWebFragment", null,
                "id", "testid");

        when(userFormatModuleDescriptor.getHtml(resourceName, startingParams)).thenReturn("PASS");

        final String html;
        if (params == null)
        {
            html = userFormat.format(key, "testid");
        }
        else
        {
            html = userFormat.format(key, "testid", params);
        }

        // Do this first, since it's likely to be more informative than the assertEquals
        verify(userFormatModuleDescriptor).getHtml(resourceName, startingParams);
        verifyNoMoreInteractions(userFormatModuleDescriptor);
        assertEquals("PASS", html);
    }
}
