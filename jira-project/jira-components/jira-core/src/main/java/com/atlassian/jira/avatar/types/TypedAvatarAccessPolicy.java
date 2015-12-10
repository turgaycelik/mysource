package com.atlassian.jira.avatar.types;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.user.ApplicationUser;

public class TypedAvatarAccessPolicy implements AvatarAccessPolicy
{
    private final Avatar.Type type;

    public TypedAvatarAccessPolicy(final Avatar.Type type)
    {
        this.type = type;
    }

    @Override
    public boolean userCanViewAvatar(final ApplicationUser user, final Avatar avatar)
    {
        return type == avatar.getAvatarType();
    }

    @Override
    public boolean userCanCreateAvatarFor(final ApplicationUser user, final String owniningObjectId)
    {
        return true;
    }
}
