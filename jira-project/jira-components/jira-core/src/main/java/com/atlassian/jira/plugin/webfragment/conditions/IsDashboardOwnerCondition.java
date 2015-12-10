package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Checks if the current user is the owner of a given dashboard
 *
 * @since v4.0
 */
public class IsDashboardOwnerCondition implements Condition
{
    public static final String CONTEXT_KEY_DASHBOARD_ID = "dashboardId";

    public void init(final Map<String, String> params) throws PluginParseException
    {
    }

    public boolean shouldDisplay(final Map<String, Object> context)
    {
        final DashboardId dashboardId = (DashboardId) context.get(CONTEXT_KEY_DASHBOARD_ID);
        final String username = (String) context.get(JiraWebInterfaceManager.CONTEXT_KEY_USERNAME);

        //if the user has writePermission, he/she has to be the owner!
        return dashboardId != null && StringUtils.isNotBlank(username) && getPermissionService().isWritableBy(dashboardId, username);
    }

    DashboardPermissionService getPermissionService()
    {
        return ComponentAccessor.getComponentOfType(DashboardPermissionService.class);
    }
}