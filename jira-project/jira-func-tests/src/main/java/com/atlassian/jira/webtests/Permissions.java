package com.atlassian.jira.webtests;

/**
 * Mirrors JIRA's com.atlassian.jira.security.Permissions class (necessary as func tests do not depend on JIRA)
 */
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
     * Permission to created shared &quot;objects&quot; (filters / dashboards).
     */
    public static final int CREATE_SHARED_OBJECT = 22;

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

    public static final int TRANSITION_ISSUE = 46;

    public static final int MAX_PERMISSION = 46;
}
