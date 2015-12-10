package com.atlassian.jira.permission;

import java.util.Locale;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.Permissions;

/**
 * Since v6.2.
 *
 * @deprecated Use {@link com.atlassian.jira.security.PermissionManager}, {@link com.atlassian.jira.security.GlobalPermissionManager}. Since v6.3.
 */
@Deprecated
public enum Permission
{
    ADMINISTER(0, "admin.global.permissions.administer", "admin.permissions.descriptions.ADMINISTER"),
    USE(1, "admin.global.permissions.use", "admin.permissions.descriptions.USE"),
    SYSTEM_ADMIN(44, "admin.global.permissions.system.administer", "admin.permissions.descriptions.SYS_ADMIN"),
    CREATE_SHARED_OBJECTS(22, "admin.global.permissions.create.shared.filter", "admin.permissions.descriptions.CREATE_SHARED_OBJECTS"),
    MANAGE_GROUP_FILTER_SUBSCRIPTIONS(24, "admin.global.permissions.manage.group.filter.subscriptions", "admin.permissions.descriptions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS"),
    BULK_CHANGE(33, "admin.global.permissions.bulk.change", "admin.permissions.descriptions.BULK_CHANGE"),
    USER_PICKER(27, "admin.global.permissions.user.picker", "admin.permissions.descriptions.USER_PICKER"),

    PROJECT_ADMIN(23, "admin.permissions.PROJECT_ADMIN", "admin.permissions.descriptions.PROJECT_ADMIN"),
    BROWSE(10, "admin.permissions.BROWSE", "admin.permissions.descriptions.BROWSE"),
    VIEW_VERSION_CONTROL(29, "admin.permissions.VIEW_VERSION_CONTROL", "admin.permissions.descriptions.VIEW_VERSION_CONTROL"),
    VIEW_WORKFLOW_READONLY(45, "admin.permissions.VIEW_WORKFLOW_READONLY", "admin.permissions.descriptions.WORKFLOW_VIEW_READONLY"),

    CREATE_ISSUE (11, "admin.permissions.CREATE_ISSUE", "admin.permissions.descriptions.CREATE_ISSUE"),
    EDIT_ISSUE (12, "admin.permissions.EDIT_ISSUE", "admin.permissions.descriptions.EDIT_ISSUE"),
    ASSIGN_ISSUE (13, "admin.permissions.ASSIGN_ISSUE", "admin.permissions.descriptions.ASSIGN_ISSUE"),
    RESOLVE_ISSUE (14, "admin.permissions.RESOLVE_ISSUE", "admin.permissions.descriptions.RESOLVE_ISSUE"),
    DELETE_ISSUE (16, "admin.permissions.DELETE_ISSUE", "admin.permissions.descriptions.DELETE_ISSUE"),
    ASSIGNABLE_USER (17, "admin.permissions.ASSIGNABLE_USER", "admin.permissions.descriptions.ASSIGNABLE_USER"), // permission to be assigned issues
    CLOSE_ISSUE (18, "admin.permissions.CLOSE_ISSUE", "admin.permissions.descriptions.CLOSE_ISSUE"),
    TRANSITION_ISSUE (Permissions.TRANSITION_ISSUE, "admin.permissions.TRANSITION_ISSUE", "admin.permissions.descriptions.TRANSITION_ISSUE"),
    LINK_ISSUE (21, "admin.permissions.LINK_ISSUE", "admin.permissions.descriptions.LINK_ISSUE"),
    MOVE_ISSUE (25, "admin.permissions.MOVE_ISSUE", "admin.permissions.descriptions.MOVE_ISSUE"),
    SET_ISSUE_SECURITY (26, "admin.permissions.SET_ISSUE_SECURITY", "admin.permissions.descriptions.SET_ISSUE_SECURITY"),
    SCHEDULE_ISSUE (28, "admin.permissions.SCHEDULE_ISSUE", "admin.permissions.descriptions.SCHEDULE_ISSUE"),
    MODIFY_REPORTER (30, "admin.permissions.MODIFY_REPORTER", "admin.permissions.descriptions.MODIFY_REPORTER"),

    VIEW_VOTERS_AND_WATCHERS(31, "admin.permissions.VIEW_VOTERS_AND_WATCHERS", "admin.permissions.descriptions.VIEW_VOTERS_AND_WATCHERS"),
    MANAGE_WATCHER_LIST (32, "admin.permissions.MANAGE_WATCHER_LIST", "admin.permissions.descriptions.MANAGE_WATCHER_LIST"),

    COMMENT_ISSUE(15, "admin.permissions.COMMENT_ISSUE", "admin.permissions.descriptions.COMMENT_ISSUE"),
    COMMENT_EDIT_ALL(34, "admin.permissions.COMMENT_EDIT_ALL", "admin.permissions.descriptions.COMMENT_EDIT_ALL"),
    COMMENT_EDIT_OWN(35, "admin.permissions.COMMENT_EDIT_OWN", "admin.permissions.descriptions.COMMENT_EDIT_OWN"),
    COMMENT_DELETE_ALL (36, "admin.permissions.COMMENT_DELETE_ALL", "admin.permissions.descriptions.COMMENT_DELETE_ALL"),
    COMMENT_DELETE_OWN (37, "admin.permissions.COMMENT_DELETE_OWN", "admin.permissions.descriptions.COMMENT_DELETE_OWN"),

    CREATE_ATTACHMENT(19, "admin.permissions.CREATE_ATTACHMENT", "admin.permissions.descriptions.CREATE_ATTACHMENT"),
    ATTACHMENT_DELETE_ALL (38, "admin.permissions.ATTACHMENT_DELETE_ALL", "admin.permissions.descriptions.ATTACHMENT_DELETE_ALL"),
    ATTACHMENT_DELETE_OWN (39, "admin.permissions.ATTACHMENT_DELETE_OWN", "admin.permissions.descriptions.ATTACHMENT_DELETE_OWN"),

    WORK_ISSUE(20, "admin.permissions.WORK_ISSUE", "admin.permissions.descriptions.WORK_ISSUE"), // Permission to log work done on an issue.
    WORKLOG_EDIT_OWN(40, "admin.permissions.WORKLOG_EDIT_OWN", "admin.permissions.descriptions.WORKLOG_EDIT_OWN"),
    WORKLOG_EDIT_ALL(41, "admin.permissions.WORKLOG_EDIT_ALL", "admin.permissions.descriptions.WORKLOG_EDIT_ALL"),
    WORKLOG_DELETE_OWN(42, "admin.permissions.WORKLOG_DELETE_OWN", "admin.permissions.descriptions.WORKLOG_DELETE_OWN"),
    WORKLOG_DELETE_ALL(43, "admin.permissions.WORKLOG_DELETE_ALL", "admin.permissions.descriptions.WORKLOG_DELETE_ALL");

    private final int id;
    private final String nameKey;
    private final String descriptionKey;

    Permission(final int id, final String nameKey, final String descriptionKey)
    {
        this.id = id;
        this.nameKey = nameKey;
        this.descriptionKey = descriptionKey;
    }

    public int getId()
    {
        return id;
    }

    /**
     * This method returns the name of the permission in English (ignoring user locale).
     *
     * @return name of the permission
     */
    public String getName()
    {
        return ComponentAccessor.getI18nHelperFactory().getInstance(Locale.ENGLISH).getText(getNameKey());
    }

    public String getNameKey()
    {
        return nameKey;
    }

    /**
     * This method returns the description of the permission in user selected language.
     *
     * @return description of the permission
     */
    public String getDescription()
    {
        return ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText(getDescriptionKey());
    }

    public String getDescriptionKey()
    {
        return descriptionKey;
    }
}