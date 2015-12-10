package com.atlassian.jira.scheme;

import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;

/**
 * Impl for SchemeManagerFactory.
 */
public class DefaultSchemeManagerFactory implements SchemeManagerFactory
{
    private NotificationSchemeManager notificationSchemeManager;
    private PermissionSchemeManager permissionSchemeManager;
    private WorkflowSchemeManager workflowSchemeManager;
    private IssueSecuritySchemeManager issueSecuritySchemeManager;

    public DefaultSchemeManagerFactory(NotificationSchemeManager notificationSchemeManager,
                                       PermissionSchemeManager permissionSchemeManager,
                                       WorkflowSchemeManager workflowSchemeManager,
                                       IssueSecuritySchemeManager issueSecuritySchemeManager)
    {

        this.notificationSchemeManager = notificationSchemeManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.workflowSchemeManager = workflowSchemeManager;
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
    }


    public SchemeManager getSchemeManager(String managerType)
    {
        if (SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER.equals(managerType))
        {
            return notificationSchemeManager;
        }
        else if (SchemeManagerFactory.PERMISSION_SCHEME_MANAGER.equals(managerType))
        {
            return permissionSchemeManager;
        }
        else if (SchemeManagerFactory.WORKFLOW_SCHEME_MANAGER.equals(managerType))
        {
            return workflowSchemeManager;
        }
        else if (SchemeManagerFactory.ISSUE_SECURITY_SCHEME_MANAGER.equals(managerType))
        {
            return issueSecuritySchemeManager;
        }
        throw new IllegalArgumentException("This factory can not create a scheme manager for the type: " + managerType);
    }
}
