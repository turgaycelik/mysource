package com.atlassian.jira.plugin.profile;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.plugin.userformat.ProfileLinkUserFormat;
import com.atlassian.jira.plugin.userformat.UserFormatModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.user.util.UserUtilImpl;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.soy.renderer.SoyTemplateRenderer;

import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @since v3.13
 */
@RunWith (MockitoJUnitRunner.class)
public class TestProfileLinkUserFormat
{
    final static String AVATAR_URL = "http://localhost/jira/avatar_known";
    final static String AVATAR_URL_ANONYMOUS = "http://localhost/jira/avatar_unknown";

    @Mock Avatar avatar;
    @Mock AvatarService avatarService;
    @Mock I18nHelper i18nHelper;
    @Mock JiraAuthenticationContext jiraAuthenticationContext;
    ApplicationUser user;
    @Mock UserFormatModuleDescriptor userFormatModuleDescriptor;
    @Mock UserKeyService userKeyService;
    @Mock UserManager userManager;
    UserUtil userUtil;

    @Before
    public void setUp()
    {
        when(avatar.getId()).thenReturn(10023L);
        when(avatarService.getAvatar(any(User.class), any(String.class))).thenReturn(avatar);
        when(avatarService.getAvatarURL(any(ApplicationUser.class), any(ApplicationUser.class), any(Avatar.Size.class))).thenReturn(URI.create(AVATAR_URL));
        when(avatarService.getAvatarURL(any(ApplicationUser.class), isNull(ApplicationUser.class), any(Avatar.Size.class))).thenReturn(URI.create(AVATAR_URL_ANONYMOUS));
        when(i18nHelper.getText(anyString())).thenReturn("Anonymous");

        user = new MockApplicationUser("userKey", "username", "User Name", "username@example.com");
        userUtil = new UserUtilImpl(null, null, null, null, null, null, null, null, null,
                null, null, null, null, userManager, null, null, new MemoryCacheManager());

        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        when(jiraAuthenticationContext.getUser()).thenReturn(user);
        when(userKeyService.getUsernameForKey("userKey")).thenReturn("username");
        when(userManager.getUserByKey("userKey")).thenReturn(user);
    }

    @Test
    public void testGetHTMLNullUser()
    {
        when(userFormatModuleDescriptor.getHtml("view", new TestDataBuilder().url(AVATAR_URL_ANONYMOUS).user(null)
                .username(null)
                .fullname(null)
                .build())).thenReturn("HTML");

        ProfileLinkUserFormat userFormat = newProfileLinkUserFormat(avatarService, i18nHelper, jiraAuthenticationContext, userFormatModuleDescriptor, null, null);
        assertThat(userFormat.format(null, "id"), is("HTML"));
    }

    @Test
    public void testGetHTMLUnknownUser()
    {
        // userManager.getUserByKey() will return null, thus an unknown user.
        when(userKeyService.getUsernameForKey("unknownUserKey")).thenReturn("unknownusername");
        when(userFormatModuleDescriptor.getHtml("view", new TestDataBuilder().url(AVATAR_URL_ANONYMOUS).user(null)
                .username("unknownusername")
                .fullname("unknownusername")
                .build())).thenReturn("Who?!");

        ProfileLinkUserFormat userFormat = newProfileLinkUserFormat(avatarService, i18nHelper, jiraAuthenticationContext, userFormatModuleDescriptor, userKeyService, userUtil);
        assertThat(userFormat.format("unknownUserKey", "id"), is("Who?!"));

        verify(userManager).getUserByKey("unknownUserKey");
        verify(userKeyService).getUsernameForKey("unknownUserKey");
        verifyNoMoreInteractions(userManager, userKeyService);
    }

    @Test
    public void testGetHTMLKnownUserWithFullName()
    {
        when(userFormatModuleDescriptor.getHtml("view", new TestDataBuilder().url(AVATAR_URL).user(user).build())).thenReturn("HTML");

        ProfileLinkUserFormat userFormat = newProfileLinkUserFormat(avatarService, i18nHelper, jiraAuthenticationContext, userFormatModuleDescriptor, null, userUtil);
        assertThat(userFormat.format("userKey", "id"), is("HTML"));

        verify(userManager).getUserByKey("userKey");
        verifyNoMoreInteractions(userManager, userKeyService);
    }

    @Test
    public void testGetHTMLWithParams()
    {
        when(userFormatModuleDescriptor.getHtml("view", new TestDataBuilder().url(AVATAR_URL).user(user).build())).thenReturn("HTML");

        ProfileLinkUserFormat userFormat = newProfileLinkUserFormat(avatarService, i18nHelper, jiraAuthenticationContext, userFormatModuleDescriptor, null, userUtil);
        assertThat(userFormat.format("userKey", "id", Maps.<String, Object>newHashMap()), is("HTML"));

        verify(userManager).getUserByKey("userKey");
        verifyNoMoreInteractions(userManager, userKeyService);
    }

    @Test
    public void testGetHTMLKnownUserWithNoFullName()
    {
        user = new MockApplicationUser("userKey", "username", "", "username@example.com");
        when(userManager.getUserByKey("userKey")).thenReturn(user);
        when(userFormatModuleDescriptor.getHtml("view", new TestDataBuilder().url(AVATAR_URL).user(user)
                .fullname("username")
                .build())).thenReturn("HTML");

        ProfileLinkUserFormat userFormat = newProfileLinkUserFormat(avatarService, i18nHelper, jiraAuthenticationContext, userFormatModuleDescriptor, userKeyService, userUtil);
        assertThat(userFormat.format("userKey", "id"), is("HTML"));

        verify(userManager).getUserByKey("userKey");
        verifyNoMoreInteractions(userManager, userKeyService);
    }

    private ProfileLinkUserFormat newProfileLinkUserFormat(final AvatarService avatarService,
            final I18nHelper i18nHelper,
            final JiraAuthenticationContext jiraAuthenticationContext,
            final UserFormatModuleDescriptor moduleDescriptor,
            final UserKeyService userKeyService,
            final UserUtil userUtil)
    {
        return new ProfileLinkUserFormat(avatarService, i18nHelper, jiraAuthenticationContext, moduleDescriptor, userKeyService, userUtil)
        {
            @Override
            public SoyTemplateRenderer getSoyRenderer()
            {
                return null;
            }
        };
    }

    private static class TestDataBuilder
    {
        final Map<String, Object> map;

        public TestDataBuilder()
        {
            map = MapBuilder.<String, Object>newBuilder()
                    .add("avatarSize", Avatar.Size.SMALL.getParam())
                    .add("defaultFullName", "Anonymous")
                    .add("fullname", "User Name")
                    .add("id", "id")
                    .add("username", "username")
                    .add("soyRenderer", null).toHashMap();
        }

        public TestDataBuilder user(ApplicationUser user)
        {
            map.put("user", user);
            return this;
        }

        public TestDataBuilder url(String url)
        {
            map.put("avatarURL", url);
            return this;
        }

        public TestDataBuilder fullname(String fullname)
        {
            map.put("fullname", fullname);
            return this;
        }

        public TestDataBuilder username(String username)
        {
            map.put("username", username);
            return this;
        }

        public Map<String, Object> build()
        {
            return new HashMap<String, Object>(map);
        }
    }
}