package com.atlassian.jira.avatar;

import javax.annotation.Nonnull;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.dbc.Assertions;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Immutable implementation.
 *
 * @since v4.0
 */
public final class AvatarImpl implements Avatar
{
    private static final String IMAGE_PNG = "image/png";
    private final Long id;
    private final String fileName;
    private final Avatar.Type avatarType;
    private final String owner;
    private final boolean systemAvatar;
    private static String TAGGED_AVATAR_FILE_SUFFIX = "jrvtg.png";
    private String contentType;

    AvatarImpl(final Long id, final String fileName, final String contentType, final Avatar.Type avatarType, final String owner, final boolean systemAvatar)
    {
        this.id = id;

        this.fileName = Assertions.notNull("fileName", fileName);
        this.contentType = Assertions.notNull("contentType", contentType);
        this.avatarType = Assertions.notNull("avatarType", avatarType);
        this.systemAvatar = systemAvatar;

        // system avatars have no owner.
        if (!isSystemAvatar() && !isOldSystemAvatar(this.avatarType, this.fileName))
        {
            Assertions.notNull("owner", owner);
            this.owner = owner;
        }
        else if (owner != null)
        {
            throw new IllegalArgumentException("owner must be null for system avatars");
        }
        else
        {
            this.owner = null;
        }
    }

    /**
     * Factory method for creating a custom Avatar to be created by {@link com.atlassian.jira.avatar.AvatarManager}.
     */
    public static AvatarImpl createCustomAvatar(final String fileName, final String contentType, final Project owner)
    {
        return new AvatarImpl(null, toTaggedFilename(fileName), contentType, Type.PROJECT, owner.getId().toString(), false);
    }

    private static String toTaggedFilename(String fileName)
    {
        return FilenameUtils.removeExtension(fileName) + TAGGED_AVATAR_FILE_SUFFIX;
    }

    public static AvatarImpl createCustomAvatar(final String fileName, final String contentType, final ApplicationUser owner)
    {
        return new AvatarImpl(null, toTaggedFilename(fileName), IMAGE_PNG, Type.USER, owner.getKey(), false);
    }

    /**
     * Factory method for creating a system Avatar to be created by {@link com.atlassian.jira.avatar.AvatarManager}.
     */
    public static AvatarImpl createSystemAvatar(final String fileName, final String contentType, final Avatar.Type avatarType)
    {
        return new AvatarImpl(null, fileName, IMAGE_PNG, avatarType, null, true);
    }

    public Avatar.Type getAvatarType()
    {
        return avatarType;
    }

    public String getFileName()
    {
        return fileName;
    }

    @Nonnull
    public String getContentType()
    {
        return contentType;
    }

    public Long getId()
    {
        return id;
    }

    public String getOwner()
    {
        return owner;
    }

    public boolean isSystemAvatar()
    {
        return systemAvatar;
    }

    public static boolean isOldSystemAvatar(Avatar.Type type, String filename)
    {
        if (StringUtils.isBlank(filename))
        {
            return false;
        }
        if (type.equals(Type.PROJECT))
        {
            return demotedSystemProjectAvatars.contains(filename.toLowerCase());
        }
        return false;
    }

    ///CLOVER:OFF
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final AvatarImpl avatar = (AvatarImpl) o;

        if (systemAvatar != avatar.systemAvatar)
        {
            return false;
        }
        if (avatarType != avatar.avatarType)
        {
            return false;
        }
        if (fileName != null ? !fileName.equals(avatar.fileName) : avatar.fileName != null)
        {
            return false;
        }
        if (id != null ? !id.equals(avatar.id) : avatar.id != null)
        {
            return false;
        }
        if (owner != null ? !owner.equals(avatar.owner) : avatar.owner != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (avatarType != null ? avatarType.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (systemAvatar ? 1 : 0);
        return result;
    }
    ///CLOVER:ON
}
