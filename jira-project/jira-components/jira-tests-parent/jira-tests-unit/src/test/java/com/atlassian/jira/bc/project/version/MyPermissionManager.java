package com.atlassian.jira.bc.project.version;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.project.Project;

import org.ofbiz.core.entity.GenericValue;

/**
 * A MockPermissionManager where all the hasPermission methods have been overriden to return the default permission
 * setting.
 *
 * TODO - merge with MockPermissionManager?
 * 
 * @since v3.13
 */
final class MyPermissionManager extends MockPermissionManager
{
    static MockPermissionManager createPermissionManager(final boolean defaultPermission)
    {
        MockPermissionManager permissionManager = new MyPermissionManager();
        permissionManager.setDefaultPermission(defaultPermission);
        return permissionManager;
    }

    public boolean hasPermission(final int permissionsId, final User user)
    {
        return isDefaultPermission();
    }

    public boolean hasPermission(final int permissionsId, final GenericValue projectOrIssue, final User u)
    {
        return isDefaultPermission();
    }

    public boolean hasPermission(final int permissionsId, final Issue issue, final User u)
    {
        return isDefaultPermission();
    }

    public boolean hasPermission(final int permissionsId, final Project project, final User user)
    {
        return isDefaultPermission();
    }

    public boolean hasPermission(final int permissionsId, final Project project, final User user, final boolean issueCreation)
    {
        return isDefaultPermission();
    }
}
