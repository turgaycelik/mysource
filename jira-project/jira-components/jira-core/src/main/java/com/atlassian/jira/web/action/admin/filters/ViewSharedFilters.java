package com.atlassian.jira.web.action.admin.filters;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.action.filter.FilterLinkRenderer;
import com.atlassian.jira.web.action.filter.FilterOperationsAction;
import com.atlassian.jira.web.action.filter.FilterOperationsBean;
import com.atlassian.jira.web.action.filter.FilterViewHelper;
import com.atlassian.jira.web.action.filter.ManageFilters;
import com.atlassian.jira.web.action.filter.SharedFilterAdministrationViewHelper;
import com.atlassian.jira.web.action.util.SearchRequestDisplayBean;
import com.atlassian.jira.web.action.util.sharing.SharedEntitySearchAction;
import com.atlassian.jira.web.ui.model.DropDownModel;
import com.atlassian.jira.web.ui.model.DropDownModelBuilder;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Responsible for displaying the shared filters administration page.
 *
 * @since v4.4.1
 */
@WebSudoRequired
public class ViewSharedFilters extends ManageFilters implements FilterOperationsAction, SharedEntitySearchAction
{
    private static final FilterLinkRenderer NO_LINK_RENDERER = new FilterLinkRenderer()
    {
        public String render(final Long id, final String name)
        {
            return "<span data-filter-field=\"name\">" + TextUtils.htmlEncode(name) + "</span>";
        }
    };

    private final JiraAuthenticationContext authCtx;
    private final SearchRequestService searchRequestService;
    private final ShareTypeFactory shareTypeFactory;
    private final FilterViewHelper filterViewHelper;
    private final static String OWNER = "filters.searchOwnerUserName";
    private final static String NAME = "filters.searchName";

    private boolean useParentSearch = true;
    private boolean searchPerformed = false;


    public ViewSharedFilters(final JiraAuthenticationContext authCtx, final IssueSearcherManager issueSearcherManager,
            final SearchRequestService searchRequestService, final ShareTypeFactory shareTypeFactory,
            final SearchRequestDisplayBean.Factory beanFactory, final SearchService searchService,
            final SearchSortUtil searchSortUtil, final WebResourceManager webResourceManager)
    {
        super(authCtx, issueSearcherManager, searchRequestService, shareTypeFactory, beanFactory, searchService, searchSortUtil, webResourceManager);
        this.authCtx = authCtx;
        this.searchRequestService = searchRequestService;
        this.shareTypeFactory = shareTypeFactory;
        filterViewHelper = new SharedFilterAdministrationViewHelper(shareTypeFactory, authCtx, ActionContext.getRequest().getContextPath(),
                "ViewSharedFilters.jspa", searchRequestService);
    }

    @Override
    public DropDownModel getDropDownModel(SearchRequestDisplayBean displayBean, int listIndex)
    {
        DropDownModelBuilder builder = DropDownModelBuilder.builder();

        builder.setTopText(getText("common.words.operations"));
        builder.startSection()
                .addItem
                        (
                                builder.item()
                                        .setText(getText("sharedfilters.admin.cog.changeowner"))
                                        .setAttr("id", "change_owner_" + displayBean.getId())
                                        .setAttr("class", "change-owner")
                                        .setAttr("href", toUrl(displayBean, "ChangeSharedFilterOwner!default.jspa", true) + buildQueryStringForModel(ExecutingHttpRequest.get()))
                                        .setAttr("rel", "" + displayBean.getId())
                        )
                .addItem
                        (
                                builder.item()
                                        .setText(getText("sharedfilters.delete"))
                                        .setAttr("id", "delete_" + displayBean.getId())
                                        .setAttr("class", "delete-filter")
                                        .setAttr("href", toUrl(displayBean, "DeleteSharedFilter!default.jspa", true)+ buildQueryStringForModel(ExecutingHttpRequest.get()))
                                        .setAttr("rel", "" + displayBean.getId())
                        );

        builder.endSection();
        return builder.build();
    }

    private String buildQueryStringForModel(HttpServletRequest request)
    {
        final StringBuilder builder = new StringBuilder("");
        if (request.getMethod().equalsIgnoreCase("POST"))
        {
            appendParameter(builder, "searchOwnerUserName", getSearchOwnerUserName());
            appendParameter(builder, "searchName", getSearchName());
        }
        else
        {
            if (request.getQueryString()  != null) {
                builder.append("&");
                builder.append(request.getQueryString());
            }
        }
        appendParameter(builder, "totalResultCount", ""+getTotalResultCount());
        return builder.toString();
    }

    private StringBuilder appendParameter(StringBuilder builder, final String param, final String value)
    {
        return builder.append("&").append(param).append("=").append(JiraUrlCodec.encode(value));
    }

    @Override
    protected FilterViewHelper getFilterHelper()
    {
        return filterViewHelper;
    }

    @Override
    public String doDefault() throws Exception
    {
        return super.doDefault();
    }

    @Override
    protected void doValidation()
    {
        super.doValidation();
    }

    @Override
    protected String doExecute()
    {
        useParentSearch = true;
        setReturnUrl(String.format("ViewSharedFilters.jspa"));
        ActionContext.getSession().put(OWNER, getSearchOwnerUserName());
        ActionContext.getSession().put(NAME, getSearchName());
        return executeSearch();
    }

    @Override
    public FilterLinkRenderer getFilterLinkRenderer()
    {
        return NO_LINK_RENDERER;
    }

    @Override
    public FilterOperationsBean getFilterOperationsBean()
    {
        return super.getFilterOperationsBean();
    }

    private String executeSearch()
    {
        searchPerformed = true;
        setReturnUrl(String.format("ViewSharedFilters.jspa"));
        final JiraServiceContext ctx = getJiraServiceContext();
        FilterViewHelper.SearchResult searchResults = getFilterHelper().search(ctx);
        setSearchResults(searchResults);
        if ((searchResults == null) || ctx.getErrorCollection().hasAnyErrors())
        {
            searchPerformed = false;
        }
        else
        {
            setFilters(beanFactory.createDisplayBeans(searchResults.getResults()));
        }
        return isContentOnly() ? CONTENTONLY : SUCCESS;
    }

    @Override
    public boolean isSearchRequested()
    {
        return useParentSearch ? super.isSearchRequested() : searchPerformed;
    }
}
