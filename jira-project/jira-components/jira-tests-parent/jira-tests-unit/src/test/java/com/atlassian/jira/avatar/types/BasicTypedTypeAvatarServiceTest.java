package com.atlassian.jira.avatar.types;

import java.util.NoSuchElementException;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockAvatar;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BasicTypedTypeAvatarServiceTest
{
    public static final long AVATAR_ID = 23423;
    public static final long NON_EXISTING_AVATAR_ID = 363l;
    private final MockAvatar mockAvatar = new MockAvatar(AVATAR_ID, "asdf", "a", Avatar.Type.ISSUETYPE, "issue1", true);
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    AvatarManager avatarManager;

    @Mock
    AvatarAccessPolicy accessPolicy;

    BasicTypedTypeAvatarService typedAvatars;
    private ApplicationUser admin = new MockApplicationUser("admin");

    @Before
    public void setUp() throws Exception
    {
        when(avatarManager.getById(AVATAR_ID)).
                thenReturn(mockAvatar);
        when(avatarManager.getById(NON_EXISTING_AVATAR_ID)).
                thenReturn(null);

        typedAvatars = new BasicTypedTypeAvatarService(Avatar.Type.ISSUETYPE, avatarManager, accessPolicy);
    }

    @Test
    public void testReturnsGivenAvatarWhenThereArePermissions() throws Exception
    {
        when(accessPolicy.userCanViewAvatar(admin, mockAvatar)).thenReturn(true);

        final Avatar avatar = typedAvatars.getAvatar(admin, AVATAR_ID);

        assertThat(avatar, is((Avatar) mockAvatar));
    }

    @Test
    public void testDoesntReturnAvatarWhenThereAreNoPermissions() throws Exception
    {
        when(accessPolicy.userCanViewAvatar(admin, mockAvatar)).thenReturn(false);

        final Avatar avatar = typedAvatars.getAvatar(admin, AVATAR_ID);

        assertThat(avatar, is(nullValue()));
    }

    @Test
    public void shouldReturnNullAvatarWhenNullIsPassedAndShouldNotUserPolicy() throws Exception
    {
        final Avatar avatar = typedAvatars.getAvatar(admin, NON_EXISTING_AVATAR_ID);

        assertThat(avatar, is(nullValue()));
        verify(accessPolicy, never()).userCanViewAvatar(any(ApplicationUser.class), any(Avatar.class));
    }

    @Test
    public void shouldReturnDefaultAvatar() throws Exception {
        // given
        final long defaultAvatarId = 3467L;
        when(avatarManager.getDefaultAvatarId(Avatar.Type.ISSUETYPE)).thenReturn(defaultAvatarId);
        when(avatarManager.getById(defaultAvatarId)).thenReturn(mockAvatar);

        // when
        final Avatar avatar = typedAvatars.getDefaultAvatar();

        // then
        assertThat(avatar, is((Avatar) mockAvatar));
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowNoSuchElementExceptionWhenThereIsNoDefaultAvatar() throws Exception {
        // given
        when(avatarManager.getDefaultAvatarId(Avatar.Type.ISSUETYPE)).thenReturn(null);

        // when
        typedAvatars.getDefaultAvatar();

        // expect exception
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowNoSuchElementExceptionWhenDefaultAvatarDoesNotExist() throws Exception {
        // given
        final long defaultAvatarId = 3467L;
        when(avatarManager.getDefaultAvatarId(Avatar.Type.ISSUETYPE)).thenReturn(defaultAvatarId);
        when(avatarManager.getById(defaultAvatarId)).thenReturn(null);

        // when
        typedAvatars.getDefaultAvatar();

        // expect exception
    }
}
