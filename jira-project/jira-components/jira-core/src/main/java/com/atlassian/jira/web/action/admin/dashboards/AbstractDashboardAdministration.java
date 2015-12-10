package com.atlassian.jira.web.action.admin.dashboards;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;

import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;

/**
 * This is the base class for DeleteSharedDashboard and ChangeSharedDashboardOwner
 *
 * @since v4.4
 */
public abstract class AbstractDashboardAdministration extends JiraWebActionSupport
{
    private PortalPage dashboard;
    private Long dashboardId;
    private final PermissionManager permissionManager;
    private final PortalPageManager portalPageManager;
    private String searchName;
    private String searchOwnerUserName;
    private String sortColumn;
    private String sortAscending;
    private String pagingOffset;
    private String totalResultCount;

    public AbstractDashboardAdministration(PermissionManager permissionManager, PortalPageManager portalPageManager)
    {
        this.permissionManager = permissionManager;
        this.portalPageManager = portalPageManager;
    }

    /**
     * This will always return the Dashboard using the dashboard context - this means you will (should?) never get Permission
     * exceptions
     * @return  the {@link com.atlassian.jira.issue.search.SearchRequest} that represents the current dashboardId
     */

    protected PortalPage getDashboard()
    {
        if ((dashboard == null) && (getDashboardId() != null))
        {
            dashboard = portalPageManager.getPortalPageById(getDashboardId());
        }
        return dashboard;
    }


    protected void setDashboard(PortalPage dashboard)
    {
        this.dashboard = dashboard;
    }

    public Long getDashboardId()
    {
        return dashboardId;
    }

    public void setDashboardId(final Long dashboardId)
    {
        this.dashboardId = dashboardId;
    }

    public void setPageId(final Long pageId)
    {
        this.dashboardId = pageId;
    }

    public String getDashboardName() throws GenericEntityException
    {
        final PortalPage dashboard = getDashboard();
        return (dashboard == null) ? null : dashboard.getName();
    }

    // need these to sahere with delteportalpage.jsp
    public String getPageName() throws Exception
    {
        return getDashboardName();
    }

    public Long getPageId()
    {
        return getDashboardId();
    }

    public String getSearchName()
    {
        return searchName;
    }

    public void setSearchName(String searchName)
    {
        this.searchName = searchName;
    }

    public String getSearchOwnerUserName()
    {
        return searchOwnerUserName;
    }

    public void setSearchOwnerUserName(String searchOwnerUserName)
    {
        this.searchOwnerUserName = searchOwnerUserName;
    }

    public String getSortColumn()
    {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn)
    {
        this.sortColumn = sortColumn;
    }

    public String getSortAscending()
    {
        return sortAscending;
    }

    public void setSortAscending(String sortAscending)
    {
        this.sortAscending = sortAscending;
    }

    public String getPagingOffset()
    {
        return pagingOffset;
    }

    public void setPagingOffset(String pagingOffset)
    {
        this.pagingOffset = pagingOffset;
    }

    public String getTotalResultCount()
    {
        return totalResultCount;
    }

    public void setTotalResultCount(String totalResultCount)
    {
        this.totalResultCount = totalResultCount;
    }

    protected String buildReturnUri()
    {
        StringBuilder url = new StringBuilder(getReturnUrl());
        url.append("?atl_token=").append(JiraUrlCodec.encode(getXsrfToken(), "UTF-8"));
        if (StringUtils.isNotBlank(getSearchName()))
        {
            url.append("&searchName=").append(JiraUrlCodec.encode(getSearchName(), "UTF-8"));
        }
        if (StringUtils.isNotBlank(getSearchOwnerUserName()))
        {
            url.append("&searchOwnerUserName=").append(JiraUrlCodec.encode(getSearchOwnerUserName(), "UTF-8"));
        }
        if (StringUtils.isNotBlank(getSortColumn()))
        {
            url.append("&sortColumn=").append(JiraUrlCodec.encode(getSortColumn(), "UTF-8"));
            url.append("&sortAscending=").append(JiraUrlCodec.encode(getSortAscending(), "UTF-8"));
            url.append("&pagingOffset=").append(JiraUrlCodec.encode(getPagingOffset(), "UTF-8"));
        }
        return url.toString();
    }

    protected JiraServiceContext getJiraServiceContext(String owner)
    {
        JiraServiceContext ctx;
        if (permissionManager.hasPermission(Permissions.ADMINISTER, getLoggedInUser()))
        {
            ctx = new JiraServiceContextImpl(UserUtils.getUser(owner));
        }
        else
        {
            ctx =  getJiraServiceContext();
        }
        return ctx;
    }

    protected JiraServiceContext getJiraServiceContext(long id)
    {
        JiraServiceContext ctx;
        final PortalPage dashboard = getDashboard(id);
        if (permissionManager.hasPermission(Permissions.ADMINISTER, getLoggedInUser()))
        {
            ctx = new JiraServiceContextImpl(dashboard.getOwner());
        }
        else
        {
            ctx =  getJiraServiceContext();
        }
        return ctx;
    }

    private PortalPage getDashboard(long id)
    {
        return portalPageManager.getPortalPageById(id);
    }

}
