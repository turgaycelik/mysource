/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.movesubtask.operation;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.bean.MoveIssueBean;

public abstract class AbstractMoveSubTaskOperation implements MoveSubTaskOperation
{
    private final IssueManager issueManager;

    public AbstractMoveSubTaskOperation(IssueManager issueManager)
    {
        this.issueManager = issueManager;
    }

    public boolean canPerform(MoveIssueBean moveIssueBean, User remoteUser)
    {
        Long issueId = moveIssueBean.getIssueId();
        PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
        return permissionManager.hasPermission(Permissions.MOVE_ISSUE, issueManager.getIssueObject(issueId), remoteUser);
    }
}
