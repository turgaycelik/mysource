package com.atlassian.jira.rest.v2.issue;

import java.net.URI;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarPickerHelper;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockAvatar;
import com.atlassian.jira.rest.util.AttachmentHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static com.atlassian.jira.avatar.Avatar.Size;
import static com.atlassian.jira.avatar.Avatar.Size.LARGE;
import static com.atlassian.jira.avatar.Avatar.Size.MEDIUM;
import static com.atlassian.jira.avatar.Avatar.Size.NORMAL;
import static com.atlassian.jira.avatar.Avatar.Size.SMALL;
import static com.atlassian.jira.avatar.Avatar.Type.USER;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class AvatarResourceHelperTest
{
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    ApplicationUser fredo = new MockApplicationUser("fredo");

    ApplicationUser admin = new MockApplicationUser("admin");

    Avatar adminAvatar = new MockAvatar(1, "admin.jpg", "jpg", USER, "admin", false);

    Avatar fredoAvatar = new MockAvatar(2, "fredo.png", "png", USER, "fredo", false);

    @Mock
    JiraAuthenticationContext authContext;

    @Mock
    AvatarManager avatarManager;

    @Mock
    AvatarPickerHelper avatarPickerHelper;

    @Mock
    AttachmentHelper attachmentHelper;

    @Mock
    UserManager userManager;

    @Mock
    @AvailableInContainer
    AvatarService avatarService;

    @InjectMocks
    AvatarResourceHelper avatarResourceHelper;

    @Before
    public void setUp() throws Exception
    {
        when(authContext.getUser()).thenReturn(admin);

        when(avatarManager.getAllSystemAvatars(USER)).thenReturn(ImmutableList.<Avatar>of());
        when(avatarManager.getCustomAvatarsForOwner(USER, admin.getUsername())).thenReturn(ImmutableList.of(adminAvatar));
        when(avatarManager.getCustomAvatarsForOwner(USER, fredo.getUsername())).thenReturn(ImmutableList.of(fredoAvatar));
        when(avatarService.getAvatarUrlNoPermCheck(any(ApplicationUser.class), any(Avatar.class), any(Size.class))).thenAnswer(new UriFromFilename());

        when(userManager.getUserByKey(fredo.getKey())).thenReturn(fredo);
        when(userManager.getUserByKey(admin.getKey())).thenReturn(admin);
    }

    @Test
    public void getAllAvatarsReturnsAvatarsForUser() throws Exception
    {
        final Map<String, URI> fredoAvatarUrls = ImmutableMap.of(
                "16x16", createUriFor(fredoAvatar, SMALL),
                "24x24", createUriFor(fredoAvatar, NORMAL),
                "32x32", createUriFor(fredoAvatar, MEDIUM),
                "48x48", createUriFor(fredoAvatar, LARGE)
        );

        final Map<String, List<AvatarBean>> avatarBeans = ImmutableMap.<String, List<AvatarBean>>of(
                "system", ImmutableList.<AvatarBean>of(),
                "custom", ImmutableList.of(new AvatarBean(String.valueOf(fredoAvatar.getId()), fredoAvatar.getOwner(), fredoAvatar.isSystemAvatar(), fredoAvatarUrls))
        );

        assertThat("should return all of fredo's avatars", avatarResourceHelper.getAllAvatars(USER, fredo.getUsername(), 10000L), equalTo(avatarBeans));
    }

    private static URI createUriFor(final Avatar avatar, final Size size)
    {
        return URI.create(String.format("http://%s/%s", avatar.getFileName(), size.getPixels()));
    }

    /**
     * Return a URI based on the Avatar filename and requested size.
     */
    private static class UriFromFilename implements Answer<URI>
    {
        @Override
        public URI answer(final InvocationOnMock invocation) throws Throwable
        {
            Avatar avatar = (Avatar) invocation.getArguments()[1];
            Size size = (Size) invocation.getArguments()[2];

            return createUriFor(avatar, size);
        }
    }
}
