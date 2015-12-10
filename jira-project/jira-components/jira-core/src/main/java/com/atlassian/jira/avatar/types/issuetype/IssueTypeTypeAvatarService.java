package com.atlassian.jira.avatar.types.issuetype;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.types.BasicTypedTypeAvatarService;
import com.atlassian.jira.avatar.types.TypedAvatarAccessPolicy;

public class IssueTypeTypeAvatarService extends BasicTypedTypeAvatarService
{
    // for DI only
    public IssueTypeTypeAvatarService(final AvatarManager avatarManager)
    {
        super(Avatar.Type.ISSUETYPE, avatarManager, new TypedAvatarAccessPolicy(Avatar.Type.ISSUETYPE));
    }
}
