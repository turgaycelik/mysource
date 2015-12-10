package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * Decides wheter or not to show the Manage Dashboards link.  This depends on if the user is logged in and if
 * the dashboard is the default dashboard being viewed on the HOME page (rather than admin section).
 *
 * @since v4.0
 */
public class ShowDashboardToolsMenuCondition implements Condition
{
    public static final String CONTEXT_KEY_DASHBOARD_ID = "dashboardId";

    public void init(final Map<String, String> params) throws PluginParseException
    {
    }

    public boolean shouldDisplay(final Map<String, Object> context)
    {
        final DashboardPermissionService permissionService = getPermissionService();
        final PortalPageService portalPageService = getPortalPageService();
        final PortalPage defaultPortalPage = portalPageService.getSystemDefaultPortalPage();
        final DashboardId dashboardId = (DashboardId) context.get(CONTEXT_KEY_DASHBOARD_ID);
        final String username = (String) context.get(JiraWebInterfaceManager.CONTEXT_KEY_USERNAME);

        //if we're viewing the default dashboard and the user has permission to edit it means we're in the admin section and
        //we should not show the Manage dashboards link.
        if (dashboardId != null && defaultPortalPage.getId().equals(Long.valueOf(dashboardId.value())) && permissionService.isWritableBy(dashboardId, username))
        {
            return false;
        }
        final User user = getUserUtil().getUser(username);

        //for any other dashboard if the user is logged in show the Manage link.
        return user != null;
    }

    PortalPageService getPortalPageService()
    {
        return ComponentAccessor.getComponentOfType(PortalPageService.class);
    }

    DashboardPermissionService getPermissionService()
    {
        return ComponentAccessor.getComponentOfType(DashboardPermissionService.class);
    }

    UserUtil getUserUtil()
    {
        return ComponentAccessor.getUserUtil();
    }
}
