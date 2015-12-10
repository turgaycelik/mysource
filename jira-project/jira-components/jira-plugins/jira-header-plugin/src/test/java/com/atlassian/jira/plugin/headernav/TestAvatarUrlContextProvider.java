package com.atlassian.jira.plugin.headernav;

import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.avatar.AvatarsDisabledException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Map;

import static com.atlassian.jira.avatar.Avatar.Size;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class TestAvatarUrlContextProvider
{
    private static final String CONTEXT_KEY = AvatarUrlContextProvider.AVATAR_URL_KEY;
    private static final Size AVATAR_SIZE = AvatarUrlContextProvider.AVATAR_SIZE;

    private static final String AVATAR_URL = "http://example.com/my/avatar";

    private final ApplicationUser user = mock(ApplicationUser.class);
    private final AvatarService avatarServiceMock = mock(AvatarService.class);
    private final JiraAuthenticationContext authenticationContext = mock(JiraAuthenticationContext.class);
    
    private final AvatarUrlContextProvider contextProvider = new AvatarUrlContextProvider(avatarServiceMock, authenticationContext);

    @Rule
    public final JiraAuthenticationContextRule authenticationContextRule = new JiraAuthenticationContextRule(authenticationContext, user);

    @AnonymousUser
    @Test
    public void anonymousUserWithDisabledAvatars()
    {
        givenAvatarsAreDisabled();
        final Map<String, Object> result = contextProvider.getContextMap(null);
        thenContextContainsNull(result);
    }

    @AuthenticatedUser
    @Test
    public void authenticatedUserWithDisabledAvatars()
    {
        givenAvatarsAreDisabled();
        final Map<String, Object> result = contextProvider.getContextMap(null);
        thenContextContainsNull(result);
    }

    @AnonymousUser
    @Test
    public void anonymousUser()
    {
        givenAvatarsAreEnabled(null);
        final Map<String, Object> result = contextProvider.getContextMap(null);
        thenContextContainsAvatarUrl(result);
    }

    @AuthenticatedUser
    @Test
    public void authenticatedUser()
    {
        givenAvatarsAreEnabled(user);
        final Map<String, Object> result = contextProvider.getContextMap(null);
        thenContextContainsAvatarUrl(result);
    }

    private void givenAvatarsAreDisabled()
    {
        when(avatarServiceMock.getAvatarURL(any(ApplicationUser.class), any(ApplicationUser.class), eq(AVATAR_SIZE))).thenThrow(new AvatarsDisabledException());
    }

    private void givenAvatarsAreEnabled(@Nullable final ApplicationUser user)
    {
        when(avatarServiceMock.getAvatarURL(user, user, AVATAR_SIZE)).thenReturn(URI.create(AVATAR_URL));
    }

    private void thenContextContainsNull(final Map<String, Object> result)
    {
        assertNotNull(result);
        assertThat(result, hasEntry(CONTEXT_KEY, null));
    }

    private void thenContextContainsAvatarUrl(final Map<String, Object> result)
    {
        assertNotNull(result);
        assertThat(result, hasEntry(CONTEXT_KEY, (Object) AVATAR_URL));
    }
}
