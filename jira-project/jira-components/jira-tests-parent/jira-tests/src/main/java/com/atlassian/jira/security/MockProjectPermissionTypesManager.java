package com.atlassian.jira.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.atlassian.fugue.Option;
import com.atlassian.jira.permission.MockProjectPermission;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.permission.ProjectPermissionCategory;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.security.plugin.ProjectPermissionTypesManager;

import com.google.common.base.Predicate;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;
import static com.atlassian.jira.permission.ProjectPermissionCategory.ATTACHMENTS;
import static com.atlassian.jira.permission.ProjectPermissionCategory.COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissionCategory.ISSUES;
import static com.atlassian.jira.permission.ProjectPermissionCategory.PROJECTS;
import static com.atlassian.jira.permission.ProjectPermissionCategory.TIME_TRACKING;
import static com.atlassian.jira.permission.ProjectPermissionCategory.VOTERS_AND_WATCHERS;
import static com.google.common.collect.Collections2.filter;

/**
 * @since v6.3
 */
public class MockProjectPermissionTypesManager implements ProjectPermissionTypesManager
{
    private static final List<ProjectPermission> SYSTEM_PROJECT_PERMISSIONS = Arrays.<ProjectPermission>asList(
        new MockProjectPermission("ADMINISTER_PROJECTS", "admin.permissions.PROJECT_ADMIN", "admin.permissions.descriptions.PROJECT_ADMIN", PROJECTS),
        new MockProjectPermission("BROWSE_PROJECTS", "admin.permissions.BROWSE", "admin.permissions.descriptions.BROWSE", PROJECTS),
        new MockProjectPermission("VIEW_DEV_TOOLS", "admin.permissions.VIEW_VERSION_CONTROL", "admin.permissions.descriptions.VIEW_VERSION_CONTROL", PROJECTS),
        new MockProjectPermission("VIEW_READONLY_WORKFLOW", "admin.permissions.VIEW_WORKFLOW_READONLY", "admin.permissions.descriptions.WORKFLOW_VIEW_READONLY", PROJECTS),
        new MockProjectPermission("CREATE_ISSUES", "admin.permissions.CREATE_ISSUE", "admin.permissions.descriptions.CREATE_ISSUE", ISSUES),
        new MockProjectPermission("EDIT_ISSUES", "admin.permissions.EDIT_ISSUE", "admin.permissions.descriptions.EDIT_ISSUE", ISSUES),
        new MockProjectPermission("TRANSITION_ISSUES", "admin.permissions.TRANSITION_ISSUE", "admin.permissions.descriptions.TRANSITION_ISSUE", ISSUES),
        new MockProjectPermission("SCHEDULE_ISSUES", "admin.permissions.SCHEDULE_ISSUE", "admin.permissions.descriptions.SCHEDULE_ISSUE", ISSUES),
        new MockProjectPermission("MOVE_ISSUES", "admin.permissions.MOVE_ISSUE", "admin.permissions.descriptions.MOVE_ISSUE", ISSUES),
        new MockProjectPermission("ASSIGN_ISSUES", "admin.permissions.ASSIGN_ISSUE", "admin.permissions.descriptions.ASSIGN_ISSUE", ISSUES),
        new MockProjectPermission("ASSIGNABLE_USER", "admin.permissions.ASSIGNABLE_USER", "admin.permissions.descriptions.ASSIGNABLE_USER", ISSUES),
        new MockProjectPermission("RESOLVE_ISSUES", "admin.permissions.RESOLVE_ISSUE", "admin.permissions.descriptions.RESOLVE_ISSUE", ISSUES),
        new MockProjectPermission("CLOSE_ISSUES", "admin.permissions.CLOSE_ISSUE", "admin.permissions.descriptions.CLOSE_ISSUE", ISSUES),
        new MockProjectPermission("MODIFY_REPORTER", "admin.permissions.MODIFY_REPORTER", "admin.permissions.descriptions.MODIFY_REPORTER", ISSUES),
        new MockProjectPermission("DELETE_ISSUES", "admin.permissions.DELETE_ISSUE", "admin.permissions.descriptions.DELETE_ISSUE", ISSUES),
        new MockProjectPermission("LINK_ISSUES", "admin.permissions.LINK_ISSUE", "admin.permissions.descriptions.LINK_ISSUE", ISSUES),
        new MockProjectPermission("SET_ISSUE_SECURITY", "admin.permissions.SET_ISSUE_SECURITY", "admin.permissions.descriptions.SET_ISSUE_SECURITY", ISSUES),
        new MockProjectPermission("VIEW_VOTERS_AND_WATCHERS", "admin.permissions.VIEW_VOTERS_AND_WATCHERS", "admin.permissions.descriptions.VIEW_VOTERS_AND_WATCHERS", VOTERS_AND_WATCHERS),
        new MockProjectPermission("MANAGE_WATCHERS", "admin.permissions.MANAGE_WATCHER_LIST", "admin.permissions.descriptions.MANAGE_WATCHER_LIST", VOTERS_AND_WATCHERS),
        new MockProjectPermission("ADD_COMMENTS", "admin.permissions.COMMENT_ISSUE", "admin.permissions.descriptions.COMMENT_ISSUE", COMMENTS),
        new MockProjectPermission("EDIT_ALL_COMMENTS", "admin.permissions.COMMENT_EDIT_ALL", "admin.permissions.descriptions.COMMENT_EDIT_ALL", COMMENTS),
        new MockProjectPermission("EDIT_OWN_COMMENTS", "admin.permissions.COMMENT_EDIT_OWN", "admin.permissions.descriptions.COMMENT_EDIT_OWN", COMMENTS),
        new MockProjectPermission("DELETE_ALL_COMMENTS", "admin.permissions.COMMENT_DELETE_ALL", "admin.permissions.descriptions.COMMENT_DELETE_ALL", COMMENTS),
        new MockProjectPermission("DELETE_OWN_COMMENTS", "admin.permissions.COMMENT_DELETE_OWN", "admin.permissions.descriptions.COMMENT_DELETE_OWN", COMMENTS),
        new MockProjectPermission("CREATE_ATTACHMENTS", "admin.permissions.CREATE_ATTACHMENT", "admin.permissions.descriptions.CREATE_ATTACHMENT", ATTACHMENTS),
        new MockProjectPermission("DELETE_ALL_ATTACHMENTS", "admin.permissions.ATTACHMENT_DELETE_ALL", "admin.permissions.descriptions.ATTACHMENT_DELETE_ALL", ATTACHMENTS),
        new MockProjectPermission("DELETE_OWN_ATTACHMENTS", "admin.permissions.ATTACHMENT_DELETE_OWN", "admin.permissions.descriptions.ATTACHMENT_DELETE_OWN", ATTACHMENTS),
        new MockProjectPermission("WORK_ON_ISSUES", "admin.permissions.WORK_ISSUE", "admin.permissions.descriptions.WORK_ISSUE", TIME_TRACKING),
        new MockProjectPermission("EDIT_OWN_WORKLOGS", "admin.permissions.WORKLOG_EDIT_OWN", "admin.permissions.descriptions.WORKLOG_EDIT_OWN", TIME_TRACKING),
        new MockProjectPermission("EDIT_ALL_WORKLOGS", "admin.permissions.WORKLOG_EDIT_ALL", "admin.permissions.descriptions.WORKLOG_EDIT_ALL", TIME_TRACKING),
        new MockProjectPermission("DELETE_OWN_WORKLOGS", "admin.permissions.WORKLOG_DELETE_OWN", "admin.permissions.descriptions.WORKLOG_DELETE_OWN", TIME_TRACKING),
        new MockProjectPermission("DELETE_ALL_WORKLOGS", "admin.permissions.WORKLOG_DELETE_ALL", "admin.permissions.descriptions.WORKLOG_DELETE_ALL", TIME_TRACKING)
    );

    @Override
    public Collection<ProjectPermission> all()
    {
        return SYSTEM_PROJECT_PERMISSIONS;
    }

    @Override
    public Collection<ProjectPermission> withCategory(final ProjectPermissionCategory category)
    {
        return filter(all(), new Predicate<ProjectPermission>()
        {
            @Override
            public boolean apply(ProjectPermission permission)
            {
                return category.equals(permission.getCategory());
            }
        });
    }

    @Override
    public Option<ProjectPermission> withKey(ProjectPermissionKey key)
    {
        for (ProjectPermission permission : all())
        {
            if (permission.getKey().equals(key.permissionKey()))
            {
                return some(permission);
            }
        }
        return none();
    }

    @Override
    public boolean exists(ProjectPermissionKey key)
    {
        return withKey(key) != null;
    }
}
