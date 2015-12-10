package com.atlassian.jira.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;

import java.util.Collections;
import java.util.Set;

/**
 * Denies the permission for everyone.
 */
public class DenyWorkflowPermission implements WorkflowPermission
{
    private int denyPermission;

    public DenyWorkflowPermission(int permissionId)
    {
        this.denyPermission = permissionId;
    }

    public Set getUsers(PermissionContext ctx)
    {
        return Collections.EMPTY_SET;
    }


    public boolean allows(int permission, Issue issue, User user)
    {
        return permission != denyPermission;
    }

    public String toString()
    {
        return "'denied' workflow permission";
    }
}
