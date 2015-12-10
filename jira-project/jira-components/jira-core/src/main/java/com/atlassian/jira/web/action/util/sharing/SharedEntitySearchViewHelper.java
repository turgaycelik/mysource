package com.atlassian.jira.web.action.util.sharing;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.sharing.SharedEntityColumn;
import com.atlassian.jira.sharing.search.GroupShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.ProjectShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.SharedEntitySearchContext;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.sharing.type.GroupShareType;
import com.atlassian.jira.sharing.type.ProjectShareType;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.sharing.type.ShareTypeRenderer.RenderMode;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.web.bean.ShareTypeRendererBean;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class that makes searching for Shared Entities easier.
 *
 * @since v3.13
 */
public abstract class SharedEntitySearchViewHelper<E extends SharedEntity>
{
    private static final String ERRORKEY_SHARES = "shares";
    private final TypeDescriptor entityType;

    /**
     * Container for sorting constants.
     */
    public static final class SortColumn
    {
        public static final String FAVCOUNT = "favcount";
        public static final String NAME = "name";
        public static final String OWNER = "owner";
        public static final String DESCRIPTION = "description";

        public static final String DEFAULT_SORT = NAME;

        private static final Set<String> COLUMNS;

        static
        {
            final Set<String>columns = new HashSet<String>();
            columns.add(FAVCOUNT);
            columns.add(NAME);
            columns.add(OWNER);
            columns.add(DESCRIPTION);

            COLUMNS = Collections.unmodifiableSet(columns);
        }

        /**
         * Provide a default-backed column name for sorting.
         *
         * @param requestedSort null or a column name.
         * @return the column name requested or a default if null.
         */
        public static String getValidSortColumn(final String requestedSort)
        {
            final String lowerSort = requestedSort != null ? requestedSort.toLowerCase() : null;
            return (!isValid(lowerSort) ? DEFAULT_SORT : lowerSort);
        }

        public static boolean isValid(final String requestedSort)
        {
            return (requestedSort != null) && COLUMNS.contains(requestedSort);
        }

        private SortColumn()
        {}
    }

    /**
     * Container for search field related constants.
     */
    private static final class Search
    {
        private static final String ANY = "any";
        private static final String GROUP = "group";
        private static final String PROJECT = "project";
        private static final String TYPE = "type";
        private static final String PARAM1 = "param1";
        private static final String PARAM2 = "param2";

        private Search()
        {}
    }

    /**
     * Maps of the values that are needed for view/display and the objects that are need for the search API itself
     */
    private static final Map<String, SharedEntityColumn> SORT_COLUMN_TO_SHARED_ENTITY_COLUMN_MAP =
            ImmutableMap.of
                    (
                            SortColumn.DESCRIPTION, SharedEntityColumn.DESCRIPTION,
                            SortColumn.FAVCOUNT, SharedEntityColumn.FAVOURITE_COUNT,
                            SortColumn.NAME, SharedEntityColumn.NAME,
                            SortColumn.OWNER, SharedEntityColumn.OWNER
                    );

    /**
     * The number of favourites to show.
     */
    private static final int PAGE_WIDTH_FAVOURITE = 20;

    /**
     * The number of filters on one page of results.
     */
    private static final int PAGE_WIDTH_DEFAULT = 20;

    private final ShareTypeFactory shareTypeFactory;
    private final JiraAuthenticationContext authCtx;
    private final String applicationContext;
    private final String actionUrlPrefix;

    /**
     * The URL parameter name for the search view.
     */
    private final String viewParameter;

    /**
     * The URL parameter value for the search view.
     */
    private final String viewValue;

    //
    // Search Parameters.
    //
    private String searchName = null;
    private String searchOwnerUserName = null;
    private String searchShareType = null;
    private String groupShare = null;
    private Long pagingOffset = null;
    private Long projectShare = null;
    private Long roleShare = null;

    private String sortColumn = SortColumn.DEFAULT_SORT;
    private boolean sortAscending = true;

    private List<ShareTypeRendererBean> shareTypes;

    public SharedEntitySearchViewHelper(final ShareTypeFactory shareTypeFactory, final JiraAuthenticationContext authCtx, final String applicationContext,
            final String actionUrlPrefix, final String viewParameter, final String viewValue, final TypeDescriptor entityType)
    {
        Assertions.notNull("shareTypeFactory", shareTypeFactory);
        Assertions.notNull("authCtx", authCtx);
        Assertions.notBlank("actionUrlPrefix", actionUrlPrefix);
        Assertions.notBlank("viewParameter", viewParameter);
        Assertions.notBlank("viewValue", viewValue);
        Assertions.notNull("entityType", entityType);

        this.entityType = entityType;
        this.shareTypeFactory = shareTypeFactory;
        this.authCtx = authCtx;
        this.actionUrlPrefix = actionUrlPrefix;
        this.viewParameter = viewParameter;
        this.viewValue = viewValue;
        this.applicationContext = applicationContext;
    }

    //
    // Search state bean methods.
    //
    public String getSearchName()
    {
        return searchName;
    }

    public void setSearchName(final String searchName)
    {
        this.searchName = searchName;
    }

    public String getSearchOwnerUserName()
    {
        return searchOwnerUserName;
    }

    public void setSearchOwnerUserName(final String searchOwnerUserName)
    {
        this.searchOwnerUserName = searchOwnerUserName;
    }

    public String getSearchShareType()
    {
        return searchShareType;
    }

    public void setSearchShareType(final String searchShareType)
    {
        this.searchShareType = searchShareType;
    }

    public void setGroupShare(final String groupShare)
    {
        this.groupShare = groupShare;
    }

    public String getGroupShare()
    {
        return groupShare;
    }

    public Long getPagingOffset()
    {
        return pagingOffset;
    }

    public void setProjectShare(final String projectShare)
    {
        try
        {
            this.projectShare = Long.valueOf(projectShare);
        }
        catch (final NumberFormatException e)
        {
            this.projectShare = null;
        }
    }

    public String getProjectShare()
    {
        return projectShare == null ? null : projectShare.toString();
    }

    public void setRoleShare(final String roleShare)
    {
        try
        {
            this.roleShare = Long.valueOf(roleShare);
        }
        catch (final NumberFormatException e)
        {
            this.roleShare = null;
        }
    }

    public String getRoleShare()
    {
        return roleShare == null ? null : roleShare.toString();
    }

    public void setPagingOffset(final Long pagingOffset)
    {
        this.pagingOffset = pagingOffset;
    }

    public String getSortColumn()
    {
        return sortColumn;
    }

    public void setSortColumn(final String sortColumn)
    {
        this.sortColumn = SortColumn.getValidSortColumn(sortColumn);
    }

    public boolean isSortAscending()
    {
        return sortAscending;
    }

    public void setSortAscending(final boolean sortAscending)
    {
        this.sortAscending = sortAscending;
    }

    public abstract SharedEntitySearchContext getEntitySearchContext();

    /**
     * Returns the {@link com.atlassian.jira.web.bean.ShareTypeRendererBean ShareTypeRendererBeans} used by the front end to render each type of
     * sharing that a filter could have. Does lazy initialisation so it's performant to call multiple times in a view. Is only used by the search
     * form.
     *
     * @return the available {@link com.atlassian.jira.sharing.type.ShareType ShareTypes}.
     */
    public List<ShareTypeRendererBean> getShareTypeRendererBeans()
    {
        if (shareTypes == null)
        {
            shareTypes = new ArrayList<ShareTypeRendererBean>();
            shareTypes.add(new ShareTypeRendererBean(shareTypeFactory.getShareType(GroupShareType.TYPE), authCtx, RenderMode.SEARCH, entityType));
            shareTypes.add(new ShareTypeRendererBean(shareTypeFactory.getShareType(ProjectShareType.TYPE), authCtx, RenderMode.SEARCH, entityType));
        }
        return shareTypes;
    }

    /**
     * Return the URL that can be used to sort by the passed column.
     *
     * @param sortColumnName the column to sort by.
     * @return the URL that can be used to sort the passed column.
     */
    public String generateSortUrl(final String sortColumnName)
    {
        final String actualSortColumn = SortColumn.getValidSortColumn(sortColumnName);

        final StringBuffer url = createBasicUrlSearchParams();
        addParameter(url, "sortColumn", actualSortColumn);

        boolean doSortAscending = true;
        if (actualSortColumn.equalsIgnoreCase(getSortColumn()))
        {
            // user clicked column to get the column to resort in the other order.
            doSortAscending = !isSortAscending();
        }
        else if (actualSortColumn.equalsIgnoreCase(SortColumn.FAVCOUNT))
        {
            // Popularity is defaulted to descending order
            doSortAscending = false;
        }
        addParameter(url, "sortAscending", doSortAscending);
        addParameter(url, "pagingOffset", 0);

        return url.toString();
    }

    /**
     * Return the HTML for the sort icon used to display the sort order for the passed column. The returned String can be empty if the passed passed
     * column should not display an icon.
     *
     * @param sortColumnName the name of the column
     * @return the HTML for the icon to display.
     */
    public String generateSortIcon(final String sortColumnName)
    {
        final String actualSortColumnName = SortColumn.getValidSortColumn(sortColumnName);

        // non sorted columns should not see any icons.
        if (!actualSortColumnName.equalsIgnoreCase(getSortColumn()))
        {
            return "";
        }

        final String altText;
        final String img;
        if (isSortAscending())
        {
            // JRA-14000 This is intentionally using the old arrow, the blue arrows look bad here
            // Also, the standard size makes the header row too tall - using a smaller version
            img = "icon_sortascending.png";
            altText = authCtx.getI18nHelper().getText("navigator.ascending.order");
        }
        else
        {
            // JRA-14000 This is intentionally using the old arrow, the blue arrows look bad here
            // Also, the standard size makes the header row too tall - using a smaller version
            img = "icon_sortdescending.png";
            altText = authCtx.getI18nHelper().getText("navigator.descending.order");
        }
        final StringBuilder sb = new StringBuilder("<img class=\"sortArrow\" src=\"");
        if (StringUtils.isNotBlank(applicationContext))
        {
            sb.append(applicationContext);
        }
        sb.append("/images/icons/").append(img).append("\" alt=\"").append(altText).append("\" />");
        return sb.toString();
    }

    /**
     * Return the CSS class(es) to display the passed column's sort status.
     *
     * @param sortColumnName the column to get the classes for.
     * @return the CSS class(es) to represent the passed column's sorting status.
     */
    public String generateSortCssClass(final String sortColumnName)
    {
        final String actualSortColumnName = SortColumn.getValidSortColumn(sortColumnName);

        final StringBuilder css = new StringBuilder("colHeaderSortable ");
        if (actualSortColumnName.equalsIgnoreCase(getSortColumn()))
        {
            css.append("colHeaderOver");
        }
        else
        {
            css.append("colHeaderLink");
        }
        return css.toString();
    }

    /**
     * Performs the search for filters based on the configured state.
     *
     * @param jiraServiceContext the service context to use for performing the search with the underlying service.
     * @return the SearchResult for the search currently configured. null can be returned to indicate an error.
     */
    public SearchResult<E> search(final JiraServiceContext jiraServiceContext)
    {
        if (!jiraServiceContext.getErrorCollection().hasAnyErrors())
        {
            final SharedEntitySearchParameters parameters = buildSearchParameters(jiraServiceContext);
            // if parameters are null we should have an error message already, so let's do nothing
            final int pagingOffset = getSanePagingOffset();
            if ((parameters != null) && validateSearchParameters(jiraServiceContext, parameters, pagingOffset, PAGE_WIDTH_DEFAULT))
            {
                final SharedEntitySearchResult<E> searchResult = doExecuteSearch(jiraServiceContext, parameters, pagingOffset, PAGE_WIDTH_DEFAULT);
                return createSearchResults(searchResult, pagingOffset, PAGE_WIDTH_DEFAULT);
            }
        }
        return null;
    }

    /**
     * Gets the filters which have been made favourite by the most people.
     *
     * @param jiraServiceContext the service context to use for performing the search.
     * @return the list of {@link com.atlassian.jira.sharing.SharedEntity} objects.
     */
    public List<E>getPopularFilters(final JiraServiceContext jiraServiceContext)
    {
        final SharedEntitySearchParametersBuilder popularSearchParameters = new SharedEntitySearchParametersBuilder();
        popularSearchParameters.setSortColumn(SharedEntityColumn.FAVOURITE_COUNT, false);
        popularSearchParameters.setEntitySearchContext(getEntitySearchContext());

        final SharedEntitySearchResult<E> sharedEntitySearchResult = doExecuteSearch(jiraServiceContext, popularSearchParameters.toSearchParameters(),
            0, PAGE_WIDTH_FAVOURITE);
        if ((sharedEntitySearchResult != null) && !jiraServiceContext.getErrorCollection().hasAnyErrors() && !sharedEntitySearchResult.isEmpty())
        {
            return sharedEntitySearchResult.getResults();
        }
        else
        {
            return Collections.emptyList();
        }
    }

    /**
     * Produces a JSON representation of the current form state. Used to pre-populate the JavaScript-controlled component in select-share-types.jsp.
     *
     * @return the JSON representation to be used by the client to set up the search form controls state.
     */
    public String getSearchShareTypeJSON()
    {
        final Map<String, Object> jsonMap = new HashMap<String, Object>();
        if (Search.GROUP.equalsIgnoreCase(getSearchShareType()))
        {
            jsonMap.put(Search.TYPE, Search.GROUP);
            if (getGroupShare() != null)
            {
                jsonMap.put(Search.PARAM1, getGroupShare());
            }
        }
        else if (Search.PROJECT.equalsIgnoreCase(getSearchShareType()))
        {
            jsonMap.put(Search.TYPE, Search.PROJECT);
            if (getProjectShare() != null)
            {
                jsonMap.put(Search.PARAM1, getProjectShare());
            }
            if (getRoleShare() != null)
            {
                jsonMap.put(Search.PARAM2, getRoleShare());
            }
        }
        else
        {
            jsonMap.put(Search.TYPE, Search.ANY);
        }
        final JSONArray jsonArray = new JSONArray();
        jsonArray.put(new JSONObject(jsonMap));
        return jsonArray.toString();
    }

    /**
     * Uses the state in the search form to create a {@link com.atlassian.jira.sharing.search.SharedEntitySearchParameters}. Null return always
     * implies an error message has been added to jiraServiceContext.
     *
     * @param jiraServiceContext the JiraServiceContext to be used for validating the search params during building.
     * @return null if the search parameters don't stack up.
     */
    private SharedEntitySearchParameters buildSearchParameters(final JiraServiceContext jiraServiceContext)
    {
        final SharedEntitySearchParametersBuilder builder = new SharedEntitySearchParametersBuilder();
        builder.setName(StringUtils.isBlank(getSearchName()) ? null : getSearchName());
        builder.setDescription(StringUtils.isBlank(getSearchName()) ? null : getSearchName());
        builder.setUserName(StringUtils.isBlank(getSearchOwnerUserName()) ? null : getSearchOwnerUserName());
        // we are using OR searching at the moment. This may change in the future
        builder.setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.OR);
        // what are we sorting on
        builder.setSortColumn(mapSortColumnToSharedEntityColumn(), isSortAscending());

        // are we executing and admin search or use search
        builder.setEntitySearchContext(getEntitySearchContext());

        // we should only limit the search by parameters if there is a logged in user.
        if (jiraServiceContext.getLoggedInUser() != null)
        {
            // what share types do they want to filter on
            if (Search.GROUP.equalsIgnoreCase(getSearchShareType()))
            {
                builder.setShareTypeParameter(new GroupShareTypeSearchParameter(getGroupShare()));
            }
            else if (Search.PROJECT.equalsIgnoreCase(getSearchShareType()))
            {
                final Long projectShareLong = asLongOrNull(getProjectShare());
                // if they have URL hacked the project to a non number value then this may be true.
                if (projectShareLong == null)
                {
                    jiraServiceContext.getErrorCollection().addError(ERRORKEY_SHARES,
                        jiraServiceContext.getI18nBean().getText("common.sharing.shared.illegal.search.parameters"));
                    return null;
                }
                builder.setShareTypeParameter(new ProjectShareTypeSearchParameter(projectShareLong, asLongOrNull(getRoleShare())));
            }
        }

        return builder.toSearchParameters();
    }

    /**
     * Creates the basic URL parameters needed to keep the current search state.
     *
     * @return a string buffer containing a URL with the basic search parameters.
     */
    protected StringBuffer createBasicUrlSearchParams()
    {
        final StringBuffer sb = new StringBuffer().append(actionUrlPrefix);
        final boolean gotQMark = actionUrlPrefix.contains("?");
        sb.append(gotQMark ? "&" : "?"); // if we already have a ? then go to &
        sb.append("Search=Search");
        addParameter(sb, viewParameter, viewValue);
        addParameter(sb, "searchName", getSearchName());
        addParameter(sb, "searchOwnerUserName", getSearchOwnerUserName());
        addParameter(sb, "searchShareType", getSearchShareType());
        addParameter(sb, "projectShare", getProjectShare());
        addParameter(sb, "roleShare", getRoleShare());
        addParameter(sb, "groupShare", getGroupShare());

        return sb;
    }

    protected StringBuffer addParameter(final StringBuffer url, final String key, final Object value)
    {
        return url.append('&').append(key).append('=').append(urlenc(value));
    }

    /**
     * Create a URL to move to the passed pageNumber
     *
     * @param pageNumber the page number to move to.
     * @return the generated URL.
     */
    private String makePagingUrl(final int pageNumber)
    {
        final StringBuffer url = createBasicUrlSearchParams();
        addParameter(url, "pagingOffset", pageNumber);
        addParameter(url, "sortAscending", isSortAscending());
        addParameter(url, "sortColumn", getSortColumn());

        return url.toString();
    }

    /**
     * URL encode the string representation of passed object.
     *
     * @param input the object whose string representation is to be escaped.
     * @return the URL encoded version of the passed object's string representation.
     */
    String urlenc(final Object input)
    {
        if (input == null)
        {
            return "";
        }
        return JiraUrlCodec.encode(input.toString());
    }

    /**
     * Return the {@link SharedEntityColumn} associated with the
     * {@link com.atlassian.jira.web.action.util.sharing.SharedEntitySearchViewHelper.SortColumn}
     *
     * @return the mapped SharedEntityColumn.
     */

    private SharedEntityColumn mapSortColumnToSharedEntityColumn()
    {
        SharedEntityColumn columnToUse = SharedEntityColumn.NAME;
        if (!StringUtils.isBlank(getSortColumn()))
        {
            columnToUse = SORT_COLUMN_TO_SHARED_ENTITY_COLUMN_MAP.get(getSortColumn());
            if (columnToUse == null)
            {
                columnToUse = SharedEntityColumn.NAME;
            }
        }
        return columnToUse;
    }

    /**
     * Returns the given String parsed as a Long or null if there's a NumberFormatException.
     *
     * @param longString a String representation of a Long.
     * @return a Long, possibly null.
     */
    private Long asLongOrNull(final String longString)
    {
        try
        {
            return Long.valueOf(longString);
        }
        catch (final NumberFormatException e)
        {
            return null;
        }
    }

    private int getSanePagingOffset()
    {
        final Long pagingOffset = getPagingOffset();
        return pagingOffset == null ? 0 : Math.max(0, pagingOffset.intValue());
    }

    /**
     * Return the SearchResult object for the passed parameters.
     *
     * @param result     the result of a search.
     * @param pageOffset the page offset.
     * @param pageSize   the size of the pages.
     * @return the SearchResult object for the passed parameters.
     */
    private SearchResult<E> createSearchResults(final SharedEntitySearchResult<E> result, final int pageOffset, final int pageSize)
    {
        if (result == null)
        {
            return null;
        }
        else if (result.isEmpty())
        {
            return SearchResult.emptyResult();
        }
        else
        {
            final String nextUrl = result.hasMoreResults() ? makePagingUrl(pageOffset + 1) : null;
            final String previousUrl = pageOffset > 0 ? makePagingUrl(pageOffset - 1) : null;

            final int startingResultNo = pageOffset * pageSize;
            final int startNo = startingResultNo + 1;
            final List<E> filterList = result.getResults();
            final int endNo = startingResultNo + filterList.size();

            return new SearchResult<E>(filterList, nextUrl, previousUrl, startNo, endNo, result.getTotalResultCount());
        }
    }

    /**
     * Represents a page of results of the search. Contains view specific information related to results of the search.
     *
     * @since 3.13.
     */
    public static final class SearchResult<E>
    {
        /**
         * Represents no filters returned.
         */
        @SuppressWarnings ({ "unchecked" })
        public static final SearchResult<?> EMPTY_RESULTS = new SearchResult<Object>(Collections.emptyList(), null, null, -1, -1, -1);

        /**
         * The list of {@link com.atlassian.jira.sharing.SharedEntity} objects found.
         */
        private final List<E> results;

        /**
         * The string representation of the URL to return the next search results or null if no such results exist.
         */
        private final String nextUrl;

        /**
         * The string representation of the URL to return the previous search results or null if no such results exist.
         */
        private final String previousUrl;

        /**
         * The position of the first result in the search order. Can be -1 if no search results exist.
         */
        private final int startResultPosition;

        /**
         * The position of the last result in the search order. Can be -1 if no search results exist.
         */
        private final int endResultPosition;
        /**
         * The total number of results found, beyond the actual page size
         */
        private final int totalResultCount;

        private SearchResult(final List<E> results, final String nextUrl, final String previousUrl, final int startResultPosition, final int endResultPosition, final int totalResultCount)
        {
            this.results = results;
            this.nextUrl = nextUrl;
            this.previousUrl = previousUrl;
            this.startResultPosition = startResultPosition;
            this.endResultPosition = endResultPosition;
            this.totalResultCount = totalResultCount;
        }

        public List<E> getResults()
        {
            return results;
        }

        public String getNextUrl()
        {
            return nextUrl;
        }

        public String getPreviousUrl()
        {
            return previousUrl;
        }

        public int getStartResultPosition()
        {
            return startResultPosition;
        }

        public int getEndResultPosition()
        {
            return endResultPosition;
        }

        public int getTotalResultCount()
        {
            return totalResultCount;
        }

        @SuppressWarnings ({ "unchecked" })
        public static<E> SearchResult<E> emptyResult()
        {
            return (SearchResult<E>) EMPTY_RESULTS;
        }
    }

    /**
     * Execute the search for the passed parameters.
     *
     * @param ctx              the context to execute the search under.
     * @param searchParameters the parameters for the search.
     * @param pageOffset       the offset of the search result page to return.
     * @param pageWidth        the width of a search result page.
     * @return the result of the search or null if an error occurred.
     */
    protected abstract SharedEntitySearchResult<E> doExecuteSearch(JiraServiceContext ctx, SharedEntitySearchParameters searchParameters, int pageOffset, int pageWidth);

    /**
     * Ensure that it is possible to execute a search with the passed parameters.
     *
     * @param ctx              the context to execute the search under.
     * @param searchParameters the parameters for the search.
     * @param pageOffset       the offset of the search result page to return.
     * @param pageWidth        the width of a search result page.
     * @return true iff the search can be performed.
     */
    protected abstract boolean validateSearchParameters(JiraServiceContext ctx, SharedEntitySearchParameters searchParameters, int pageOffset, int pageWidth);
}
