package com.atlassian.jira.permission;

import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

/**
 * Provides mappings between legacy project permission ID-s and new project permission keys.
 *
 * @since v6.2.5
 */
public class LegacyProjectPermissionKeyMapping
{
    public static final BiMap<Integer, ProjectPermissionKey> ID_TO_KEY = ImmutableBiMap.<Integer, ProjectPermissionKey>builder().
        put(Permissions.PROJECT_ADMIN, ProjectPermissions.ADMINISTER_PROJECTS).
        put(Permissions.BROWSE, ProjectPermissions.BROWSE_PROJECTS).
        put(Permissions.VIEW_VERSION_CONTROL, ProjectPermissions.VIEW_DEV_TOOLS).
        put(Permissions.VIEW_WORKFLOW_READONLY, ProjectPermissions.VIEW_READONLY_WORKFLOW).
        put(Permissions.CREATE_ISSUE, ProjectPermissions.CREATE_ISSUES).
        put(Permissions.EDIT_ISSUE, ProjectPermissions.EDIT_ISSUES).
        put(Permissions.TRANSITION_ISSUE, ProjectPermissions.TRANSITION_ISSUES).
        put(Permissions.SCHEDULE_ISSUE, ProjectPermissions.SCHEDULE_ISSUES).
        put(Permissions.MOVE_ISSUE, ProjectPermissions.MOVE_ISSUES).
        put(Permissions.ASSIGN_ISSUE, ProjectPermissions.ASSIGN_ISSUES).
        put(Permissions.ASSIGNABLE_USER, ProjectPermissions.ASSIGNABLE_USER).
        put(Permissions.RESOLVE_ISSUE, ProjectPermissions.RESOLVE_ISSUES).
        put(Permissions.CLOSE_ISSUE, ProjectPermissions.CLOSE_ISSUES).
        put(Permissions.MODIFY_REPORTER, ProjectPermissions.MODIFY_REPORTER).
        put(Permissions.DELETE_ISSUE, ProjectPermissions.DELETE_ISSUES).
        put(Permissions.LINK_ISSUE, ProjectPermissions.LINK_ISSUES).
        put(Permissions.SET_ISSUE_SECURITY, ProjectPermissions.SET_ISSUE_SECURITY).
        put(Permissions.VIEW_VOTERS_AND_WATCHERS, ProjectPermissions.VIEW_VOTERS_AND_WATCHERS).
        put(Permissions.MANAGE_WATCHER_LIST, ProjectPermissions.MANAGE_WATCHERS).
        put(Permissions.COMMENT_ISSUE, ProjectPermissions.ADD_COMMENTS).
        put(Permissions.COMMENT_EDIT_ALL, ProjectPermissions.EDIT_ALL_COMMENTS).
        put(Permissions.COMMENT_EDIT_OWN, ProjectPermissions.EDIT_OWN_COMMENTS).
        put(Permissions.COMMENT_DELETE_ALL, ProjectPermissions.DELETE_ALL_COMMENTS).
        put(Permissions.COMMENT_DELETE_OWN, ProjectPermissions.DELETE_OWN_COMMENTS).
        put(Permissions.CREATE_ATTACHMENT, ProjectPermissions.CREATE_ATTACHMENTS).
        put(Permissions.ATTACHMENT_DELETE_ALL, ProjectPermissions.DELETE_ALL_ATTACHMENTS).
        put(Permissions.ATTACHMENT_DELETE_OWN, ProjectPermissions.DELETE_OWN_ATTACHMENTS).
        put(Permissions.WORK_ISSUE, ProjectPermissions.WORK_ON_ISSUES).
        put(Permissions.WORKLOG_EDIT_OWN, ProjectPermissions.EDIT_OWN_WORKLOGS).
        put(Permissions.WORKLOG_EDIT_ALL, ProjectPermissions.EDIT_ALL_WORKLOGS).
        put(Permissions.WORKLOG_DELETE_OWN, ProjectPermissions.DELETE_OWN_WORKLOGS).
        put(Permissions.WORKLOG_DELETE_ALL, ProjectPermissions.DELETE_ALL_WORKLOGS).
        build();

    public static ProjectPermissionKey getKey(Long id)
    {
        return (id == null) ? null : getKey(id.intValue());
    }

    public static ProjectPermissionKey getKey(Integer id)
    {
        return ID_TO_KEY.get(id);
    }

    public static Integer getId(ProjectPermissionKey key)
    {
        return ID_TO_KEY.inverse().get(key);
    }

    public static Long getIdAsLong(ProjectPermissionKey key)
    {
        Integer id = getId(key);

        return (id == null) ? null : id.longValue();
    }
}
