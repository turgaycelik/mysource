package com.atlassian.jira.web.action;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetRequestContextFactory;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardService;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.DashboardTab;
import com.atlassian.gadgets.dashboard.PermissionException;
import com.atlassian.gadgets.dashboard.view.DashboardTabViewFactory;
import com.atlassian.gadgets.view.ViewComponent;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.portal.GadgetApplinkUpgradeUtil;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.bc.security.login.LoginProperties;
import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.dashboard.DashboardUtil;
import com.atlassian.jira.dashboard.permission.GadgetPermissionManager;
import com.atlassian.jira.dashboard.permission.JiraPermissionService;
import com.atlassian.jira.event.DashboardViewEvent;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.Renderable;
import com.atlassian.jira.web.action.setup.SetupImport;
import com.atlassian.jira.web.pagebuilder.GeneralJspDecorator;
import com.atlassian.jira.web.pagebuilder.JiraPageBuilderService;
import com.atlassian.seraph.util.RedirectUtils;
import webwork.action.ActionContext;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.collect.CollectionUtil.transform;

/**
 * Figures out what the id of the portal page the user is currently viewing is.  Will fall back to system default if
 * none can be found and the user has not favourites.
 *
 * @since v4.0
 */
public class Dashboard extends JiraWebActionSupport
{
    private final XsrfTokenGenerator xsrfTokenGenerator;
    private final DashboardTabViewFactory dashboardTabViewFactory;
    private final DashboardService dashboardService;
    private final PortalPageService portalPageService;
    private final GadgetRequestContextFactory gadgetRequestContextFactory;
    private final ApplicationProperties applicationProperties;
    private final GadgetPermissionManager gadgetPermissionManager;
    private final LoginService loginService;
    private final UserHistoryManager userHistoryManager;
    private final GadgetApplinkUpgradeUtil gadgetApplinkUpgradeUtil;
    private final EventPublisher eventPublisher;
    private final JiraPageBuilderService jiraPageBuilderService;
    private final FeatureManager featureManager;

    private Long currentDashboardId;
    private List<DashboardTab> dashboardTabs;
    private List<DashboardTab> userTabs;
    private DashboardState currentDashboardState;

    private Boolean currentUserOwnPages;
    private Long selectPageId;

    /**
     * Stores the message that should be displayed to the user for a warning. This can be null when it is yet to be
     * calculated. No error message is indicated by the empty string. This is cached because it can be expensive to
     * calculate.
     */
    private String warningMessage = null;
    private static final String LOGIN_GADGET_SPEC = "rest/gadgets/1.0/g/com.atlassian.jira.gadgets/gadgets/login.xml";

    /**
     * A message indicating the status of the JIRA installion.  This is normally displayed after
     * a successfull installation.
     */
    private String installationMessage = null;

    /**
     * Where the installtion was completed from.
     */
    private String installationSource = null;

    public void setSrc(String src)
    {
        this.installationSource = src;
    }

    public Dashboard(final PortalPageService portalPageService, final ApplicationProperties applicationProperties,
            final GadgetPermissionManager gadgetPermissionManager, final LoginService loginService,
            final UserHistoryManager userHistoryManager, final GadgetApplinkUpgradeUtil gadgetApplinkUpgradeUtil,
            final EventPublisher eventPublisher, final JiraPageBuilderService jiraPageBuilderService,
            final FeatureManager featureManager, final  XsrfTokenGenerator xsrfTokenGenerator)
    {
        this.xsrfTokenGenerator = xsrfTokenGenerator;
        this.applicationProperties = applicationProperties;
        this.gadgetPermissionManager = gadgetPermissionManager;
        this.loginService = loginService;
        this.userHistoryManager = userHistoryManager;
        this.gadgetApplinkUpgradeUtil = gadgetApplinkUpgradeUtil;
        this.eventPublisher = eventPublisher;
        //Number of these components are provided by the AG plugin and thus need to be retrieved via OSGi.
        this.dashboardTabViewFactory = ComponentAccessor.getOSGiComponentInstanceOfType(DashboardTabViewFactory.class);
        this.dashboardService = ComponentAccessor.getOSGiComponentInstanceOfType(DashboardService.class);
        this.gadgetRequestContextFactory = ComponentAccessor.getOSGiComponentInstanceOfType(GadgetRequestContextFactory.class);
        this.portalPageService = portalPageService;
        this.jiraPageBuilderService = jiraPageBuilderService;
        this.featureManager = featureManager;
    }

    @Override
    protected void doValidation()
    {
        if (!isDashboardPluginEnabled())
        {
            String link = getAdministratorContactLink();
            addErrorMessage(getText("admin.errors.portalpages.plugin.disabled", link));
            return;
        }

        try
        {
            //cannot edit the default dashboard from this action.  It's only possible in the admin section.
            JiraPermissionService.setAllowEditingOfDefaultDashboard(false);
            getCurrentDashboardState();
        }
        catch (PermissionException e)
        {
            addErrorMessage(getText("admin.errors.portalpages.no.access"));
        }
    }

    @Override
    protected String doExecute() throws Exception
    {
        if (!isDashboardPluginEnabled())
        {
            return ERROR;
        }

        if (featureManager.isEnabled(JiraPageBuilderService.SEND_HEAD_EARLY_FEATURE_KEY))
        {
            // The two com.atlassian.gadgets.dashboard resources are included by the template inside the decorated page
            // (returned by this.getDashboardRenderable()), however we include them here so that they're flushed early.
            // Also they appear before the dashboard contexts in the non-send-head-early version, so let's preserve
            // that ordering.
            jiraPageBuilderService.assembler().resources()
                .requireWebResource("com.atlassian.gadgets.dashboard:dashboard")
                .requireWebResource("com.atlassian.gadgets.dashboard:gadget-dashboard-resources")
                .requireContext("atl.dashboard")
                .requireContext("jira.dashboard");

            jiraPageBuilderService.get().setDecorator(new GeneralJspDecorator(jiraPageBuilderService.assembler()));
            jiraPageBuilderService.get().flush();
        }

        if (ActionContext.getParameters().containsKey("resetPortal"))
        {
            userHistoryManager.addItemToHistory(UserHistoryItem.DASHBOARD, getLoggedInApplicationUser(),
                    String.valueOf(getPageIdForUser()));
        }

        //JRA-18300: only update the session if the dashboard is in fact a favourite dashboard!
        if (selectPageId != null && isFavouritePage(selectPageId))
        {
            userHistoryManager.addItemToHistory(UserHistoryItem.DASHBOARD, getLoggedInApplicationUser(), String.valueOf(selectPageId));
        }

        eventPublisher.publish(new DashboardViewEvent(currentDashboardId));

        displayInstallationMessageIfRequired();

        return SUCCESS;
    }

    private void displayInstallationMessageIfRequired()
    {
        if (this.installationSource != null)
        {
            installationMessage = SetupImport.class.getSimpleName().equals(this.installationSource) ?
                    getText("setup.complete.import.message") : getText("setup.complete.message");
        }
    }

    private boolean isDashboardPluginEnabled()
    {
        return dashboardTabViewFactory != null && dashboardService != null && gadgetRequestContextFactory != null;
    }

    public String getDashboardTitle()
    {
        return getCurrentDashboardState().getTitle();
    }

    public String getInstallationMessage()
    {
        return this.installationMessage;
    }

    public Renderable getDashboardRenderable()
    {
        final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(ActionContext.getRequest());
        final ViewComponent view = dashboardTabViewFactory.createDashboardView(getDashboardTabs(), getCurrentDashboardState(),
                getUsername(), DashboardUtil.getMaxGadgets(applicationProperties), requestContext);
        return new Renderable()
        {
            @Override
            public void render(Writer writer) throws IOException
            {
                view.writeTo(writer);
            }
        };
    }

    public Long getCurrentDashboardId()
    {
        if (currentDashboardId == null)
        {
            initialiseCurrentDashboardId();
        }
        return currentDashboardId;
    }

    /**
     * Return to the caller the PortalPage being rendered. This method may load the PortalPage into memory if it has not
     * already been read.
     *
     * @return the PortalPage being rendered.
     */
    public DashboardState getCurrentDashboardState() throws PermissionException
    {
        if (currentDashboardState == null)
        {
            currentDashboardState = dashboardService.get(DashboardId.valueOf(getCurrentDashboardId().toString()), getUsername());
            //if we're not logged in and the dashboard is the default dashboard, then add the Login gadget.
            if (getLoggedInUser() == null && isSystemDashboardId(getCurrentDashboardId()) && !applicationProperties.getOption(APKeys.JIRA_DISABLE_LOGIN_GADGET))
            {
                //get right most column!
                final DashboardState.ColumnIndex rightColumn = DashboardState.ColumnIndex.from(currentDashboardState.getLayout().getNumberOfColumns() - 1);
                final Map<String, String> loginGadgetPrefs = buildLoginProperties();
                currentDashboardState = currentDashboardState.prependGadgetToColumn(
                        GadgetState.gadget(GadgetId.valueOf(Long.toString(0L))).specUri(URI.create(LOGIN_GADGET_SPEC)).color(Color.color1).userPrefs(loginGadgetPrefs).build(),
                        rightColumn);
            }
            //remove any gadgets a user may potentially not have permission to see.
            currentDashboardState = gadgetPermissionManager.filterGadgets(currentDashboardState, getLoggedInUser());
        }
        return currentDashboardState;
    }

    private Map<String, String> buildLoginProperties()
    {
        final LoginProperties loginProperties = loginService.getLoginProperties(getLoggedInUser(), request);
        return MapBuilder.<String, String>newBuilder().
                add("loginSucceeded", String.valueOf(loginProperties.isLoginSucceeded())).
                add("allowCookies", String.valueOf(loginProperties.isAllowCookies())).
                add("externalUserManagement", String.valueOf(loginProperties.isExternalUserManagement())).
                add("isPublicMode", String.valueOf(loginProperties.isPublicMode())).
                add("isElevatedSecurityCheckShown", String.valueOf(loginProperties.isElevatedSecurityCheckShown())).
                add("captchaFailure", String.valueOf(loginProperties.isCaptchaFailure())).
                add("loginFailedByPermissions", String.valueOf(loginProperties.getLoginFailedByPermissions())).
                add("isAdminFormOn", String.valueOf(applicationProperties.getOption(APKeys.JIRA_SHOW_CONTACT_ADMINISTRATORS_FORM))).
                toMap();
    }

    public Long getSelectPageId()
    {
        return selectPageId;
    }

    public void setSelectPageId(final Long selectPageId)
    {
        this.selectPageId = selectPageId;
    }

    public String getLoginLink()
    {
        final StringBuilder link = new StringBuilder();

        link.append("<a rel=\"nofollow\" href=\"");
        link.append(RedirectUtils.getLinkLoginURL(request));
        link.append("\">");
        link.append(getText("login.required.login"));
        link.append("</a>");

        return getText("dashboard.page.login", link.toString());
    }

    /**
     * Return a warning HTML string that will be displayed on the GUI.
     *
     * @return the HTML to display on the GUI.
     */
    public String getWarningMessage()
    {
        final PortalPage portalPage = portalPageService.getPortalPage(getJiraServiceContext(), currentDashboardId);
        if (portalPage == null)
        {
            return null;
        }

        if (warningMessage != null)
        {
            return warningMessage;
        }

        if (getLoggedInUser() == null)
        {
            if (isEmptyDashboard())
            {
                final StringBuilder link = new StringBuilder();
                link.append("<a rel=\"nofollow\" href=\"");
                link.append(RedirectUtils.getLinkLoginURL(request));
                link.append("\">");
                warningMessage = getText("dashboard.no.gadget.permission.logged.out", link.toString(), "</a>");
            }
            else
            {
                //anonymous user should see no other message.
                warningMessage = "";
            }
            return warningMessage;
        }

        String newWarningMessage = null;

        final boolean hasPreferredPages = doesUserHavePreferredPages();
        if (selectPageId != null)
        {
            //we have asked to display a page directly.
            boolean currentPortalPageFavourite = portalPageService.isFavourite(getLoggedInUser(), portalPage);
            if (!currentPortalPageFavourite && (hasPreferredPages || !portalPage.isSystemDefaultPortalPage()))
            {
                //we need to display a warning on the page indicating that the user only sees this page because they
                //requested it directly.
                newWarningMessage = getText("dashboard.non.favourite.displayed", createFavouriteLink(portalPage), "</a>");
            }
        }
        if (newWarningMessage == null)
        {
            //user will see the system default but has some dashboard pages they own. Give them a warning.
            if (!hasPreferredPages && portalPage.isSystemDefaultPortalPage() && doesUserOwnPages())
            {
                newWarningMessage = getText("dashboard.no.pages.to.display", createManageDashboardLinkShowMyTab(), "</a>");
            }
        }

        if (newWarningMessage == null)
        {
            if (!isUserCurrentPageOwner(portalPage))
            {
                if (isEmptyPortalPage(portalPage.getId()))
                {
                    if (portalPageService.isFavourite(getLoggedInUser(), portalPage))
                    {
                        // If this page is a favourite, then suggest the user unfavourite it.
                        newWarningMessage = getText("dashboard.no.portlet", createUnfavouriteLink(portalPage), "</a>");
                    }
                }
            }
        }

        //if the dashboard we're viewing doesn't contain any gadgets show a warning to the user
        if (newWarningMessage == null && isEmptyDashboard() && !isUserCurrentPageOwner(portalPage))
        {
            String link = getAdministratorContactLink();
            newWarningMessage = getText("dashboard.no.gadget.permission", link);
        }

        this.warningMessage = newWarningMessage == null ? "" : newWarningMessage;
        return this.warningMessage;
    }

    private boolean isEmptyPortalPage(Long portalPageId)
    {
        final List<List<PortletConfiguration>> list = portalPageService.getPortletConfigurations(getJiraServiceContext(), portalPageId);
        for (List<PortletConfiguration> portletConfigurations : list)
        {
            if(!portletConfigurations.isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    private boolean isEmptyDashboard()
    {
        DashboardState state = getCurrentDashboardState();
        int gadgetCount = 0;
        for (Iterable<GadgetState> column : state.getColumns())
        {
            for (GadgetState gadgetState : column)
            {
                gadgetCount++;
            }
        }
        return gadgetCount == 0;
    }

    private boolean isFavouritePage(final Long pageId)
    {
        return portalPageService.isFavourite(getLoggedInApplicationUser(), portalPageService.getPortalPage(getJiraServiceContext(), pageId));
    }

    /**
     * Return a list of pages that the user will see of the dashboard. This will be the same as {@link #getUserTabs()}
     * except when the user is looking at a page that don't have favourited. In that case a new collection with the
     * extra page included will be returned.
     *
     * @return the list of pages that the user will see on the dashboard.
     */
    private List<DashboardTab> getDashboardTabs()
    {
        if (dashboardTabs == null)
        {
            final ApplicationUser currentUser = getLoggedInApplicationUser();
            final List<DashboardTab> ret = new ArrayList<DashboardTab>(getUserTabs());
            final PortalPage page = portalPageService.getPortalPage(getJiraServiceContext(), getCurrentDashboardId());
            //if the page is not a favourite then we need to display a temporary tab for it
            if (!portalPageService.isFavourite(currentUser, page))
            {
                ret.add(createDashboardTab(page));
            }

            dashboardTabs = ret;
        }
        return dashboardTabs;
    }

    /**
     * Returns true if the current user has a list of preferred pages for display.
     *
     * @return true if the user has a list of preferred pages or false otherwise.
     */
    private boolean doesUserHavePreferredPages()
    {
        return !getUserTabs().isEmpty();
    }

    private static String createLink(final String id, final String url)
    {
        return "<a id=\"" + id + "\" href=\"" + url + "\">";
    }

    private String createFavouriteLink(final PortalPage portalPage)
    {
        StringBuilder buffer = new StringBuilder("AddFavourite.jspa?entityType=").append(portalPage.getEntityType().getName());
        buffer.append("&entityId=").append(portalPage.getId());
        buffer.append("&atl_token=").append(xsrfTokenGenerator.generateToken());
        buffer.append("&returnUrl=").append(JiraUrlCodec.encode("Dashboard.jspa?selectPageId=" + portalPage.getId()));

        return createLink("dashmsg_favourite", buffer.toString());
    }

    private String createUnfavouriteLink(final PortalPage portalPage)
    {
        StringBuilder buffer = new StringBuilder("RemoveFavourite.jspa?entityType=").append(portalPage.getEntityType().getName());
        buffer.append("&entityId=").append(portalPage.getId());
        buffer.append("&atl_token=").append(xsrfTokenGenerator.generateToken());
        buffer.append("&returnUrl=").append("Dashboard.jspa");

        return createLink("dashmsg_unfavourite", buffer.toString());
    }

    private String createManageDashboardLinkShowMyTab()
    {
        return createLink("dashmsg_managedashboard", "ConfigurePortalPages!default.jspa?view=my");
    }

    /**
     * Returns true when the current user owns some dashboard pages.
     *
     * @return true if the user has a list of preferred pages or false otherwise.
     */
    private boolean doesUserOwnPages()
    {
        if (currentUserOwnPages == null)
        {
            final User user = getLoggedInUser();
            if (user == null)
            {
                currentUserOwnPages = Boolean.FALSE;
            }
            else
            {
                final Collection portalPages = portalPageService.getOwnedPortalPages(user);
                currentUserOwnPages = portalPages != null && !portalPages.isEmpty();
            }
        }
        return currentUserOwnPages;
    }

    /**
     * Tells the caller whether or not the current user is an owner of the current page.
     *
     * @return true if the user is the owner of the current page.
     */
    private boolean isUserCurrentPageOwner(PortalPage currentPortalPage)
    {
        return currentPortalPage != null
                && currentPortalPage.getOwner() != null
                && currentPortalPage.getOwner().equals(getLoggedInApplicationUser());
    }

    /**
     * Get the portal page to render. The page found is cached because this can be an expensive operation.
     */
    private void initialiseCurrentDashboardId()
    {
        if (currentDashboardId != null)
        {
            return;
        }

        if (selectPageId != null)
        {
            //if the user asked for a particular page then try to display it no matter if invalid.
            currentDashboardId = selectPageId;
        }
        else
        {
            Long pageId;
            if (getLoggedInApplicationUser() != null)
            {
                final List<UserHistoryItem> history = userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, getLoggedInApplicationUser());
                if (history.isEmpty())
                {
                    pageId = getPageIdForUser();
                    userHistoryManager.addItemToHistory(UserHistoryItem.DASHBOARD, getLoggedInApplicationUser(), String.valueOf(pageId));
                }
                else
                {
                    pageId = Long.valueOf(history.get(0).getEntityId());
                    final PortalPage portalPage = portalPageService.getPortalPage(new JiraServiceContextImpl(getLoggedInApplicationUser(), new SimpleErrorCollection()), pageId);
                    //if this page doesn't exist any longer or it's not a favourite the history must be stale!
                    if(portalPage == null || !portalPageService.isFavourite(getLoggedInApplicationUser(), portalPage))
                    {
                        pageId = getPageIdForUser();
                        userHistoryManager.addItemToHistory(UserHistoryItem.DASHBOARD, getLoggedInApplicationUser(), String.valueOf(pageId));
                    }
                }
            }
            else
            {
                pageId = portalPageService.getSystemDefaultPortalPage().getId();
            }

            if (pageId != null)
            {
                currentDashboardId = pageId;
            }
            else
            {
                //we are in some serious trouble here...display a serious error message.
                addErrorMessage("dashboard.no.default.dashboard");
            }
        }
    }

    private boolean isSystemDashboardId(final Long dashboardId)
    {
        if (dashboardId == null)
        {
            return false;
        }
        final PortalPage defaultPortalPage = portalPageService.getSystemDefaultPortalPage();
        return defaultPortalPage != null && defaultPortalPage.getId().equals(dashboardId);
    }

    /**
     * Return the PortalPage that the current user can see. This method should not be called directly because it is
     * slow.
     *
     * @return a PortalPage the current user can see.
     */
    private Long getPageIdForUser()
    {
        //try to select the user's first favourite
        final List<DashboardTab> tabs = getUserTabs();
        if (tabs != null && !tabs.isEmpty())
        {
            return Long.valueOf(tabs.get(0).getDashboardId().toString());
        }
        //no favourite pages means that we should use the system default.
        return portalPageService.getSystemDefaultPortalPage().getId();
    }

    /**
     * Return to the caller a list of PortalPages that the user *should* see on the dashboard. This will be the user's
     * favourite pages.
     * <p/>
     * The user may see more PortalPages on the dashboard if they are looking at a page they don't have favourited.
     *
     * @return a collection of pages that the user should see on the dashboard.
     */
    private List<DashboardTab> getUserTabs()
    {
        if (userTabs == null)
        {
            userTabs = transform(portalPageService.getFavouritePortalPages(getLoggedInApplicationUser()), new Function<PortalPage, DashboardTab>()
            {
                public DashboardTab get(final PortalPage dashboard)
                {
                    return createDashboardTab(dashboard);
                }
            });
        }

        return userTabs;
    }

    private DashboardTab createDashboardTab(final PortalPage dashboard)
    {
        return new DashboardTab(
                DashboardId.valueOf(Long.toString(dashboard.getId())),
                dashboard.getName(),
                URI.create("Dashboard.jspa?selectPageId=" + dashboard.getId()));
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

