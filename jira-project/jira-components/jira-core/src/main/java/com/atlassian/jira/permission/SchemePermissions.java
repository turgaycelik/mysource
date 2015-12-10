/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.permission;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import static com.atlassian.jira.permission.Permission.ASSIGNABLE_USER;
import static com.atlassian.jira.permission.Permission.ASSIGN_ISSUE;
import static com.atlassian.jira.permission.Permission.ATTACHMENT_DELETE_ALL;
import static com.atlassian.jira.permission.Permission.ATTACHMENT_DELETE_OWN;
import static com.atlassian.jira.permission.Permission.BROWSE;
import static com.atlassian.jira.permission.Permission.CLOSE_ISSUE;
import static com.atlassian.jira.permission.Permission.COMMENT_DELETE_ALL;
import static com.atlassian.jira.permission.Permission.COMMENT_DELETE_OWN;
import static com.atlassian.jira.permission.Permission.COMMENT_EDIT_ALL;
import static com.atlassian.jira.permission.Permission.COMMENT_EDIT_OWN;
import static com.atlassian.jira.permission.Permission.COMMENT_ISSUE;
import static com.atlassian.jira.permission.Permission.CREATE_ATTACHMENT;
import static com.atlassian.jira.permission.Permission.CREATE_ISSUE;
import static com.atlassian.jira.permission.Permission.DELETE_ISSUE;
import static com.atlassian.jira.permission.Permission.EDIT_ISSUE;
import static com.atlassian.jira.permission.Permission.LINK_ISSUE;
import static com.atlassian.jira.permission.Permission.MANAGE_WATCHER_LIST;
import static com.atlassian.jira.permission.Permission.MODIFY_REPORTER;
import static com.atlassian.jira.permission.Permission.MOVE_ISSUE;
import static com.atlassian.jira.permission.Permission.PROJECT_ADMIN;
import static com.atlassian.jira.permission.Permission.RESOLVE_ISSUE;
import static com.atlassian.jira.permission.Permission.SCHEDULE_ISSUE;
import static com.atlassian.jira.permission.Permission.SET_ISSUE_SECURITY;
import static com.atlassian.jira.permission.Permission.TRANSITION_ISSUE;
import static com.atlassian.jira.permission.Permission.VIEW_VERSION_CONTROL;
import static com.atlassian.jira.permission.Permission.VIEW_VOTERS_AND_WATCHERS;
import static com.atlassian.jira.permission.Permission.VIEW_WORKFLOW_READONLY;
import static com.atlassian.jira.permission.Permission.WORKLOG_DELETE_ALL;
import static com.atlassian.jira.permission.Permission.WORKLOG_DELETE_OWN;
import static com.atlassian.jira.permission.Permission.WORKLOG_EDIT_ALL;
import static com.atlassian.jira.permission.Permission.WORKLOG_EDIT_OWN;
import static com.atlassian.jira.permission.Permission.WORK_ISSUE;

/**
 * This class gets a list of all the permissions that can be part of a permission scheme. We want those permissions to be
 * explicitly ordered as that influences the UI. Hence the manual initialization of specific maps.
 *
 * TODO: delete this class (see SOFT-80)
 *
 * @deprecated Use {@link com.atlassian.jira.security.PermissionManager}.
 */
@Deprecated
public class SchemePermissions
{
    private ImmutableMap<Integer, Permission> projectPermissions = toMap(
            PROJECT_ADMIN,
            BROWSE,
            VIEW_VERSION_CONTROL,
            VIEW_WORKFLOW_READONLY);
    private ImmutableMap<Integer, Permission> issuePermissions = toMap(
            CREATE_ISSUE,
            EDIT_ISSUE,
            TRANSITION_ISSUE,
            SCHEDULE_ISSUE,
            MOVE_ISSUE,
            ASSIGN_ISSUE,
            ASSIGNABLE_USER,
            RESOLVE_ISSUE,
            CLOSE_ISSUE,
            MODIFY_REPORTER,
            DELETE_ISSUE,
            LINK_ISSUE,
            SET_ISSUE_SECURITY);
    private ImmutableMap<Integer, Permission> votersAndWatchersPermissions = toMap(
            VIEW_VOTERS_AND_WATCHERS,
            MANAGE_WATCHER_LIST);
    private ImmutableMap<Integer, Permission> commentsPermissions = toMap(
            COMMENT_ISSUE,
            COMMENT_EDIT_ALL,
            COMMENT_EDIT_OWN,
            COMMENT_DELETE_ALL,
            COMMENT_DELETE_OWN);
    private ImmutableMap<Integer, Permission> attachmentsPermissions = toMap(
            CREATE_ATTACHMENT,
            ATTACHMENT_DELETE_ALL,
            ATTACHMENT_DELETE_OWN);
    private ImmutableMap<Integer, Permission> timeTrackingPermissions = toMap(
            WORK_ISSUE,
            WORKLOG_EDIT_OWN,
            WORKLOG_EDIT_ALL,
            WORKLOG_DELETE_OWN,
            WORKLOG_DELETE_ALL);
    private ImmutableMap<Integer, Permission> permissions = ImmutableMap.<Integer, Permission>builder()
                                                                        .putAll(projectPermissions)
                                                                        .putAll(issuePermissions)
                                                                        .putAll(votersAndWatchersPermissions)
                                                                        .putAll(commentsPermissions)
                                                                        .putAll(attachmentsPermissions)
                                                                        .putAll(timeTrackingPermissions)
                                                                        .build();

    private static ImmutableMap<Integer, Permission> toMap(Permission... permissions)
    {
        final ImmutableMap.Builder<Integer, Permission> map = ImmutableMap.builder();
        for(final Permission permission : permissions)
        {
            map.put(permission.getId(), permission);
        }
        return map.build();
    }

    /**
     * @return Map of project related Permissions keyed by their ID
     */
    public Map<Integer, Permission> getProjectPermissions()
    {
        return projectPermissions;
    }

    /**
     * @return Map of issue related Permissions keyed by their ID
     */
    public Map<Integer, Permission> getIssuePermissions()
    {
        return issuePermissions;
    }

    /**
     * @return Map of voters/watchers related Permissions keyed by their ID
     */
    public Map<Integer, Permission> getVotersAndWatchersPermissions()
    {
        return votersAndWatchersPermissions;
    }

    /**
     * @return Map of comment related Permissions keyed by their ID
     */
    public Map<Integer, Permission> getCommentsPermissions()
    {
        return commentsPermissions;
    }

    /**
     * @return Map of attachment related Permissions keyed by their ID
     */
    public Map<Integer, Permission> getAttachmentsPermissions()
    {
        return attachmentsPermissions;
    }

    /**
     * @return Map of time tracking related Permissions keyed by their ID
     */
    public Map<Integer, Permission> getTimeTrackingPermissions()
    {
        return timeTrackingPermissions;
    }

    /**
     * Get a map of the permissions that can be part of a permission scheme. This map contains the permission id and the permission name
     * @return Map containing the permissions
     */
    public Map<Integer, Permission> getSchemePermissions()
    {
        return permissions;
    }

    /**
     * Checks to see if the permission exists
     * @param id The permission Id
     * @return True / False
     */
    public boolean schemePermissionExists(final Integer id)
    {
        return getSchemePermissions().containsKey(id);
    }

    /**
     * Get the name of the permission
     * @param id The permission Id
     * @return The name of the permission
     */
    public String getPermissionName(final Integer id)
    {
        return getSchemePermissions().get(id).getNameKey();
    }

    /**
     * Gets the description for the permission
     * @param id Id of the permission that you want to get the description for
     * @return String containing the description
     */
    public String getPermissionDescription(final int id)
    {
        if (getSchemePermissions().get(id) != null)
        {
            return getSchemePermissions().get(id).getDescription();
        }
        return "";
    }
}
