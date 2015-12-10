package com.atlassian.jira.web.action.admin.dashboards;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.sharing.search.SharedEntitySearchContext;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.GroupPermissionChecker;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.action.user.ConfigurePortalPages;
import com.atlassian.jira.web.action.user.PortalPageLinkRenderer;
import com.atlassian.jira.web.action.user.UserProfileAction;
import com.atlassian.jira.web.action.util.PortalPageDisplayBean;
import com.atlassian.jira.web.action.util.sharing.SharedEntitySearchAction;
import com.atlassian.jira.web.action.util.sharing.SharedEntitySearchViewHelper;
import com.atlassian.jira.web.ui.model.DropDownModel;
import com.atlassian.jira.web.ui.model.DropDownModelBuilder;
import com.atlassian.jira.web.ui.model.DropDownModelProvider;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Responsible for displaying the shared dashboards administration page.
 *
 * @since v4.4.1
 */
@WebSudoRequired
public class ViewSharedDashboards extends ConfigurePortalPages
        implements SharedEntitySearchAction, DropDownModelProvider<PortalPageDisplayBean>
{
    private static final PortalPageLinkRenderer NO_LINK_RENDERER = new PortalPageLinkRenderer()
    {
        public String render(final Long id, final String name)
        {
            return "<span data-field=\"name\">" + TextUtils.htmlEncode(name) + "</span>";
        }
    };

    private final JiraAuthenticationContext authCtx;
    private final SearchRequestService searchRequestService;
    private final ShareTypeFactory shareTypeFactory;
    private final PortalPageViewHelper dashboardViewHelper;
    private final static String OWNER = "filters.searchOwnerUserName";
    private final static String NAME = "filters.searchName";
    private static final String CONTENTONLY = "contentonly";

    public ViewSharedDashboards(final PortalPageService portalPageService, final JiraAuthenticationContext authCtx,
            final FavouritesService favouritesService, final PermissionManager permissionManager,
            final ShareTypeFactory shareTypeFactory, final UserFormatManager userFormatManager,
            final WebResourceManager webResourceManager, final UserHistoryManager userHistoryManager,
            final SearchRequestService searchRequestService)
    {
        super(portalPageService, authCtx, favouritesService, permissionManager, shareTypeFactory, userFormatManager, webResourceManager, userHistoryManager);
        this.authCtx = authCtx;
        this.searchRequestService = searchRequestService;
        this.shareTypeFactory = shareTypeFactory;
        this.dashboardViewHelper = new PortalViewHelper(shareTypeFactory, authCtx, ActionContext.getRequest().getContextPath(),
                "ViewSharedDashboards.jspa", portalPageService);
    }

    @Override
    public DropDownModel getDropDownModel(PortalPageDisplayBean displayBean, int listIndex)
    {
        DropDownModelBuilder builder = DropDownModelBuilder.builder();

        builder.setTopText(getText("common.words.operations"));
        builder.startSection()
                .addItem
                        (
                                builder.item()
                                        .setText(getText("shareddashboards.admin.cog.changeowner"))
                                        .setAttr("id", "change_owner_" + displayBean.getId())
                                        .setAttr("class", "change-owner")
                                        .setAttr("href", toUrl(displayBean, "ChangeSharedDashboardOwner!default.jspa", "dashboardId", true) + buildQueryStringForModel(ExecutingHttpRequest.get()))
                        )
                .addItem
                        (
                                builder.item()
                                        .setText(getText("shareddashboards.delete"))
                                        .setAttr("id", "delete_" + displayBean.getId())
                                        .setAttr("class", "delete-dashboard")
                                        .setAttr("href", toUrl(displayBean, "DeleteSharedDashboard!default.jspa", "dashboardId", true) + buildQueryStringForModel(ExecutingHttpRequest.get()))
                        );

        builder.endSection();
        return builder.build();
    }

    private String buildQueryStringForModel(HttpServletRequest request)
    {
        final StringBuilder builder = new StringBuilder("");
        if (request.getMethod().equalsIgnoreCase("POST"))
        {
            builder.append("&");
            builder.append("searchOwnerUserName=");
            builder.append(JiraUrlCodec.encode(getSearchOwnerUserName()));
            builder.append("&searchName=");
            builder.append(JiraUrlCodec.encode(getSearchName()));
        }
        else
        {
            if (getQueryString()  != null) {
                builder.append("&");
                builder.append(getQueryString());
            }
        }
        builder.append("&totalResultCount=").append(JiraUrlCodec.encode(""+getTotalResultCount()));
        return builder.toString();
    }

    @Override
    public String doDefault()
    {
        return executeSearchView();
    }

    @Override
    protected void doValidation()
    {
        super.doValidation();
    }

    @Override
    protected String doExecute()
    {
        setReturnUrl(String.format("ViewSharedDashboards.jspa"));
        ActionContext.getSession().put(OWNER, getSearchOwnerUserName());
        ActionContext.getSession().put(NAME, getSearchName());
        return executeSearchView();
    }

    private String executeSearchView()
    {

        final JiraServiceContext ctx = getJiraServiceContext();
        SharedEntitySearchViewHelper.SearchResult<PortalPage> searchResults = getPortalPageViewHelper().search(ctx);
        setSearchResults(searchResults);
        if (!ctx.getErrorCollection().hasAnyErrors())
        {
            setPages(transformToDisplayBeans(searchResults.getResults()));
        }
        return isContentOnly() ?  CONTENTONLY : SUCCESS;
    }

    @Override
    public PortalPageViewHelper getPortalPageViewHelper()
    {
        return dashboardViewHelper;
    }

    @Override
    public PortalPageLinkRenderer getPortalPageLinkRenderer()
    {
        return NO_LINK_RENDERER;
    }

    private String getQueryString()
    {
        return ExecutingHttpRequest.get().getQueryString();
    }

    private static class PortalViewHelper extends  PortalPageViewHelper
    {
        public PortalViewHelper(final ShareTypeFactory shareTypeFactory, final JiraAuthenticationContext authCtx, final String applicationContext, final String actionUrlPrefix, final PortalPageService portalPageService)
        {
            super(shareTypeFactory, authCtx, applicationContext, actionUrlPrefix, portalPageService);

        }

        @Override
        public SharedEntitySearchContext getEntitySearchContext()
        {
            return SharedEntitySearchContext.ADMINISTER;
        }

    }
}
