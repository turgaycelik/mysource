/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.component.ComponentAccessor;

import org.apache.commons.lang3.StringUtils;

/**
 * @deprecated Use fields and utility methods defined in {@link com.atlassian.jira.permission.ProjectPermissions}
 *              and {@link com.atlassian.jira.permission.GlobalPermissionType}. Since v6.3.
 */
@Deprecated
public final class Permissions
{
    /**
     * Permission to administer JIRA
     */
    public static final int ADMINISTER = 0;

    /**
     * Permission to use JIRA
     */
    public static final int USE = 1;

    /**
     * Permission to be a System Admin of JIRA
     */
    public static final int SYSTEM_ADMIN = 44;

    /**
     * Permission to administer a Project
     */
    public static final int PROJECT_ADMIN = 23;

    /**
     * Permission to browse projects and issues.
     * <p/>
     * This includes filtering using the Issue Navigator.
     */
    public static final int BROWSE = 10;

    /**
     * Permission to create issues.
     */
    public static final int CREATE_ISSUE = 11;

    /**
     * Permission to edit issues.
     * <p/>
     * This includes managing attachments and adding them.
     */
    public static final int EDIT_ISSUE = 12;

    /**
     * Permission to assign issues to other users.
     */
    public static final int ASSIGN_ISSUE = 13;

    /**
     * Permission to resolve issues.
     */
    public static final int RESOLVE_ISSUE = 14;

    /**
     * Permission to comment on issues
     */
    public static final int COMMENT_ISSUE = 15;

    /**
     * Permission to delete issues and comments.
     */
    public static final int DELETE_ISSUE = 16;

    /**
     * Permission to be assigned issues.
     */
    public static final int ASSIGNABLE_USER = 17;

    /**
     * Permission to close issues.
     */
    public static final int CLOSE_ISSUE = 18;

    /**
     * Permission to perform workflow action (transition).
     * <p>
     * For 6.2.x release it's partially used. It was introduced for veto transition but the actual permission check
     * is performed against the BROWSE permission. This permission is not visible in UI or REST.
     * <p>
     * In 6.3 release this will be full-fledged permission.
     * @since 6.2.2
     */
    @ExperimentalApi
    public static final int TRANSITION_ISSUE = 46;

    /**
     * Permission to create attachments.
     */
    public static final int CREATE_ATTACHMENT = 19;

    /**
     * Permission to log work done on an issue.
     */
    public static final int WORK_ISSUE = 20;

    /**
     * Permission to link issues together and create linked issues.
     */
    public static final int LINK_ISSUE = 21;

    /**
     * Permission to created shared objects
     */
    public static final int CREATE_SHARED_OBJECTS = 22;

    /**
     * Permission to manage group filter subscriptions
     */
    public static final int MANAGE_GROUP_FILTER_SUBSCRIPTIONS = 24;

    /**
     * Permission to modify all comments
     */
    public static final int COMMENT_EDIT_ALL = 34;

    /**
     * Permission to modify only your own comments
     */
    public static final int COMMENT_EDIT_OWN = 35;

    /**
     * Permission to move issues between projects
     */
    public static final int MOVE_ISSUE = 25;
    public static final int SET_ISSUE_SECURITY = 26;
    public static final int USER_PICKER = 27;
    public static final int SCHEDULE_ISSUE = 28;
    public static final int VIEW_VERSION_CONTROL = 29;
    public static final int MODIFY_REPORTER = 30;
    public static final int VIEW_VOTERS_AND_WATCHERS = 31;
    public static final int MANAGE_WATCHER_LIST = 32;

    /**
     * Permission to modify a collection of issues (global)
     */
    public static final int BULK_CHANGE = 33;

    /**
     * Permission to delete all comments
     */
    public static final int COMMENT_DELETE_ALL = 36;

    /**
     * Permission to delete own comments
     */
    public static final int COMMENT_DELETE_OWN = 37;

    /**
     * Permission to delete all attachments
     */
    public static final int ATTACHMENT_DELETE_ALL = 38;

    /**
     * Permission to delete own attachments
     */
    public static final int ATTACHMENT_DELETE_OWN = 39;

    /**
     * Permission to edit/remove worklogs
     */
    public static final int WORKLOG_EDIT_OWN = 40;
    public static final int WORKLOG_EDIT_ALL = 41;
    public static final int WORKLOG_DELETE_OWN = 42;
    public static final int WORKLOG_DELETE_ALL = 43;

    public static final int VIEW_WORKFLOW_READONLY = 45;

    /**
     * This constant should not be used for a number of reasons.
     * <ul>
     *     <li>
     *       Because of the way that Java compiles public static final int, a plugin will not be able to detect
     *       changes in this value if we add new permissions.
     *     </li>
     *     <li>
     *       Global Permissions have been made pluggable, so they are now keyed by String rather than int.<br/>
     *       The ints will continued to work for compatibility reasons for some time, but the ideas of a
     *       "max permission ID" no longer makes sense.
     *       See {@link GlobalPermissionManager#getAllGlobalPermissions()}.
     *     </li>
     *     <li>
     *       There are plans to make Project Permissions pluggable as well in the near future.<br>
     *       For now, using the {@link com.atlassian.jira.security.Permissions.Permission} enum is preferred over
     *       {@code MAX_PERMISSION}.
     *     </li>
     * </ul>
     *
     * @deprecated Do not use MAX_PERMISSION - it is unsafe due to the way that Java compiles constant ints, and
     * no longer relevant. Since v6.2.3
     */
    @Deprecated
    public static final int MAX_PERMISSION = 46;

    /**
     * Returns a Set of all the permissions that grant a user the permission to log into
     * JIRA (i.e. 'Use' JIRA).
     *
     * @return A set containing all the permissions or an empty set otherwise
     * @since 3.13
     * @deprecated Log in permission will be changing significantly in JIRA 7.0 ... if you are using this method, then you will need to watch out for announcements in 7.0 Developer upgrade guide. Since v6.2.5
     */
    public static Set<Integer> getUsePermissions()
    {
        //Using a sorted set here to make things a bit easier for unit tests
        final Set<Integer> ret = new TreeSet<Integer>();
        ret.add(ADMINISTER);
        ret.add(USE);
        ret.add(SYSTEM_ADMIN);
        return Collections.unmodifiableSet(ret);
    }

    /**
     * Given a string approximation, try to guess the type
     * If permName is null then -1 is returned
     * If the permName is not recognised then -1 is returned
     *
     * @param permName permission name
     * @return permission type
     */
    public static int getType(final String permName)
    {
        if (StringUtils.isBlank(permName))
        {
            return -1;
        }

        final String perm = permName.toLowerCase();

        if (perm.startsWith("admin"))
        {
            return ADMINISTER;
        }
        else if (perm.startsWith("use"))
        {
            return USE;
        }
        else if (perm.startsWith("sysadmin"))
        {
            return SYSTEM_ADMIN;
        }
        else if (perm.startsWith("project"))
        {
            return PROJECT_ADMIN;
        }
        else if (perm.startsWith("browse"))
        {
            return BROWSE;
        }
        else if (perm.startsWith("create"))
        {
            return CREATE_ISSUE;
        }
        else if (perm.startsWith("edit"))
        {
            return EDIT_ISSUE;
        }
        else if (perm.startsWith("update"))
        {
            return EDIT_ISSUE;
        }
        else if (perm.startsWith("scheduleissue"))
        {
            return SCHEDULE_ISSUE;
        }
        else if (perm.startsWith("assignable"))
        {
            return ASSIGNABLE_USER;
        }
        else if (perm.startsWith("assign"))
        {
            return ASSIGN_ISSUE;
        }
        else if (perm.startsWith("resolv"))
        {
            return RESOLVE_ISSUE;
        }
        else if (perm.startsWith("close"))
        {
            return CLOSE_ISSUE;
        }
        else if (perm.startsWith("transition"))
        {
            return TRANSITION_ISSUE;
        }
        else if (perm.startsWith("worklogeditall"))
        {
            return WORKLOG_EDIT_ALL;
        }
        else if (perm.startsWith("worklogeditown"))
        {
            return WORKLOG_EDIT_OWN;
        }
        else if (perm.startsWith("worklogdeleteown"))
        {
            return WORKLOG_DELETE_OWN;
        }
        else if (perm.startsWith("worklogdeleteall"))
        {
            return WORKLOG_DELETE_ALL;
        }
        else if (perm.startsWith("work"))
        {
            return WORK_ISSUE;
        }
        else if (perm.startsWith("link"))
        {
            return LINK_ISSUE;
        }
        else if (perm.startsWith("delete"))
        {
            return DELETE_ISSUE;
        }
        else if (perm.startsWith("sharefilters"))
        {
            return CREATE_SHARED_OBJECTS;
        }
        else if (perm.startsWith("groupsubscriptions"))
        {
            return MANAGE_GROUP_FILTER_SUBSCRIPTIONS;
        }
        else if (perm.startsWith("move"))
        {
            return MOVE_ISSUE;
        }
        else if (perm.startsWith("setsecurity"))
        {
            return SET_ISSUE_SECURITY;
        }
        else if (perm.startsWith("pickusers"))
        {
            return USER_PICKER;
        }
        else if (perm.startsWith("viewversioncontrol"))
        {
            return VIEW_VERSION_CONTROL;
        }
        else if (perm.startsWith("modifyreporter"))
        {
            return MODIFY_REPORTER;
        }
        else if (perm.startsWith("viewvotersandwatchers"))
        {
            return VIEW_VOTERS_AND_WATCHERS;
        }
        else if (perm.startsWith("managewatcherlist"))
        {
            return MANAGE_WATCHER_LIST;
        }
        else if (perm.startsWith("bulkchange"))
        {
            return BULK_CHANGE;
        }
        else if (perm.startsWith("commenteditall"))
        {
            return COMMENT_EDIT_ALL;
        }
        else if (perm.startsWith("commenteditown"))
        {
            return COMMENT_EDIT_OWN;
        }
        else if (perm.startsWith("commentdeleteown"))
        {
            return COMMENT_DELETE_OWN;
        }
        else if (perm.startsWith("commentdeleteall"))
        {
            return COMMENT_DELETE_ALL;
        }
        else if (perm.startsWith("attachdeleteown"))
        {
            return ATTACHMENT_DELETE_OWN;
        }
        else if (perm.startsWith("attachdeleteall"))
        {
            return ATTACHMENT_DELETE_ALL;
        }
        else if (perm.startsWith("attach"))
        {
            return CREATE_ATTACHMENT;
        }
        else if (perm.startsWith("comment"))
        {
            return COMMENT_ISSUE;
        }
        else if (perm.startsWith("viewworkflowreadonly"))
        {
            return VIEW_WORKFLOW_READONLY;
        }

        return -1;
    }

    public static String getShortName(final int id)
    {
        switch (id)
        {
            case Permissions.ADMINISTER:
                return "admin";

            case Permissions.USE:
                return "use";

            case Permissions.SYSTEM_ADMIN:
                return "sysadmin";

            case Permissions.PROJECT_ADMIN:
                return "project";

            case Permissions.BROWSE:
                return "browse";

            case Permissions.CREATE_ISSUE:
                return "create";

            case Permissions.EDIT_ISSUE:
                return "edit";

            case Permissions.SCHEDULE_ISSUE:
                return "scheduleissue";

            case Permissions.ASSIGN_ISSUE:
                return "assign";

            case Permissions.ASSIGNABLE_USER:
                return "assignable";

            case Permissions.CREATE_ATTACHMENT:
                return "attach";

            case Permissions.RESOLVE_ISSUE:
                return "resolve";

            case Permissions.CLOSE_ISSUE:
                return "close";

            case Permissions.TRANSITION_ISSUE:
                return "transition";

            case Permissions.COMMENT_ISSUE:
                return "comment";

            case Permissions.DELETE_ISSUE:
                return "delete";

            case Permissions.WORK_ISSUE:
                return "work";

            case Permissions.WORKLOG_DELETE_ALL:
                return "worklogdeleteall";

            case Permissions.WORKLOG_DELETE_OWN:
                return "worklogdeleteown";

            case Permissions.WORKLOG_EDIT_ALL:
                return "worklogeditall";

            case Permissions.WORKLOG_EDIT_OWN:
                return "worklogeditown";

            case Permissions.LINK_ISSUE:
                return "link";

            case Permissions.CREATE_SHARED_OBJECTS:
                return "sharefilters";

            case Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS:
                return "groupsubscriptions";

            case Permissions.MOVE_ISSUE:
                return "move";

            case Permissions.SET_ISSUE_SECURITY:
                return "setsecurity";

            case Permissions.USER_PICKER:
                return "pickusers";

            case Permissions.VIEW_VERSION_CONTROL:
                return "viewversioncontrol";

            case Permissions.MODIFY_REPORTER:
                return "modifyreporter";

            case Permissions.VIEW_VOTERS_AND_WATCHERS:
                return "viewvotersandwatchers";

            case Permissions.MANAGE_WATCHER_LIST:
                return "managewatcherlist";

            case Permissions.BULK_CHANGE:
                return "bulkchange";

            case Permissions.COMMENT_EDIT_ALL:
                return "commenteditall";

            case Permissions.COMMENT_EDIT_OWN:
                return "commenteditown";

            case Permissions.COMMENT_DELETE_ALL:
                return "commentdeleteall";

            case Permissions.COMMENT_DELETE_OWN:
                return "commentdeleteown";

            case Permissions.ATTACHMENT_DELETE_ALL:
                return "attachdeleteall";

            case Permissions.ATTACHMENT_DELETE_OWN:
                return "attachdeleteown";

            case Permissions.VIEW_WORKFLOW_READONLY:
                return "viewworkflowreadonly";
        }

        return "";
    }

    /**
     * Get the description of a permission.
     * <p/>
     * Returns an empty string if the permission with given type cannot be found.
     *
     * @param permType permission type
     * @return description of a permission
     */
    public static String getDescription(final int permType)
    {
        switch (permType)
        {
            case Permissions.ADMINISTER:
            {
                return getText("admin.permissions.descriptions.ADMINISTER");
            }

            case Permissions.USE:
            {
                return getText("admin.permissions.descriptions.USE");
            }

            case Permissions.SYSTEM_ADMIN:
            {
                return getText("admin.permissions.descriptions.SYS_ADMIN");
            }

            case Permissions.PROJECT_ADMIN:
            {
                return getText("admin.permissions.descriptions.PROJECT_ADMIN");
            }

            case Permissions.BROWSE:
            {
                return getText("admin.permissions.descriptions.BROWSE");
            }

            case Permissions.CREATE_ISSUE:
            {
                return getText("admin.permissions.descriptions.CREATE_ISSUE");
            }

            case Permissions.EDIT_ISSUE:
            {
                return getText("admin.permissions.descriptions.EDIT_ISSUE");
            }

            case Permissions.SCHEDULE_ISSUE:
            {
                return getText("admin.permissions.descriptions.SCHEDULE_ISSUE");
            }

            case Permissions.ASSIGN_ISSUE:
            {
                return getText("admin.permissions.descriptions.ASSIGN_ISSUE");
            }

            case Permissions.ASSIGNABLE_USER:
            {
                return getText("admin.permissions.descriptions.ASSIGNABLE_USER");
            }

            case Permissions.CREATE_ATTACHMENT:
            {
                return getText("admin.permissions.descriptions.CREATE_ATTACHMENT");
            }

            case Permissions.RESOLVE_ISSUE:
            {
                return getText("admin.permissions.descriptions.RESOLVE_ISSUE");
            }

            case Permissions.CLOSE_ISSUE:
            {
                return getText("admin.permissions.descriptions.CLOSE_ISSUE");
            }

            case Permissions.TRANSITION_ISSUE:
            {
                return getText("admin.permissions.descriptions.TRANSITION_ISSUE");
            }

            case Permissions.COMMENT_ISSUE:
            {
                return getText("admin.permissions.descriptions.COMMENT_ISSUE");
            }

            case Permissions.DELETE_ISSUE:
            {
                return getText("admin.permissions.descriptions.DELETE_ISSUE");
            }

            case Permissions.WORK_ISSUE:
            {
                return getText("admin.permissions.descriptions.WORK_ISSUE");
            }

            case Permissions.WORKLOG_DELETE_ALL:
            {
                return getText("admin.permissions.descriptions.WORKLOG_DELETE_ALL");
            }

            case Permissions.WORKLOG_DELETE_OWN:
            {
                return getText("admin.permissions.descriptions.WORKLOG_DELETE_OWN");
            }

            case Permissions.WORKLOG_EDIT_ALL:
            {
                return getText("admin.permissions.descriptions.WORKLOG_EDIT_ALL");
            }

            case Permissions.WORKLOG_EDIT_OWN:
            {
                return getText("admin.permissions.descriptions.WORKLOG_EDIT_OWN");
            }

            case Permissions.LINK_ISSUE:
            {
                return getText("admin.permissions.descriptions.LINK_ISSUE");
            }

            case Permissions.CREATE_SHARED_OBJECTS:
            {
                return getText("admin.permissions.descriptions.CREATE_SHARED_OBJECTS");
            }

            case Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS:
            {
                return getText("admin.permissions.descriptions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS");
            }

            case Permissions.MOVE_ISSUE:
            {
                return getText("admin.permissions.descriptions.MOVE_ISSUE");
            }

            case Permissions.SET_ISSUE_SECURITY:
            {
                return getText("admin.permissions.descriptions.SET_ISSUE_SECURITY");
            }

            case Permissions.USER_PICKER:
            {
                return getText("admin.permissions.descriptions.USER_PICKER");
            }

            case Permissions.VIEW_VERSION_CONTROL:
            {
                return getText("admin.permissions.descriptions.VIEW_VERSION_CONTROL");
            }

            case Permissions.MODIFY_REPORTER:
            {
                return getText("admin.permissions.descriptions.MODIFY_REPORTER");
            }

            case Permissions.VIEW_VOTERS_AND_WATCHERS:
            {
                return getText("admin.permissions.descriptions.VIEW_VOTERS_AND_WATCHERS");
            }

            case Permissions.MANAGE_WATCHER_LIST:
            {
                return getText("admin.permissions.descriptions.MANAGE_WATCHER_LIST");
            }

            case Permissions.BULK_CHANGE:
            {
                return getText("admin.permissions.descriptions.BULK_CHANGE");
            }

            case Permissions.COMMENT_EDIT_ALL:
            {
                return getText("admin.permissions.descriptions.COMMENT_EDIT_ALL");
            }

            case Permissions.COMMENT_EDIT_OWN:
            {
                return getText("admin.permissions.descriptions.COMMENT_EDIT_OWN");
            }

            case Permissions.COMMENT_DELETE_ALL:
            {
                return getText("admin.permissions.descriptions.COMMENT_DELETE_ALL");
            }

            case Permissions.COMMENT_DELETE_OWN:
            {
                return getText("admin.permissions.descriptions.COMMENT_DELETE_OWN");
            }

            case Permissions.ATTACHMENT_DELETE_ALL:
            {
                return getText("admin.permissions.descriptions.ATTACHMENT_DELETE_ALL");
            }

            case Permissions.ATTACHMENT_DELETE_OWN:
            {
                return getText("admin.permissions.descriptions.ATTACHMENT_DELETE_OWN");
            }

            case Permissions.VIEW_WORKFLOW_READONLY:
            {
                return getText("admin.permissions.descriptions.WORKFLOW_VIEW_READONLY");
            }

        }

        return "";
    }

    /**
     * @deprecated This method is a shim. call {@link com.atlassian.jira.security.GlobalPermissionManager#isGlobalPermission(int)} instead.
     */
    public static boolean isGlobalPermission(final int permType)
    {
        return ComponentAccessor.getComponent(GlobalPermissionManager.class).isGlobalPermission(permType);
    }

    /**
     *
     * @param permType    permission Type
     * @return        true if the permission type is a SYSTEM_ADMIN or a has ADMINISTER privileges
     *
     * @deprecated No longer used. Also you should be using GlobalPermissionKey now instead of int.
     */
    public static boolean isAdministrativePermission(int permType)
    {
        return (permType == Permissions.ADMINISTER || permType == Permissions.SYSTEM_ADMIN);
    }

    private static String getText(final String key)
    {
        return ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText(key);
    }

    /**
     * Extremely Experimental API, do not use, it may change without notice...
     */
    @ExperimentalApi
    public enum Type
    {
        GLOBAL, PROJECT, ISSUE, VOTERS_AND_WATCHERS, TIME_TRACKING, COMMENTS, ATTATCHMENTS
    }

    /**
     * Experimental API, do not use, it may change without notice...
     *
     * Plans are afoot to add custom permissions ... at which point this enum would be core permissions only ...
     */
    @ExperimentalApi
    public enum Permission
    {
        ADMINISTER(0, Type.GLOBAL, "admin.global.permissions.administer", "admin.permissions.descriptions.ADMINISTER"),
        USE(1, Type.GLOBAL, "admin.global.permissions.use", "admin.permissions.descriptions.USE"),
        SYSTEM_ADMIN(44, Type.GLOBAL, "admin.global.permissions.system.administer", "admin.permissions.descriptions.SYS_ADMIN"),
        CREATE_SHARED_OBJECTS(22, Type.GLOBAL, "admin.global.permissions.create.shared.filter", "admin.permissions.descriptions.CREATE_SHARED_OBJECTS"),
        MANAGE_GROUP_FILTER_SUBSCRIPTIONS(24, Type.GLOBAL, "admin.global.permissions.manage.group.filter.subscriptions", "admin.permissions.descriptions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS"),
        BULK_CHANGE(33, Type.GLOBAL, "admin.global.permissions.bulk.change", "admin.permissions.descriptions.BULK_CHANGE"),
        /**
         * Ability to select a user or group from a popup window. Users with this permission will be able to see names
         * of all users and groups in the system.
         */
        USER_PICKER(27, Type.GLOBAL, "admin.global.permissions.user.picker", "admin.permissions.descriptions.USER_PICKER"),

        PROJECT_ADMIN(23, Type.PROJECT, "admin.permissions.PROJECT_ADMIN", "admin.permissions.descriptions.PROJECT_ADMIN"),
        BROWSE(10, Type.PROJECT, "admin.permissions.BROWSE", "admin.permissions.descriptions.BROWSE"),
        VIEW_VERSION_CONTROL(29, Type.PROJECT, "admin.permissions.VIEW_VERSION_CONTROL", "admin.permissions.descriptions.VIEW_VERSION_CONTROL"),
        VIEW_WORKFLOW_READONLY(45, Type.PROJECT, "admin.permissions.VIEW_WORKFLOW_READONLY", "admin.permissions.descriptions.VIEW_WORKFLOW_READONLY"),

        CREATE_ISSUE (11, Type.ISSUE, "admin.permissions.CREATE_ISSUE", "admin.permissions.descriptions.CREATE_ISSUE"),
        EDIT_ISSUE (12, Type.ISSUE, "admin.permissions.EDIT_ISSUE", "admin.permissions.descriptions.EDIT_ISSUE"),
        ASSIGN_ISSUE (13, Type.ISSUE, "admin.permissions.ASSIGN_ISSUE", "admin.permissions.descriptions.ASSIGN_ISSUE"),
        RESOLVE_ISSUE (14, Type.ISSUE, "admin.permissions.RESOLVE_ISSUE", "admin.permissions.descriptions.RESOLVE_ISSUE"),
        DELETE_ISSUE (16, Type.ISSUE, "admin.permissions.DELETE_ISSUE", "admin.permissions.descriptions.DELETE_ISSUE"),
        ASSIGNABLE_USER (17, Type.ISSUE, "admin.permissions.ASSIGNABLE_USER", "admin.permissions.descriptions.ASSIGNABLE_USER"), // permission to be assigned issues
        CLOSE_ISSUE (18, Type.ISSUE, "admin.permissions.CLOSE_ISSUE", "admin.permissions.descriptions.CLOSE_ISSUE"),
        TRANSITION_ISSUE (Permissions.TRANSITION_ISSUE, Type.ISSUE, "admin.permissions.TRANSITION_ISSUE", "admin.permissions.descriptions.TRANSITION_ISSUE"),
        LINK_ISSUE (21, Type.ISSUE, "admin.permissions.LINK_ISSUE", "admin.permissions.descriptions.LINK_ISSUE"),
        MOVE_ISSUE (25, Type.ISSUE, "admin.permissions.MOVE_ISSUE", "admin.permissions.descriptions.MOVE_ISSUE"),
        SET_ISSUE_SECURITY (26, Type.ISSUE, "admin.permissions.SET_ISSUE_SECURITY", "admin.permissions.descriptions.SET_ISSUE_SECURITY"),
        SCHEDULE_ISSUE (28, Type.ISSUE, "admin.permissions.SCHEDULE_ISSUE", "admin.permissions.descriptions.SCHEDULE_ISSUE"),
        MODIFY_REPORTER (30, Type.ISSUE, "admin.permissions.MODIFY_REPORTER", "admin.permissions.descriptions.MODIFY_REPORTER"),

        VIEW_VOTERS_AND_WATCHERS(31, Type.VOTERS_AND_WATCHERS, "admin.permissions.VIEW_VOTERS_AND_WATCHERS", "admin.permissions.descriptions.VIEW_VOTERS_AND_WATCHERS"),
        MANAGE_WATCHER_LIST (32, Type.VOTERS_AND_WATCHERS, "admin.permissions.MANAGE_WATCHER_LIST", "admin.permissions.descriptions.MANAGE_WATCHER_LIST"),

        COMMENT_ISSUE(15, Type.COMMENTS, "admin.permissions.COMMENT_ISSUE", "admin.permissions.descriptions.COMMENT_ISSUE"),
        COMMENT_EDIT_ALL(34, Type.COMMENTS, "admin.permissions.COMMENT_EDIT_ALL", "admin.permissions.descriptions.COMMENT_EDIT_ALL"),
        COMMENT_EDIT_OWN(35, Type.COMMENTS, "admin.permissions.COMMENT_EDIT_OWN", "admin.permissions.descriptions.COMMENT_EDIT_OWN"),
        COMMENT_DELETE_ALL (36, Type.COMMENTS, "admin.permissions.COMMENT_DELETE_ALL", "admin.permissions.descriptions.COMMENT_DELETE_ALL"),
        COMMENT_DELETE_OWN (37, Type.COMMENTS, "admin.permissions.COMMENT_DELETE_OWN", "admin.permissions.descriptions.COMMENT_DELETE_OWN"),

        CREATE_ATTACHMENT(19, Type.ATTATCHMENTS, "admin.permissions.CREATE_ATTACHMENT", "admin.permissions.descriptions.CREATE_ATTACHMENT"),
        ATTACHMENT_DELETE_ALL (38, Type.ATTATCHMENTS, "admin.permissions.ATTACHMENT_DELETE_ALL", "admin.permissions.descriptions.ATTACHMENT_DELETE_ALL"),
        ATTACHMENT_DELETE_OWN (39, Type.ATTATCHMENTS, "admin.permissions.ATTACHMENT_DELETE_OWN", "admin.permissions.descriptions.ATTACHMENT_DELETE_OWN"),

        WORK_ISSUE(20, Type.TIME_TRACKING, "admin.permissions.WORK_ISSUE", "admin.permissions.descriptions.WORK_ISSUE"), // Permission to log work done on an issue.
        WORKLOG_EDIT_OWN(40, Type.TIME_TRACKING, "admin.permissions.WORKLOG_EDIT_OWN", "admin.permissions.descriptions.WORKLOG_EDIT_OWN"),
        WORKLOG_EDIT_ALL(41, Type.TIME_TRACKING, "admin.permissions.WORKLOG_EDIT_ALL", "admin.permissions.descriptions.WORKLOG_EDIT_ALL"),
        WORKLOG_DELETE_OWN(42, Type.TIME_TRACKING, "admin.permissions.WORKLOG_DELETE_OWN", "admin.permissions.descriptions.WORKLOG_DELETE_OWN"),
        WORKLOG_DELETE_ALL(43, Type.TIME_TRACKING, "admin.permissions.WORKLOG_DELETE_ALL", "admin.permissions.descriptions.WORKLOG_DELETE_ALL");

        private int id;
        private Type type;
        private String nameKey;
        private String descriptionKey;

        Permission(int id, Type type, String nameKey, String descriptionKey)
        {
            this.id = id;
            this.type = type;
            this.nameKey = nameKey;
            this.descriptionKey = descriptionKey;
        }

        public int getId()
        {
            return id;
        }

        public Type getType()
        {
            return type;
        }

        public String getNameKey()
        {
            return nameKey;
        }

        public String getDescriptionKey()
        {
            return descriptionKey;
        }
    }
}

