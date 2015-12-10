package com.atlassian.jira.pageobjects.elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.jira.avatar.Avatar;

/**
 * Avatar links come in two shapes: old imageUrl links and real link to avatars.
 */
public abstract class AvatarId
{
    public abstract String asRelativeLinkForDefaultSize(Avatar.Type type);

    private static class NumberedAvatarId extends AvatarId
    {
        private final long avatarId;

        private NumberedAvatarId(final long avatarId) {this.avatarId = avatarId;}

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            final NumberedAvatarId that = (NumberedAvatarId) o;

            if (avatarId != that.avatarId) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            return (int) (avatarId ^ (avatarId >>> 32));
        }

        @Override
        public String toString()
        {
            return "Avatar - id" + avatarId;
        }

        @Override
        public String asRelativeLinkForDefaultSize(final Avatar.Type type)
        {
            return String.format("/secure/viewavatar?avatarId=%dl&avatarType=%s", avatarId, type.getName());
        }
    }

    private static class LinkAsAvatarId extends AvatarId
    {
        private final String fullImageLink;

        private LinkAsAvatarId(final String fullImageLink) {this.fullImageLink = fullImageLink;}

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            final LinkAsAvatarId that = (LinkAsAvatarId) o;

            if (!fullImageLink.equals(that.fullImageLink)) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            return fullImageLink.hashCode();
        }

        @Override
        public String toString()
        {
            return "Avatar - link '" + fullImageLink + '\'';
        }

        @Override
        public String asRelativeLinkForDefaultSize(final Avatar.Type type)
        {
            return fullImageLink;
        }
    }

    public static AvatarId fromId(long id)
    {
        return new NumberedAvatarId(id);
    }

    public static AvatarId fromImageLink(String link)
    {
        final Pattern avatarIdPattern = Pattern.compile("[&\\?]avatarId=([0-9]+)", 0);
        final Matcher avatarIdMatcher = avatarIdPattern.matcher(link);

        if (avatarIdMatcher.find())
        {
            final String avatarIdString = avatarIdMatcher.group(1);
            try
            {
                Long avatarId = Long.valueOf(avatarIdString);
                return new NumberedAvatarId(avatarId);
            }
            catch (NumberFormatException e)
            {
            }
        }

        return new LinkAsAvatarId(link);
    }
}
