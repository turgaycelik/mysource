package com.atlassian.jira.permission;

import com.atlassian.jira.security.plugin.ProjectPermissionKey;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * This class contains constants representing keys of built-in system project permissions
 * as well as utility methods related to project permissions.
 *
 * @since v6.2.5
 */
public class ProjectPermissions
{
    public static final ProjectPermissionKey ADMINISTER_PROJECTS = new ProjectPermissionKey("ADMINISTER_PROJECTS");

    public static final ProjectPermissionKey BROWSE_PROJECTS = new ProjectPermissionKey("BROWSE_PROJECTS");

    public static final ProjectPermissionKey VIEW_DEV_TOOLS = new ProjectPermissionKey("VIEW_DEV_TOOLS");

    public static final ProjectPermissionKey VIEW_READONLY_WORKFLOW = new ProjectPermissionKey("VIEW_READONLY_WORKFLOW");

    public static final ProjectPermissionKey CREATE_ISSUES = new ProjectPermissionKey("CREATE_ISSUES");

    public static final ProjectPermissionKey EDIT_ISSUES = new ProjectPermissionKey("EDIT_ISSUES");

    public static final ProjectPermissionKey TRANSITION_ISSUES = new ProjectPermissionKey("TRANSITION_ISSUES");

    public static final ProjectPermissionKey SCHEDULE_ISSUES = new ProjectPermissionKey("SCHEDULE_ISSUES");

    public static final ProjectPermissionKey MOVE_ISSUES = new ProjectPermissionKey("MOVE_ISSUES");

    public static final ProjectPermissionKey ASSIGN_ISSUES = new ProjectPermissionKey("ASSIGN_ISSUES");

    public static final ProjectPermissionKey ASSIGNABLE_USER = new ProjectPermissionKey("ASSIGNABLE_USER");

    public static final ProjectPermissionKey RESOLVE_ISSUES = new ProjectPermissionKey("RESOLVE_ISSUES");

    public static final ProjectPermissionKey CLOSE_ISSUES = new ProjectPermissionKey("CLOSE_ISSUES");

    public static final ProjectPermissionKey MODIFY_REPORTER = new ProjectPermissionKey("MODIFY_REPORTER");

    public static final ProjectPermissionKey DELETE_ISSUES = new ProjectPermissionKey("DELETE_ISSUES");

    public static final ProjectPermissionKey LINK_ISSUES = new ProjectPermissionKey("LINK_ISSUES");

    public static final ProjectPermissionKey SET_ISSUE_SECURITY = new ProjectPermissionKey("SET_ISSUE_SECURITY");

    public static final ProjectPermissionKey VIEW_VOTERS_AND_WATCHERS = new ProjectPermissionKey("VIEW_VOTERS_AND_WATCHERS");

    public static final ProjectPermissionKey MANAGE_WATCHERS = new ProjectPermissionKey("MANAGE_WATCHERS");

    public static final ProjectPermissionKey ADD_COMMENTS = new ProjectPermissionKey("ADD_COMMENTS");

    public static final ProjectPermissionKey EDIT_ALL_COMMENTS = new ProjectPermissionKey("EDIT_ALL_COMMENTS");

    public static final ProjectPermissionKey EDIT_OWN_COMMENTS = new ProjectPermissionKey("EDIT_OWN_COMMENTS");

    public static final ProjectPermissionKey DELETE_ALL_COMMENTS = new ProjectPermissionKey("DELETE_ALL_COMMENTS");

    public static final ProjectPermissionKey DELETE_OWN_COMMENTS = new ProjectPermissionKey("DELETE_OWN_COMMENTS");

    public static final ProjectPermissionKey CREATE_ATTACHMENTS = new ProjectPermissionKey("CREATE_ATTACHMENTS");

    public static final ProjectPermissionKey DELETE_ALL_ATTACHMENTS = new ProjectPermissionKey("DELETE_ALL_ATTACHMENTS");

    public static final ProjectPermissionKey DELETE_OWN_ATTACHMENTS = new ProjectPermissionKey("DELETE_OWN_ATTACHMENTS");

    public static final ProjectPermissionKey WORK_ON_ISSUES = new ProjectPermissionKey("WORK_ON_ISSUES");

    public static final ProjectPermissionKey EDIT_OWN_WORKLOGS = new ProjectPermissionKey("EDIT_OWN_WORKLOGS");

    public static final ProjectPermissionKey EDIT_ALL_WORKLOGS = new ProjectPermissionKey("EDIT_ALL_WORKLOGS");

    public static final ProjectPermissionKey DELETE_OWN_WORKLOGS = new ProjectPermissionKey("DELETE_OWN_WORKLOGS");

    public static final ProjectPermissionKey DELETE_ALL_WORKLOGS = new ProjectPermissionKey("DELETE_ALL_WORKLOGS");

    /**
     * Historically system project permissions had short name aliases that are used in
     * workflow condition/validator descriptors, gadget configurations etc. This method
     * looks up a system project permission key that corresponds to such short name.
     *
     * @param name Short name of a system project permission.
     * @return a key corresponding to the system permission with the specified short name.
     *      Null if there is no system permission with the matching short name.
     * @since v6.2.5
     */
    public static ProjectPermissionKey systemProjectPermissionKeyByShortName(String name)
    {
        if (isBlank(name))
        {
            return null;
        }

        name = name.toLowerCase();

        if (name.startsWith("project"))
        {
            return ADMINISTER_PROJECTS;
        }
        if (name.startsWith("browse"))
        {
            return BROWSE_PROJECTS;
        }
        if (name.startsWith("viewversioncontrol"))
        {
            return VIEW_DEV_TOOLS;
        }
        if (name.startsWith("viewworkflowreadonly"))
        {
            return VIEW_READONLY_WORKFLOW;
        }
        if (name.startsWith("create"))
        {
            return CREATE_ISSUES;
        }
        if (name.startsWith("edit"))
        {
            return EDIT_ISSUES;
        }
        if (name.startsWith("transition"))
        {
            return TRANSITION_ISSUES;
        }
        if (name.startsWith("scheduleissue"))
        {
            return SCHEDULE_ISSUES;
        }
        if (name.startsWith("move"))
        {
            return MOVE_ISSUES;
        }
        if (name.startsWith("assignable"))
        {
            return ASSIGNABLE_USER;
        }
        if (name.startsWith("assign"))
        {
            return ASSIGN_ISSUES;
        }
        if (name.startsWith("resolve"))
        {
            return RESOLVE_ISSUES;
        }
        if (name.startsWith("close"))
        {
            return CLOSE_ISSUES;
        }
        if (name.startsWith("modifyreporter"))
        {
            return MODIFY_REPORTER;
        }
        if (name.startsWith("delete"))
        {
            return DELETE_ISSUES;
        }
        if (name.startsWith("link"))
        {
            return LINK_ISSUES;
        }
        if (name.startsWith("setsecurity"))
        {
            return SET_ISSUE_SECURITY;
        }
        if (name.startsWith("viewvotersandwatchers"))
        {
            return VIEW_VOTERS_AND_WATCHERS;
        }
        if (name.startsWith("managewatcherlist"))
        {
            return MANAGE_WATCHERS;
        }
        if (name.startsWith("commenteditall"))
        {
            return EDIT_ALL_COMMENTS;
        }
        if (name.startsWith("commenteditown"))
        {
            return EDIT_OWN_COMMENTS;
        }
        if (name.startsWith("commentdeleteall"))
        {
            return DELETE_ALL_COMMENTS;
        }
        if (name.startsWith("commentdeleteown"))
        {
            return DELETE_OWN_COMMENTS;
        }
        if (name.startsWith("comment"))
        {
            return ADD_COMMENTS;
        }
        if (name.startsWith("attachdeleteall"))
        {
            return DELETE_ALL_ATTACHMENTS;
        }
        if (name.startsWith("attachdeleteown"))
        {
            return DELETE_OWN_ATTACHMENTS;
        }
        if (name.startsWith("attach"))
        {
            return CREATE_ATTACHMENTS;
        }
        if (name.startsWith("worklogeditown"))
        {
            return EDIT_OWN_WORKLOGS;
        }
        if (name.startsWith("worklogeditall"))
        {
            return EDIT_ALL_WORKLOGS;
        }
        if (name.startsWith("worklogdeleteown"))
        {
            return DELETE_OWN_WORKLOGS;
        }
        if (name.startsWith("worklogdeleteall"))
        {
            return DELETE_ALL_WORKLOGS;
        }
        if (name.startsWith("work"))
        {
            return WORK_ON_ISSUES;
        }

        return null;
    }
}
