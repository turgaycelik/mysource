package com.atlassian.jira.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;

import java.util.Set;

/**
 * Represents a single permission granted in the JIRA workflow XML.
 */
public interface WorkflowPermission
{
    /**
     * Get users specified by this permission (eg. group members, or a single user).
     * @return A set of {@link User}s.
     */
    public Set getUsers(PermissionContext ctx);

    /**
     * Whether this workflow permission allows a permission.
     *
     * @param permission The requested permission
     * @param issue The current issue whose workflow step we consider.
     * @param user The user requesting the permission
     * @return Whether the workflow grants the permission.
     */
    public boolean allows(int permission, Issue issue, User user);
}
