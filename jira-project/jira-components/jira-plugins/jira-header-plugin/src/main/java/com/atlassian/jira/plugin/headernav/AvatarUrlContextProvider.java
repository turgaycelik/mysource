package com.atlassian.jira.plugin.headernav;

import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.avatar.AvatarsDisabledException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static com.atlassian.jira.avatar.Avatar.Size;

/**
 * Injects the url of the user's avatar into the context (see {@link #AVATAR_URL_KEY} for the key). If the avatar could
 * not be found, the context contains <code>null</code>.
 */
public class AvatarUrlContextProvider implements ContextProvider
{
    static final String AVATAR_URL_KEY = "avatarUrl";
    static final Size AVATAR_SIZE = Size.NORMAL;

    private final AvatarService avatarService;
    private final JiraAuthenticationContext authenticationContext;

    public AvatarUrlContextProvider(@Nonnull final AvatarService avatarService, @Nonnull final JiraAuthenticationContext authenticationContext)
    {
        this.avatarService = Assertions.notNull(avatarService);
        this.authenticationContext = Assertions.notNull(authenticationContext);
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Nonnull
    @Override
    public Map<String, Object> getContextMap(@Nullable final Map<String, Object> context)
    {
        final ApplicationUser user = authenticationContext.getUser();
        final String url = getAvatarUrl(user);
        return Collections.<String, Object>singletonMap(AVATAR_URL_KEY, url);
    }

    @Nullable
    private String getAvatarUrl(@Nullable final ApplicationUser user)
    {
        return getAvatarUrl(user, AVATAR_SIZE);
    }

    @Nullable
    private String getAvatarUrl(@Nullable final ApplicationUser user, @Nonnull final Size size)
    {
        try
        {
            final URI avatarURL = avatarService.getAvatarURL(user, user, size);
            return avatarURL != null ? avatarURL.toASCIIString() : null;
        }
        catch (AvatarsDisabledException e)
        {
            return null;
        }
    }
}
