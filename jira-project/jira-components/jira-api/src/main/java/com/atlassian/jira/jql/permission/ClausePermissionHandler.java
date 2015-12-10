package com.atlassian.jira.jql.permission;

/**
 * A composite interface that deals with clause sanitisation and permission checking. An instance of this should be
 * registered against each clause in the system.
 *
 * @see com.atlassian.jira.jql.permission.ClauseSanitiser
 * @see com.atlassian.jira.jql.permission.ClausePermissionChecker
 * @since v4.0
 */
public interface ClausePermissionHandler extends ClauseSanitiser, ClausePermissionChecker
{
}
