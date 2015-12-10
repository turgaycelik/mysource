/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.IssueActionSupport;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkEditBeanSessionHelper;
import com.atlassian.jira.web.component.ColumnLayoutItemProvider;
import com.atlassian.jira.web.component.IssueTableLayoutBean;
import com.google.common.annotations.VisibleForTesting;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.List;

public class AbstractBulkOperationAction extends IssueActionSupport
{
    private final SearchService searchService;
    private final BulkEditBeanSessionHelper bulkEditBeanSessionHelper;

    public AbstractBulkOperationAction(final SearchService searchService,
                                       final BulkEditBeanSessionHelper bulkEditBeanSessionHelper)
    {
        this.searchService = searchService;
        this.bulkEditBeanSessionHelper = bulkEditBeanSessionHelper;
    }

    public BulkEditBean getBulkEditBean()
    {
        return getRootBulkEditBean();
    }

    public List getColumns() throws Exception
    {
        return getColumnsProvider().getColumns(getLoggedInUser(), getSearchRequest());
    }

    @VisibleForTesting
    ColumnLayoutItemProvider getColumnsProvider()
    {
        return new ColumnLayoutItemProvider();
    }

    public IssueTableLayoutBean getIssueTableLayoutBean() throws Exception
    {
        IssueTableLayoutBean layoutBean = new IssueTableLayoutBean(getColumns());
        layoutBean.setSortingEnabled(false);
        return layoutBean;
    }

    protected void clearBulkEditBean()
    {
        bulkEditBeanSessionHelper.removeFromSession();
    }

    protected String finishWizard() throws Exception
    {
        clearBulkEditBean();
        return getRedirect(getRedirectUrl());
    }

    protected String watchProgress(String progressUrl) throws Exception
    {
        getRootBulkEditBean().setRedirectUrl(getRedirectUrl());
        return getRedirect(progressUrl);
    }

    protected String getRedirectUrl() throws Exception
    {
        String finishedUrl = "/secure/IssueNavigator.jspa";
        BulkEditBean editBean = getRootBulkEditBean();
        if (editBean != null && editBean.isSingleMode())
        {
            finishedUrl = "/browse/" + editBean.getSingleIssueKey();
        }
        return finishedUrl;
    }

    protected void storeBulkEditBean(BulkEditBean bulkEditBean)
    {
        bulkEditBeanSessionHelper.storeToSession(bulkEditBean);
    }

    public BulkEditBean getRootBulkEditBean()
    {
        final BulkEditBean bean = bulkEditBeanSessionHelper.getFromSession();
        if (bean == null)
        {
            log.warn(
                    "Bulk edit bean unexpectedly null. Perhaps session was lost (e.g. when URL used is different to base URL in General Configuration)?");
        }
        return bean;
    }

    /**
     * Determines if the current user can disable mail notifications for this bulk operation.
     * <p/>
     * Only global admins or a user who is a project admin of all projects associated with the selected issues
     * can disable bulk mail notifications.
     *
     * @return true     if the user is a global admin or a project admin of all projects associated with the selected issues.
     */
    public boolean isCanDisableMailNotifications()
    {
        // Check for global admin permission
        if (hasPermission(Permissions.ADMINISTER))
        {
            return true;
        }
        // Check for project admin permission on all projects from selected issue collection
        else
        {
            Collection<Project> projects = getBulkEditBean().getProjectObjects();

            for (Project project : projects)
            {
                if (!hasProjectPermission(Permissions.PROJECT_ADMIN, project))
                {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean isSendBulkNotification()
    {
        return getBulkEditBean().isSendBulkNotification();
    }

    public void setSendBulkNotification(boolean sendBulkNotification)
    {
        if (getBulkEditBean() != null)
        {
            getBulkEditBean().setSendBulkNotification(sendBulkNotification);
        }
    }

    protected String redirectToStart()
    {
        ActionContext.getSession()
                .put(SessionKeys.SESSION_TIMEOUT_MESSAGE, getText("bulk.operation.session.timeout.message"));
        return getRedirect("SessionTimeoutMessage.jspa");
    }
}
