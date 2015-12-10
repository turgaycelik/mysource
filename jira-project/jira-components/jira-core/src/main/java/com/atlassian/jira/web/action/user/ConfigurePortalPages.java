package com.atlassian.jira.web.action.user;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.sharing.search.SharedEntitySearchContext;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.util.PortalPageDisplayBean;
import com.atlassian.jira.web.action.util.portal.PortalPageRetriever;
import com.atlassian.jira.web.action.util.sharing.SharedEntitySearchAction;
import com.atlassian.jira.web.action.util.sharing.SharedEntitySearchViewHelper;
import com.atlassian.jira.web.ui.model.DropDownModel;
import com.atlassian.jira.web.ui.model.DropDownModelBuilder;
import com.atlassian.jira.web.ui.model.DropDownModelProvider;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This action is the place we manage Portal Pages, aka Dashboard pages from
 */
public class ConfigurePortalPages extends JiraWebActionSupport
        implements SharedEntitySearchAction, DropDownModelProvider<PortalPageDisplayBean>
{
    private static final PortalPageLinkRenderer DASHBOARD_LINK_RENDERER  =  new PortalPageLinkRenderer()
    {
        @Override
        public String render(Long id, String name)
        {
            return  "<a href=\""+ActionContext.getRequest().getContextPath()+"/secure/Dashboard.jspa?selectPageId="+ id +"\">"+ TextUtils.htmlEncode(name) + "</a>";
        }
    };


    private static final String KEY_NO_SEARCH = "common.sharing.searching.no.search.performed";
    private static final String KEY_NO_RESULTS = "portalpage.no.search.results";

    private static final String CONTENTONLY = "contentonly";

    private final PortalPageService portalPageService;
    private final JiraAuthenticationContext authenticationContext;
    private final FavouritesService favouritesService;
    private final PermissionManager permissionManager;
    private final ShareTypeFactory shareTypeFactory;
    private final ConfigurePortalPages.PortalPageViewHelper portalPageViewHelper;
    private final UserFormatManager userFormatManager;
    private final WebResourceManager webResourceManager;
    private final PortalPageRetriever portalPageRetriever;

    private String view;
    private String portalPageName;
    private String description;
    private Long copyPageId;
    private int position;

    private String searchEmptyMessageKey = KEY_NO_RESULTS;
    private SharedEntitySearchViewHelper.SearchResult<PortalPage> searchResults;
    private List<PortalPageDisplayBean> pages;
    private boolean contentOnly = false;

    public ConfigurePortalPages(final PortalPageService portalPageService, final JiraAuthenticationContext authenticationContext,
            final FavouritesService favouritesService, final PermissionManager permissionManager,
            final ShareTypeFactory shareTypeFactory, final UserFormatManager userFormatManager,
            final WebResourceManager webResourceManager, final UserHistoryManager userHistoryManager)
    {
        this.portalPageService = portalPageService;
        this.authenticationContext = authenticationContext;
        this.favouritesService = favouritesService;
        this.permissionManager = permissionManager;
        this.shareTypeFactory = shareTypeFactory;
        this.userFormatManager = userFormatManager;
        this.webResourceManager = webResourceManager;
        this.portalPageRetriever = new PortalPageRetriever(portalPageService, userHistoryManager, authenticationContext);
        this.portalPageViewHelper = new PortalPageViewHelper(this.shareTypeFactory, this.authenticationContext,
            ActionContext.getRequest().getContextPath(), "ConfigurePortalPages!default.jspa", portalPageService);
    }

    //
    // Implementation of the SharedEntitySearchAction.
    //

    public String getSearchName()
    {
        return getPortalPageViewHelper().getSearchName();
    }

    public void setSearchName(final String searchName)
    {
        getPortalPageViewHelper().setSearchName(searchName);
    }

    public String getSearchOwnerUserName()
    {
        return getPortalPageViewHelper().getSearchOwnerUserName();
    }

    public void setSearchOwnerUserName(final String searchOwnerUserName)
    {
        getPortalPageViewHelper().setSearchOwnerUserName(searchOwnerUserName);
    }

    public String getSearchShareType()
    {
        return getPortalPageViewHelper().getSearchShareType();
    }

    public void setSearchShareType(final String searchShareType)
    {
        getPortalPageViewHelper().setSearchShareType(searchShareType);
    }

    public void setGroupShare(final String groupShare)
    {
        getPortalPageViewHelper().setGroupShare(groupShare);
    }

    public String getGroupShare()
    {
        return getPortalPageViewHelper().getGroupShare();
    }

    public Long getPagingOffset()
    {
        return getPortalPageViewHelper().getPagingOffset();
    }

    public void setProjectShare(final String projectShare)
    {
        getPortalPageViewHelper().setProjectShare(projectShare);
    }

    public String getProjectShare()
    {
        return getPortalPageViewHelper().getProjectShare();
    }

    public void setRoleShare(final String roleShare)
    {
        getPortalPageViewHelper().setRoleShare(roleShare);
    }

    public String getRoleShare()
    {
        return getPortalPageViewHelper().getRoleShare();
    }

    public void setPagingOffset(final Long pagingOffset)
    {
        getPortalPageViewHelper().setPagingOffset(pagingOffset);
    }

    public String getSortColumn()
    {
        return getPortalPageViewHelper().getSortColumn();
    }

    public void setSortColumn(final String sortColumn)
    {
        getPortalPageViewHelper().setSortColumn(sortColumn);
    }

    public boolean isSortAscending()
    {
        return getPortalPageViewHelper().isSortAscending();
    }

    public void setSortAscending(final boolean sortAscending)
    {
        getPortalPageViewHelper().setSortAscending(sortAscending);
    }

    protected void setSearchResults(SharedEntitySearchViewHelper.SearchResult<PortalPage> searchResults)
    {
        this.searchResults = searchResults;
    }

    //
    // Parameter methods.
    //

    public void setPageId(final Long pageId)
    {
        portalPageRetriever.setRequestedPageId(pageId);
    }

    public String getPortalPageName()
    {
        return portalPageName;
    }

    public void setPortalPageName(final String portalPageName)
    {
        this.portalPageName = portalPageName;
    }

    public Long getCopyPageId()
    {
        return copyPageId;
    }

    public void setCopyPageId(final Long copyPageId)
    {
        this.copyPageId = copyPageId;
    }

    public Long getPageId()
    {
        return portalPageRetriever.getPageId();
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public String getView()
    {
        return view;
    }

    public void setView(final String view)
    {
        this.view = view;
    }

    public int getPosition()
    {
        return position;
    }

    public void setPosition(final int position)
    {
        this.position = position;
    }

    public String getPageName(final PortalPage portalPage)
    {
        if (portalPage != null)
        {
            return portalPage.getName();
        }
        else
        {
            return null;
        }
    }

    public boolean isContentOnly()
    {
        return contentOnly;
    }

    public void setContentOnly(boolean contentOnly)
    {
        this.contentOnly = contentOnly;
    }

//
    //Methods used by the view.
    //

    public String getSearchEmptyMessageKey()
    {
        return searchEmptyMessageKey;
    }

    public PortalPageViewHelper getPortalPageViewHelper()
    {
        return portalPageViewHelper;
    }

    public boolean isSearchRequested()
    {
        return ActionContext.getParameters().get("Search") != null;
    }

    public PortalPage getCurrentPortalPage()
    {
        return portalPageRetriever.getPortalPage(getJiraServiceContext());
    }

    public List getPages()
    {
        return pages;
    }

    protected void setPages(List<PortalPageDisplayBean> pages)
    {
        this.pages = pages;
    }

    public String getNextUrl()
    {
        return searchResults != null ? searchResults.getNextUrl() : null;
    }

    public String getPreviousUrl()
    {
        return searchResults != null ? searchResults.getPreviousUrl() : null;
    }

    public int getStartPosition()
    {
        return searchResults != null ? searchResults.getStartResultPosition() : -1;
    }

    public int getEndPosition()
    {
        return searchResults != null ? searchResults.getEndResultPosition() : -1;
    }

    public int getTotalResultCount()
    {
        return searchResults != null ? searchResults.getTotalResultCount() : -1;
    }

    //
    // Action methods.
    //

    /**
     * Default method runs when displaying portal pages.
     *
     * @return the view to display
     */
    public String doDefault()
    {
        // Figure out which Tab (view) to show the user.
        String tab;
        if (StringUtils.isNotBlank(view))
        {
            // the user has clicked on a particular tab - we show this one.
            tab = view;

        }
        else
        {
            // the user did not click on a particular tab.
            // Find which one they clicked on last.
            tab = getTabFromSession();
        }

        if (contentOnly && !isValid(tab))
        {
            ServletActionContext.getResponse().setStatus(401);
            return NONE;
        }

        if (!contentOnly)
        {
            webResourceManager.requireResource("jira.webresources:managedashboards");
        }

        if (Tab.SEARCH.equalsIgnoreCase(tab))
        {
            showSearchTab();
        }
        else if (Tab.POPULAR.equalsIgnoreCase(tab))
        {
            showPopularTab();
        }
        else if (Tab.MY.equalsIgnoreCase(tab))
        {
            // We attempt to show the "My" tab, although if it is empty, we may show another one instead.
            showMyTab();
        }
        else if (Tab.FAVOURITES.equalsIgnoreCase(tab))
        {
            // We attempt to show the "FAVOURITES" tab, although if it is empty, we may show another one instead.
            showFavouritesTab();
        }
        else
        {
            if (getLoggedInUser() == null)
            {
                showPopularTab();
            }
            else
            {
                showFavouritesTab();
            }
        }
        // remember the current tab, in case we need to know which one to show next time.
        storeTabInSession(view);

        return contentOnly ? CONTENTONLY : SUCCESS;

    }

    private boolean isValid(String tab)
    {
        return getLoggedInUser() != null || Tab.MY.equals(tab) || Tab.FAVOURITES.equals(tab);
    }

    private void showMyTab()
    {
        pages = transformToDisplayBeans(portalPageService.getOwnedPortalPages(getLoggedInUser()));
        view = Tab.MY;
    }

    private void showFavouritesTab()
    {
        // The user chose this tab specifically, or actually owns Portal Pages; show this tab.
        pages = transformToDisplayBeans(portalPageService.getFavouritePortalPages(getLoggedInUser()));
        view = Tab.FAVOURITES;
    }

    public PortalPageLinkRenderer getPortalPageLinkRenderer()
    {
        return DASHBOARD_LINK_RENDERER;
    }

    public String doMoveUp()
    {
        if (checkPortalPageId())
        {
            return ERROR;
        }

        final JiraServiceContext serviceContext = getJiraServiceContext();
        if (portalPageService.validateForChangePortalPageSequence(serviceContext, portalPageRetriever.getRequestedPageId()))
        {
            portalPageService.increasePortalPageSequence(serviceContext, portalPageRetriever.getRequestedPageId());
        }

        if (contentOnly)
        {
            showFavouritesTab();
            return CONTENTONLY;
        }
        return getRedirect("ConfigurePortalPages!default.jspa");
    }

    public String doMoveDown()
    {
        if (checkPortalPageId())
        {
            return ERROR;
        }
        final JiraServiceContext serviceContext = getJiraServiceContext();
        if (portalPageService.validateForChangePortalPageSequence(serviceContext, portalPageRetriever.getRequestedPageId()))
        {
            portalPageService.decreasePortalPageSequence(serviceContext, portalPageRetriever.getRequestedPageId());
        }

        if (contentOnly)
        {
            showFavouritesTab();
            return CONTENTONLY;
        }

        return getRedirect("ConfigurePortalPages!default.jspa");
    }

    public String doMoveToStart()
    {
        if (checkPortalPageId())
        {
            return ERROR;
        }
        final JiraServiceContext serviceContext = getJiraServiceContext();
        if (portalPageService.validateForChangePortalPageSequence(serviceContext, portalPageRetriever.getRequestedPageId()))
        {
            portalPageService.moveToStartPortalPageSequence(serviceContext, portalPageRetriever.getRequestedPageId());
        }
        if (contentOnly)
        {
            showFavouritesTab();
            return CONTENTONLY;
        }

        return getRedirect("ConfigurePortalPages!default.jspa");
    }

    public String doMoveToEnd()
    {
        if (checkPortalPageId())
        {
            return ERROR;
        }
        final JiraServiceContext serviceContext = getJiraServiceContext();
        if (portalPageService.validateForChangePortalPageSequence(serviceContext, portalPageRetriever.getRequestedPageId()))
        {
            portalPageService.moveToEndPortalPageSequence(serviceContext, portalPageRetriever.getRequestedPageId());
        }
        if (contentOnly)
        {
            showFavouritesTab();
            return CONTENTONLY;
        }

        return getRedirect("ConfigurePortalPages!default.jspa");
    }

    private boolean checkPortalPageId()
    {
        if (portalPageRetriever.getRequestedPageId() == null)
        {
            addErrorMessage(getText("admin.errors.user.page.id.must.be.set"));
            return true;
        }
        return false;
    }

    protected List<PortalPageDisplayBean> transformToDisplayBeans(final Collection<PortalPage> portalPages)
    {
        if ((portalPages == null) || portalPages.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<PortalPageDisplayBean> displayBeans = new ArrayList<PortalPageDisplayBean>(portalPages.size());
        for (final PortalPage portalPage : portalPages)
        {
            displayBeans.add(new PortalPageDisplayBean(authenticationContext, portalPage, favouritesService, permissionManager, shareTypeFactory,
                    userFormatManager));
        }

        return displayBeans;
    }

    private void showSearchTab()
    {
        // if they have pressed the 'Search' button then
        boolean searchPerformed = isSearchRequested();
        if (searchPerformed)
        {
            final JiraServiceContext ctx = getJiraServiceContext();
            searchResults = getPortalPageViewHelper().search(ctx);
            if (ctx.getErrorCollection().hasAnyErrors())
            {
                searchPerformed = false;
            }
            else
            {
                pages = transformToDisplayBeans(searchResults.getResults());
            }
        }
        if (!searchPerformed)
        {
            searchEmptyMessageKey = KEY_NO_SEARCH;
        }
        view = Tab.SEARCH;
    }

    private void showPopularTab()
    {
        final JiraServiceContext serviceContext = getJiraServiceContext();
        final Collection<PortalPage> portalPages = getPortalPageViewHelper().getPopularFilters(serviceContext);
        if (!serviceContext.getErrorCollection().hasAnyErrors())
        {
            pages = transformToDisplayBeans(portalPages);
        }

        view = Tab.POPULAR;
    }

    private void storeTabInSession(final String view)
    {
        //noinspection unchecked
        ActionContext.getSession().put(SessionKeys.CONFIGURE_PORTAL_PAGES_TAB, view);
    }

    private String getTabFromSession()
    {
        return (String) ActionContext.getSession().get(SessionKeys.CONFIGURE_PORTAL_PAGES_TAB);
    }

    @Override
    public DropDownModel getDropDownModel(final PortalPageDisplayBean displayBean, final int listIndex)
    {
        DropDownModelBuilder builder = DropDownModelBuilder.builder();

        builder.setTopText(getText("common.words.operations"));
        builder.startSection();
        if (displayBean.isCurrentOwner())
        {
                    builder.addItem(
                            builder.item()
                                    .setText(getText("common.words.edit"))
                                    .setAttr("id", "edit_" + listIndex)
                                    .setAttr("href", toUrl(displayBean, "EditPortalPage!default.jspa", "pageId", true))
                    )
                    .addItem(
                            builder.item()
                                    .setText(getText("common.words.delete"))
                                    .setAttr("id", "delete_" + listIndex)
                                    .setAttr("class", "delete_dash")
                                    .setAttr("href", toUrl(displayBean, "DeletePortalPage!default.jspa", "pageId", false))
                    );
        }
        builder.addItem(
                builder.item()
                        .setText(getText("common.words.copy"))
                        .setAttr("id", "clone_" + listIndex)
                        .setAttr("href", toUrl(displayBean, "AddPortalPage!default.jspa", "clonePageId", false)));
        builder.endSection();
        return builder.build();
    }

    protected String toUrl(PortalPageDisplayBean pageDisplayBean, final String page, final String idUrlVariable, final boolean includeReturnUrl)
    {
        StringBuilder url = new StringBuilder(page);
        url.append("?" + idUrlVariable + "=").append(pageDisplayBean.getId());
        if (includeReturnUrl && StringUtils.isNotBlank(getReturnUrl()))
        {
            url.append("&returnUrl=").append(getReturnUrl());
        }
        return getUriValidator().getSafeUri("", url.toString());
    }



    /**
     * Container for tab names.
     */
    private static final class Tab
    {
        public static final String POPULAR = "popular";
        public static final String SEARCH = "search";
        public static final String FAVOURITES = "favourites";
        public static final String MY = "my";

        public static final String DEFAULT_TAB = FAVOURITES;

        private Tab()
        {}
    }

    /**
     * Class to help executing and displaying the results of PortalPage searches.
     *
     * @since v3.13
     */
    protected static class PortalPageViewHelper extends SharedEntitySearchViewHelper<PortalPage>
    {
        private final PortalPageService portalPageService;

        public PortalPageViewHelper(final ShareTypeFactory shareTypeFactory, final JiraAuthenticationContext authCtx, final String applicationContext, final String actionUrlPrefix, final PortalPageService portalPageService)
        {
            super(shareTypeFactory, authCtx, applicationContext, actionUrlPrefix, "view", "search", PortalPage.ENTITY_TYPE);
            this.portalPageService = portalPageService;
        }

        @Override
        public SharedEntitySearchContext getEntitySearchContext()
        {
            return SharedEntitySearchContext.USE;
        }

        protected SharedEntitySearchResult<PortalPage> doExecuteSearch(final JiraServiceContext ctx, final SharedEntitySearchParameters searchParameters, final int pageOffset, final int pageWidth)
        {
            return portalPageService.search(ctx, searchParameters, pageOffset, pageWidth);
        }

        protected boolean validateSearchParameters(final JiraServiceContext ctx, final SharedEntitySearchParameters searchParameters, final int pageOffset, final int pageWidth)
        {
            portalPageService.validateForSearch(ctx, searchParameters);
            return !ctx.getErrorCollection().hasAnyErrors();
        }
    }
}
