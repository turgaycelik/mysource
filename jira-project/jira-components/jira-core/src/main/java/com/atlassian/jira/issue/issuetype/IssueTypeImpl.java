/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.issuetype;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueConstantImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.BaseUrl;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;

import org.ofbiz.core.entity.GenericValue;

public class IssueTypeImpl extends IssueConstantImpl implements IssueType
{

    public static final String AVATAR_FIELD = "avatar";
    private final AvatarManager avatarManager;

    public IssueTypeImpl(GenericValue genericValue, TranslationManager translationManager,
            JiraAuthenticationContext authenticationContext, BaseUrl locator, AvatarManager avatarManager)
    {
        super(genericValue, translationManager, authenticationContext, locator);
        this.avatarManager = avatarManager;
    }

    public boolean isSubTask()
    {
        return ComponentAccessor.getSubTaskManager().isSubTaskIssueType(genericValue);
    }

    @Override
    public Avatar getAvatar()
    {
        final Long avatarId = getAvatarId();

        return avatarId != null ?
                getAvatarOrDefault(avatarId) :
                null;
    }

    private Avatar getAvatarOrDefault(final Long avatarId)
    {
        Avatar assignedAvatar = avatarManager.getById(avatarId);
        if ( null==assignedAvatar ) {
            assignedAvatar = avatarManager.getById(avatarManager.getDefaultAvatarId(Avatar.Type.ISSUETYPE));
        }
        return assignedAvatar;
    }

    public String getType()
    {
        if (isSubTask())
        {
            return "Sub-Task";
        }
        else
        {
            return "Standard";
        }
    }

    public Long getAvatarId()
    {
        return genericValue.getLong(AVATAR_FIELD);
    }

    public void setAvatarId(final Long avatarId)
    {
        genericValue.set(AVATAR_FIELD, avatarId);
    }
}
