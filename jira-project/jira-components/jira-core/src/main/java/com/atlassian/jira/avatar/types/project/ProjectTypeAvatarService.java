package com.atlassian.jira.avatar.types.project;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.types.BasicTypedTypeAvatarService;

public class ProjectTypeAvatarService extends BasicTypedTypeAvatarService
{
    public ProjectTypeAvatarService(final AvatarManager avatarManager, final ProjectAvatarAccessPolicy accessPolicy)
    {
        super(Avatar.Type.PROJECT, avatarManager, accessPolicy);
    }
}
