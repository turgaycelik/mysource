package com.atlassian.jira.scheme;

/**
 * Given a string which represents the SchemeManager type this will return you an instance of the SchemeManager you
 * are looking for.
 */
public interface SchemeManagerFactory
{
    public static final String PERMISSION_SCHEME_MANAGER = "PermissionScheme";
    public static final String NOTIFICATION_SCHEME_MANAGER = "NotificationScheme";
    public static final String WORKFLOW_SCHEME_MANAGER = "WorkflowScheme";
    public static final String ISSUE_SECURITY_SCHEME_MANAGER = "IssueSecurityScheme";

    public SchemeManager getSchemeManager(String managerType);
}
