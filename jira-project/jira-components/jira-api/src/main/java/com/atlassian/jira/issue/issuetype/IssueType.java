/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.issuetype;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.issue.IssueConstant;

@PublicApi
public interface IssueType extends IssueConstant
{
    boolean isSubTask();

    /**
     * Get avatar assigned to this IssueType. Link to avatar will be automatically assigned to property iconUrl.
     * <p/>
     * When all issue constants will have avatars it should be moved into IssueConstant.
     *
     * @return avatar or null if this item has no avatar assigned.
     */
    @Nullable
    Avatar getAvatar();
}
