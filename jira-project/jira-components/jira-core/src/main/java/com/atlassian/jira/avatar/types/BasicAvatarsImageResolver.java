package com.atlassian.jira.avatar.types;

import java.net.URI;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarImageResolver;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.servlet.ViewUniversalAvatarServletImpl;

public class BasicAvatarsImageResolver implements AvatarImageResolver
{
    public static final String AVATAR_ID_PARAM = ViewUniversalAvatarServletImpl.AVATAR_ID_PARAM;
    public static final String SIZE_PARAM = ViewUniversalAvatarServletImpl.AVATAR_SIZE_PARAM;
    public static final String PATH_PREFIX = "/secure/viewavatar";
    public static final String AVATAR_TYPE_PARAM = ViewUniversalAvatarServletImpl.AVATAR_TYPE_PARAM;
    private final Avatar.Type type;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ApplicationProperties applicationProperties;

    public BasicAvatarsImageResolver(final Avatar.Type type, final VelocityRequestContextFactory velocityRequestContextFactory, final ApplicationProperties applicationProperties) {
        this.type = type;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public URI getAvatarAbsoluteUri(final ApplicationUser avatarUser, final Avatar avatar, final Avatar.Size requestedSize)
    {
        URI relativeUri = getAvatarRelativeUri(avatarUser, avatar, requestedSize);
        final String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();

        final UrlBuilder absoluteUriBuilder = new UrlBuilder(baseUrl + relativeUri.toASCIIString());

        return absoluteUriBuilder.asURI();
    }

    @Override
    public URI getAvatarRelativeUri(final ApplicationUser remoteUser, final Avatar avatar, final Avatar.Size requestedSize) {
        final UrlBuilder urlBuilder = new UrlBuilder(PATH_PREFIX, applicationProperties.getEncoding(), false);

        addAvatarParametersToUrlBuilder(urlBuilder, avatar, requestedSize);

        return urlBuilder.asURI();
    }

    private void addAvatarParametersToUrlBuilder(final UrlBuilder urlBuilder, final Avatar avatar, final Avatar.Size size)
    {
        if (!isSizeDefaultOrNone(size))
        {
            urlBuilder.addParameter(SIZE_PARAM, size.getParam());
        }

        Long avatarId = avatar.getId();
        urlBuilder.addParameter(AVATAR_ID_PARAM, avatarId.toString());
        urlBuilder.addParameter(AVATAR_TYPE_PARAM, type.getName());
    }

    private boolean isSizeDefaultOrNone(final Avatar.Size size)
    {
        return size == null || size == Avatar.Size.defaultSize();
    }
}
