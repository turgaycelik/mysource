package com.atlassian.jira.web.action.admin;

import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetRequestContextFactory;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardService;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.DashboardTab;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.gadgets.dashboard.view.DashboardTabViewFactory;
import com.atlassian.gadgets.view.ViewComponent;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.dashboard.DashboardUtil;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import webwork.action.ActionContext;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;

/**
 * Displays the system dashboard in the admin section for edit.
 *
 * @since v4.0
 */
@WebSudoRequired
public class EditDefaultDashboard extends JiraWebActionSupport
{
    private final PortalPageService portalPageService;
    private final DashboardPermissionService permissionService;
    private final DashboardTabViewFactory dashboardTabViewFactory;
    private final DashboardService dashboardService;
    private final GadgetRequestContextFactory gadgetRequestContextFactory;
    private Long defaultPortalPageId;
    private final ApplicationProperties applicationProperties;

    public EditDefaultDashboard(final PortalPageService portalPageService, final DashboardPermissionService permissionService,
            final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
        this.dashboardTabViewFactory = ComponentAccessor.getOSGiComponentInstanceOfType(DashboardTabViewFactory.class);
        this.dashboardService = ComponentAccessor.getOSGiComponentInstanceOfType(DashboardService.class);
        this.gadgetRequestContextFactory = ComponentAccessor.getOSGiComponentInstanceOfType(GadgetRequestContextFactory.class);
        this.portalPageService = portalPageService;
        this.permissionService = permissionService;
    }

    @Override
    public String doDefault() throws Exception
    {
        if (!isDashboardPluginEnabled())
        {
            String link = getAdministratorContactLink();
            addErrorMessage(getText("admin.errors.portalpages.plugin.disabled", link));
            return ERROR;
        }

        final PortalPage defaultPortalPage = portalPageService.getSystemDefaultPortalPage();
        if (defaultPortalPage == null)
        {
            return ERROR;
        }
        defaultPortalPageId = defaultPortalPage.getId();

        if (!permissionService.isWritableBy(DashboardId.valueOf(defaultPortalPageId.toString()), getUsername()))
        {
            return ERROR;
        }
        return SUCCESS;
    }

    private boolean isDashboardPluginEnabled()
    {
        return dashboardTabViewFactory != null && dashboardService != null && gadgetRequestContextFactory != null;
    }

    public String getDashboardHtml()
    {
        final DashboardState state = dashboardService.get(DashboardId.valueOf(defaultPortalPageId.toString()), getUsername());
        final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(ActionContext.getRequest());
        final ViewComponent view = dashboardTabViewFactory.createDashboardView(Collections.<DashboardTab>emptyList(),
                state, getUsername(), DashboardUtil.getMaxGadgets(applicationProperties), requestContext);
        final StringWriter out = new StringWriter();
        try
        {
            view.writeTo(out);
            return out.toString();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String getUsername()
    {
        final ApplicationUser remoteUser = getLoggedInApplicationUser();
        if (remoteUser != null)
        {
            return remoteUser.getUsername();
        }
        return null;
    }
}
