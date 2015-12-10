package com.atlassian.jira.web.action.filter;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.renderer.ProjectDescriptionRenderer;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.util.SearchRequestDisplayBean;
import com.atlassian.jira.web.action.util.sharing.SharedEntitySearchAction;
import com.atlassian.jira.web.action.util.sharing.SharedEntitySearchViewHelper;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Action implementation for the Filter Picker popup.
 *
 * @since v3.13
 */
public class FilterPickerPopup extends JiraWebActionSupport implements ProjectFetcher, SharedEntitySearchAction
{
    private static final FilterLinkRenderer FILTER_PICKER_CALLBACK_LINK_RENDERER = new FilterLinkRenderer()
    {
        public String render(final Long id, final String name)
        {
            return new StringBuilder().append("<a href=\"#\" id=\"filterlink_").append(id).append("\" onclick=\"submitFilter('").append(id).append(
                "', this);\"'>").append(TextUtils.htmlEncode(name)).append("</a>").toString();
        }
    };

    private static final FilterLinkRenderer PROJECT_PICKER_CALLBACK_LINK_RENDERER = new FilterLinkRenderer()
    {
        public String render(final Long id, final String name)
        {
            return new StringBuilder().append("<a href=\"#\" id=\"filterlink_").append(id).append("\" onclick=\"submitProject('").append(id).append(
                "', this);\"'>").append(TextUtils.htmlEncode(name)).append("</a>").toString();
        }
    };

    private final SearchRequestService searchRequestService;
    private final PermissionManager permissionManager;
    private final ProjectManager projectManager;
    private final ProjectFactory projectFactory;
    private final SearchRequestDisplayBean.Factory beanFactory;
    private final FilterPickerPopupViewHelper filterHelper;
    private final ProjectDescriptionRenderer projectDescriptionRenderer;

    private String filterView = null;

    private List<SearchRequestDisplayBean> filters = null;
    private SharedEntitySearchViewHelper.SearchResult searchResults = null;
    private FilterViewTabs validTabs = null;

    public FilterPickerPopup(final SearchRequestService searchRequestService, final ShareTypeFactory shareTypeFactory, final PermissionManager permissionManager, final JiraAuthenticationContext authCtx, final ProjectManager projectManager, final ProjectFactory projectFactory, final SearchRequestDisplayBean.Factory beanFactory,
            final ProjectDescriptionRenderer projectDescriptionRenderer)
    {
        this.searchRequestService = searchRequestService;
        this.permissionManager = permissionManager;
        this.projectManager = projectManager;
        this.projectFactory = projectFactory;
        this.beanFactory = beanFactory;
        this.projectDescriptionRenderer = projectDescriptionRenderer;

        filterHelper = new FilterPickerPopupViewHelper(shareTypeFactory, authCtx, ActionContext.getRequest().getContextPath(),
            "FilterPickerPopup.jspa", searchRequestService);
    }

    //
    //Implementations of the SharedEntityAction interface.
    //

    public String getSearchName()
    {
        return filterHelper.getSearchName();
    }

    public void setSearchName(final String searchName)
    {
        filterHelper.setSearchName(searchName);
    }

    public String getSearchOwnerUserName()
    {
        return filterHelper.getSearchOwnerUserName();
    }

    public void setSearchOwnerUserName(final String searchOwnerUserName)
    {
        filterHelper.setSearchOwnerUserName(searchOwnerUserName);
    }

    public String getSearchShareType()
    {
        return filterHelper.getSearchShareType();
    }

    public void setSearchShareType(final String searchShareType)
    {
        filterHelper.setSearchShareType(searchShareType);
    }

    public void setGroupShare(final String groupShare)
    {
        filterHelper.setGroupShare(groupShare);
    }

    public String getGroupShare()
    {
        return filterHelper.getGroupShare();
    }

    public Long getPagingOffset()
    {
        return filterHelper.getPagingOffset();
    }

    public void setProjectShare(final String projectShare)
    {
        filterHelper.setProjectShare(projectShare);
    }

    public String getProjectShare()
    {
        return filterHelper.getProjectShare();
    }

    public void setRoleShare(final String roleShare)
    {
        filterHelper.setRoleShare(roleShare);
    }

    public String getRoleShare()
    {
        return filterHelper.getRoleShare();
    }

    public void setPagingOffset(final Long pagingOffset)
    {
        filterHelper.setPagingOffset(pagingOffset);
    }

    public String getSortColumn()
    {
        return filterHelper.getSortColumn();
    }

    public void setSortColumn(final String sortColumn)
    {
        filterHelper.setSortColumn(sortColumn);
    }

    public boolean isSortAscending()
    {
        return filterHelper.isSortAscending();
    }

    public void setSortAscending(final boolean sortAscending)
    {
        filterHelper.setSortAscending(sortAscending);
    }

    //
    // Pop-up specific parameters.
    //
    public String getFilterView()
    {
        return filterView;
    }

    public void setFilterView(final String filterView)
    {
        this.filterView = filterView;
    }

    public boolean isShowFilters()
    {
        return filterHelper.isShowFilters();
    }

    public void setShowFilters(final boolean showFilters)
    {
        filterHelper.setShowFilters(showFilters);
    }

    public boolean isShowProjects()
    {
        return filterHelper.isShowProjects();
    }

    public void setShowProjects(final boolean showProjects)
    {
        filterHelper.setShowProjects(showProjects);
    }

    public String getField()
    {
        return filterHelper.getField();
    }

    public void setField(final String field)
    {
        filterHelper.setField(field);
    }

    //
    // View helper methods.
    //

    public List getFilters()
    {
        return filters;
    }

    public boolean isSearchRequested()
    {
        return ActionContext.getParameters().get("Search") != null;
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

    public FilterPickerPopupViewHelper getFiltersViewHelper()
    {
        return filterHelper;
    }

    /**
     * Returns the key for a message to be used in the case when there are no search results. Either there was no
     * search requested, the search failed or it returned no results.
     *
     * @return the key of the message to be rendered in the case where there is
     */
    public String getSearchEmptyMessageKey()
    {
        if (isSearchRequested() && (filters != null))
        {
            return "filters.no.search.results";
        }
        else
        {
            return "common.sharing.searching.no.search.performed";
        }
    }

    public boolean isTabShowing(final String tab)
    {
        return getTabs().isValid(tab);
    }

    public FilterLinkRenderer getFilterLinkRenderer()
    {
        if (isShowProjects())
        {
            if (isShowFilters())
            {
                // we are in filter-or-project mode so we need to choose the renderer to send back the id and the
                // indicator for whether it's a project or a filter
                return new ProjectLinkRenderer(FilterViewTabs.PROJECT.nameEquals(filterView) ? "project" : "filter");
            }
            else
            {
                return PROJECT_PICKER_CALLBACK_LINK_RENDERER;
            }
        }
        else
        {
            return FILTER_PICKER_CALLBACK_LINK_RENDERER;
        }
    }

    /**
     * Determines whether or not the list of projects should be shown grouped by categories. Standard edition doesn't
     * have categories.
     *
     * @return true only if the front-end should display projects in categories.
     */
    public boolean showCategories()
    {
        return someCategoriesExist();
    }

    private boolean someCategoriesExist()
    {
        return !getCategories().isEmpty();
    }

    public Collection /*<GenericValue>*/getCategories()
    {
        return projectManager.getProjectCategories();
    }

    //
    // Action commands.
    //

    protected String doExecute()
    {
        final FilterViewTabs.Tab currentTab = getTabs().getTabSafely(filterView);
        final boolean filterViewSet = filterView != null;

        if (currentTab != null)
        {
            filterView = currentTab.getName();
            if (FilterViewTabs.FAVOURITES == currentTab)
            {
                //if there are no favourites and using default view, then we should display search.
                final boolean hasFavourites = executeFavouriteView();
                if (!hasFavourites && !filterViewSet)
                {
                    filterView = FilterViewTabs.SEARCH.getName();
                    executeSearchView();
                }
            }
            else if (FilterViewTabs.SEARCH == currentTab)
            {
                executeSearchView();
            }
            else if (FilterViewTabs.POPULAR == currentTab)
            {
                executePopularView();
            }
            else if (FilterViewTabs.FAVOURITES == currentTab)
            {
                executeFavouriteView();
            }
            else if (FilterViewTabs.PROJECT != currentTab)
            {
                filterView = null;
            }
        }
        else
        {
            filterView = null;
        }

        return INPUT;
    }

    private void executeSearchView()
    {
        final boolean searchPerformed = isSearchRequested();
        if (searchPerformed)
        {
            final JiraServiceContext ctx = getJiraServiceContext();
            searchResults = filterHelper.search(ctx);
            if ((searchResults != null) && !ctx.getErrorCollection().hasAnyErrors())
            {
                filters = beanFactory.createDisplayBeans(searchResults.getResults());
            }
        }
    }

    /**
     * This action method handles the actual Searching for Popular Shared Filters
     */
    private void executePopularView()
    {
        final JiraServiceContext serviceContext = getJiraServiceContext();
        final List<SearchRequest> results = filterHelper.getPopularFilters(serviceContext);
        if ((results != null) && !serviceContext.getErrorCollection().hasAnyErrors())
        {
            filters = beanFactory.createDisplayBeans(results);
        }
    }

    //
    //Search methods.
    //
    private boolean executeFavouriteView()
    {
        final Collection<SearchRequest> favouriteFilters = searchRequestService.getFavouriteFilters(getLoggedInApplicationUser());
        if (!favouriteFilters.isEmpty())
        {
            filters = beanFactory.createDisplayBeans(favouriteFilters);
            return true;
        }
        else
        {
            return false;
        }
    }

    //
    // Implementation of the ProjectFetcher interface.
    //

    public Collection<Project> getProjectsInCategory(final GenericValue projectCategory)
    {
        final List<Project> projects = new ArrayList<Project>();
        final Collection<GenericValue> generics = permissionManager.getProjects(Permissions.BROWSE, getJiraServiceContext().getLoggedInUser(), projectCategory);
        // undocumented postconditions => better to be safe
        if (generics != null)
        {
            projects.addAll(projectFactory.getProjects(generics));
        }
        return projects;
    }

    public Collection<Project> getProjectsInNoCategory()
    {
        return getProjectsInCategory(null);
    }

    public boolean projectsExist()
    {
        return !projectManager.getProjectObjects().isEmpty();
    }

    public String getRenderedProjectDescription(Project project)
    {
        return projectDescriptionRenderer.getViewHtml(project.getDescription());
    }

    private FilterViewTabs getTabs()
    {
        if (validTabs == null)
        {
            List <FilterViewTabs.Tab> tabs;
            FilterViewTabs.Tab defaultTab;

            if (!isShowFilters()) {
                tabs = new ArrayList<FilterViewTabs.Tab>();
                defaultTab = null;
            }
            else if (getLoggedInUser() == null)
            {
                tabs = CollectionBuilder.newBuilder(FilterViewTabs.POPULAR, FilterViewTabs.SEARCH).asMutableList();
                defaultTab = FilterViewTabs.POPULAR;
            }
            else
            {
                tabs = CollectionBuilder.newBuilder(FilterViewTabs.FAVOURITES, FilterViewTabs.POPULAR, FilterViewTabs.SEARCH).asMutableList();
                defaultTab = FilterViewTabs.FAVOURITES;
            }

            if (isShowProjects())
            {
                tabs.add(FilterViewTabs.PROJECT);
                if (defaultTab == null)
                {
                    defaultTab = FilterViewTabs.PROJECT;
                }
            }

            validTabs = new FilterViewTabs(tabs, defaultTab);
        }

        return validTabs;
    }

    /**
     * Helper class to display the filter popup.
     */
    private static class FilterPickerPopupViewHelper extends FilterViewHelper
    {
        private boolean showFilters = true;
        private boolean showProjects = false;
        private String field;

        public FilterPickerPopupViewHelper(final ShareTypeFactory shareTypeFactory,
                final JiraAuthenticationContext authCtx, final String applicationContext, final String actionUrlPrefix,
                final SearchRequestService searchRequestService)
        {
            super(shareTypeFactory, authCtx, applicationContext, actionUrlPrefix, searchRequestService);
        }

        public boolean isShowFilters()
        {
            return showFilters;
        }

        public void setShowFilters(final boolean showFilters)
        {
            this.showFilters = showFilters;
        }

        public boolean isShowProjects()
        {
            return showProjects;
        }

        public void setShowProjects(final boolean showProjects)
        {
            this.showProjects = showProjects;
        }

        public String getField()
        {
            return field;
        }

        public void setField(final String field)
        {
            this.field = field;
        }

        protected StringBuffer createBasicUrlSearchParams()
        {
            final StringBuffer searchParams = super.createBasicUrlSearchParams();
            addParameter(searchParams, "field", field);
            if (showProjects)
            {
                addParameter(searchParams, "showProjects", showProjects);
            }
            return searchParams;
        }
    }

    /**
     * Helps render the links on the popup.
     */
    private static class ProjectLinkRenderer implements FilterLinkRenderer
    {
        private final String outputField;

        public ProjectLinkRenderer(final String outputField)
        {
            this.outputField = outputField;
        }

        public String render(final Long id, final String name)
        {
            final StringBuilder link = new StringBuilder();
            return link.append("<a href=\"#\" id=\"filterlink_").append(id).append("\" onclick=\"submitFilterOrProject('").append(id).append(
                "', this, '").append(outputField).append("');\"'>").append(TextUtils.htmlEncode(name)).append("</a>").toString();
        }
    }
}
