package com.atlassian.jira.rest.v2.avatar;

import java.net.URI;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.avatar.AvatarImageResolver;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import org.springframework.stereotype.Component;

/**
 * Helper class for building avatar URL maps.
 *
 * @since JIRA 6.3
 */
@Component
public class AvatarUrls
{

    private static final Predicate<Avatar.Size> LOW_RES_AVATARS = new Predicate<Avatar.Size>()
    {
        @Override
        public boolean apply( final Avatar.Size input)
        {
            return input.getPixels() <= 48;
        }
    };

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

        // TODO JRADEV-20790 - Don't output higher res URLs in our REST endpoints until we start using them ourselves.
        final Iterable<Avatar.Size> lowResAvatars = Iterables.filter(EnumSet.allOf(Avatar.Size.class), LOW_RES_AVATARS);

        for (Avatar.Size size : lowResAvatars)
        {
            final String sizeName = getAvatarSizeName(size);

            avatarUrls.put(sizeName, avatarService.getAvatarUrlNoPermCheck(avatarUser, avatar,  size));
        }

        return avatarUrls;
    }

    public Map<String, URI> getAvatarURLs(final ApplicationUser remoteUser, final Avatar avatar, final AvatarImageResolver avatarImageResolver)
    {
        final Map<String, URI> avatarUrls = Maps.newHashMap();

        // TODO JRADEV-20790 - Don't output higher res URLs in our REST endpoints until we start using them ourselves.
        final Iterable<Avatar.Size> lowResAvatars = Iterables.filter(EnumSet.allOf(Avatar.Size.class), LOW_RES_AVATARS);

        for (Avatar.Size size : lowResAvatars)
        {
            final String sizeName = getAvatarSizeName(size);

            final URI avatarUri = avatarImageResolver.getAvatarAbsoluteUri(remoteUser, avatar, size);
            avatarUrls.put(sizeName, avatarUri);
        }

        return avatarUrls;
    }

    private static String getAvatarSizeName(final Avatar.Size size)
    {
        final int px = size.getPixels();
        return String.format("%dx%d", px, px);
    }
}
