package com.atlassian.jira.config.util;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

/**
 * @since v4.0
 */
public class DefaultIndexPathService implements IndexPathService
{
    private final IndexPathManager indexPathManager;
    private final PermissionManager permissionManager;

    public DefaultIndexPathService(final IndexPathManager indexPathManager, final PermissionManager permissionManager)
    {
        this.indexPathManager = indexPathManager;
        this.permissionManager = permissionManager;
    }

    public String getIndexRootPath(final JiraServiceContext serviceContext)
    {
        if (permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, serviceContext.getLoggedInUser()))
        {
            return indexPathManager.getIndexRootPath();
        }
        else
        {
            serviceContext.getErrorCollection().addErrorMessage(serviceContext.getI18nBean().getText("admin.errors.indexpath.no.permission"));
            return null;
        }
    }

    public String getIssueIndexPath(final JiraServiceContext serviceContext)
    {
        if (permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, serviceContext.getLoggedInUser()))
        {
            return indexPathManager.getIssueIndexPath();
        }
        else
        {
            serviceContext.getErrorCollection().addErrorMessage(serviceContext.getI18nBean().getText("admin.errors.indexpath.no.permission"));
            return null;
        }
    }

    public String getCommentIndexPath(final JiraServiceContext serviceContext)
    {
        if (permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, serviceContext.getLoggedInUser()))
        {
            return indexPathManager.getCommentIndexPath();
        }
        else
        {
            serviceContext.getErrorCollection().addErrorMessage(serviceContext.getI18nBean().getText("admin.errors.indexpath.no.permission"));
            return null;
        }
    }

    public String getPluginIndexRootPath(final JiraServiceContext serviceContext)
    {
        if (permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, serviceContext.getLoggedInUser()))
        {
            return indexPathManager.getPluginIndexRootPath();
        }
        else
        {
            serviceContext.getErrorCollection().addErrorMessage(serviceContext.getI18nBean().getText("admin.errors.indexpath.no.permission"));
            return null;
        }
    }

    public String getSharedEntityIndexPath(final JiraServiceContext serviceContext)
    {
        if (permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, serviceContext.getLoggedInUser()))
        {
            return indexPathManager.getSharedEntityIndexPath();
        }
        else
        {
            serviceContext.getErrorCollection().addErrorMessage(serviceContext.getI18nBean().getText("admin.errors.indexpath.no.permission"));
            return null;
        }
    }

    public void setIndexRootPath(final JiraServiceContext serviceContext, final String indexPath)
    {
        if (permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, serviceContext.getLoggedInUser()))
        {
            indexPathManager.setIndexRootPath(indexPath);
        }
        else
        {
            serviceContext.getErrorCollection().addErrorMessage(serviceContext.getI18nBean().getText("admin.errors.indexpath.no.permission"));
        }

    }

    public void setUseDefaultDirectory(final JiraServiceContext serviceContext)
    {
        if (permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, serviceContext.getLoggedInUser()))
        {
            indexPathManager.setUseDefaultDirectory();
        }
        else
        {
            serviceContext.getErrorCollection().addErrorMessage(serviceContext.getI18nBean().getText("admin.errors.indexpath.no.permission"));
        }
    }
}
