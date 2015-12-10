package com.atlassian.jira.avatar;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Helper class for building avatar URL maps.
 *
 * @since JIRA 6.3
 */
public class AvatarUrls
{
    /**
     * Returns the avatar URLs a user and a specific Avatar.
     *
     * @param avatarUser the user whose avatar this is (or null)
     * @param avatar the Avatar
     * @return avatar URLs mapped by size
     */
    public static Map<String, URI> getAvatarURLs(ApplicationUser avatarUser, Avatar avatar)
    {
        final AvatarService avatarService = ComponentAccessor.getAvatarService();
        final Map<String, URI> avatarUrls = new HashMap<String, URI>();

        for (Avatar.Size size : Avatar.Size.values())
        {
            final int px = size.getPixels();
            if (px <= 48) // TODO JRADEV-20790 - Don't output higher res URLs in our REST endpoints until we start using them ourselves.
            {
                final String sizeName = String.format("%dx%d", px, px);
                    avatarUrls.put(sizeName, avatarService.getAvatarUrlNoPermCheck(avatarUser, avatar,  size));
            }
        }

        return avatarUrls;
    }
}
