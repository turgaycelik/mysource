package com.atlassian.jira.avatar;


import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.user.ApplicationUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;

/**
 * Provides links (or image date) to display avatar image in given size.
 *
 * @see com.atlassian.jira.avatar.UniversalAvatarsService#getImages(com.atlassian.jira.avatar.Avatar.Type)
 * @since v6.3
 */
@ExperimentalApi
public interface AvatarImageResolver
{
    /**
     * Return absolute link to display avatar image of given size.
     *
     * @param remoteUser user that access avatar - null can be used for anonymous access.
     * @param avatar avatar where to look for images
     * @param requestedSize requested size or null if default size should be used.
     * @return absolute url
     */
    @Nonnull
    public URI getAvatarAbsoluteUri(@Nullable ApplicationUser remoteUser, @Nonnull Avatar avatar, @Nullable Avatar.Size requestedSize);


    /**
     * Return link (relative to application context) to display avatar image of given size.
     *
     * @param remoteUser user that access avatar - null can be used for anonymous access.
     * @param avatar avatar where to look for images
     * @param requestedSize requested size or null if default size should be used.
     * @return the relative uri to the avatar with the requested size, relative to <tt>servletContext</tt>.
     */
    @Nonnull
    public URI getAvatarRelativeUri(@Nullable ApplicationUser remoteUser, @Nonnull Avatar avatar, @Nullable Avatar.Size requestedSize);
}
