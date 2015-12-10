package com.atlassian.jira.web.action.issue;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.issue.navigator.ToolOptionGroup;
import com.atlassian.jira.web.action.issue.navigator.ToolOptionItem;
import com.atlassian.query.Query;

import org.apache.log4j.Logger;

/**
 * Utility for determining tool options to display on the issue navigator
 */
public class IssueNavigatorToolsHelper
{
    private static final Logger log = Logger.getLogger(IssueNavigatorToolsHelper.class);

    private final JiraAuthenticationContext authContext;
    private final PermissionManager permissionManager;
    private final ApplicationProperties applicationProperties;
    private final ColumnLayoutManager columnLayoutManager;
    private SearchRequestInfo searchRequestInfo;
    private SearchResultInfo searchResultInfo;

    public IssueNavigatorToolsHelper(JiraAuthenticationContext authContext, PermissionManager permissionManager,
            ApplicationProperties applicationProperties, ColumnLayoutManager columnLayoutManager,
            SearchRequestInfo searchRequestInfo, SearchResultInfo searchResultInfo)
    {
        this.authContext = authContext;
        this.permissionManager = permissionManager;
        this.applicationProperties = applicationProperties;
        this.columnLayoutManager = columnLayoutManager;
        this.searchRequestInfo = searchRequestInfo;
        this.searchResultInfo = searchResultInfo;
    }

    public static class SearchRequestInfo
    {
        /**
         * filter id
         */
        public final Long filterId;

        /**
         * query
         */
        public final Query query;

        /**
         * should use filter columns
         */
        public final boolean useColumns;

        /**
         * should slip columns config at all
         */
        public final boolean skipColumns;

        /**
         * owner name of filter
         */
        public final String ownerUserKey;

        //TODO Check if skipColumns and useColumns are actually being used
        public SearchRequestInfo(Long filterId, Query query, boolean useColumns, boolean skipColumns, String ownerUserName)
        {
            this.filterId = filterId;
            this.query = query;
            this.useColumns = useColumns;
            this.skipColumns = skipColumns;
            this.ownerUserKey = ownerUserName;
        }

        public SearchRequestInfo(Long filterId, Query query, boolean useColumns, String ownerUserName)
        {
            this.filterId = filterId;
            this.query = query;
            this.useColumns = useColumns;
            this.skipColumns = false;
            this.ownerUserKey = ownerUserName;
        }
    }

    public static class SearchResultInfo
    {
        /**
         * number of issues in search
         */
        public final int total;
        /**
         * number of pages in search
         */
        public final int pages;

        public SearchResultInfo(int total, int pages)
        {
            this.total = total;
            this.pages = pages;
        }
    }

    public List<ToolOptionGroup> getToolOptions() throws ColumnLayoutStorageException
    {
        List<ToolOptionGroup> options = new ArrayList<ToolOptionGroup>();
        if (isHasBulkChangePermission())
        {
            ToolOptionGroup group = new ToolOptionGroup(getText("navigator.results.currentview.bulkchange"));
            options.add(group);

            if (isBulkEditLimited())
            {
                String label = getText("navigator.results.currentview.bulkchange.limitedissues", getBulkEditMax());
                String url = "/secure/views/bulkedit/BulkEdit1!default.jspa?reset=true&tempMax=" + getBulkEditMax();
                String title = getText("bulk.edit.limited", getBulkEditMax());
                ToolOptionItem item = new ToolOptionItem("bulkedit_max", label, url, title);
                group.addItem(item);
            }
            else
            {
                String label = getText("navigator.results.currentview.bulkchange.allissues", searchResultInfo.total);
                String url = "/secure/views/bulkedit/BulkEdit1!default.jspa?reset=true&tempMax=" + searchResultInfo.total;
                ToolOptionItem item = new ToolOptionItem("bulkedit_all", label, url, "");
                group.addItem(item);
            }

            if (searchResultInfo.pages > 1)
            {
                String label = getText("navigator.results.currentview.bulkchange.currentpage");
                String url = "/secure/views/bulkedit/BulkEdit1!default.jspa?reset=true";
                ToolOptionItem item = new ToolOptionItem("bulkedit_curr_pg", label, url, "");
                group.addItem(item);
            }
        }

        if (searchRequestInfo.query != null && searchRequestInfo.query.getOrderByClause() != null
                && !searchRequestInfo.query.getOrderByClause().getSearchSorts().isEmpty())
        {
            ToolOptionGroup group = new ToolOptionGroup();
            options.add(group);

            String label = getText("navigator.results.clear.sorts");
            String url = "/secure/IssueNavigator!clearSorts.jspa";
            ToolOptionItem item = new ToolOptionItem("", label, url, "");
            group.addItem(item);
        }

        return options;
    }

    /**
     * Checks to see if the current user has the global BULK CHANGE permission
     */
    public boolean isHasBulkChangePermission()
    {
        if (searchRequestInfo == null)
        {
            return false;
        }
        else
        {
            try
            {
                return permissionManager.hasPermission(Permissions.BULK_CHANGE, authContext.getUser());
            }
            catch (final Exception e)
            {
                log.error(e, e);
                return false;
            }
        }
    }

    /**
     * Tells whether the bulk edit limit property is currently restricting the number of issues in the current search
     * that may be bulk edited.
     *
     * @return true only if the bulk edit limit is restricting.
     */
    public boolean isBulkEditLimited()
    {
        // if the number of search results exactly matches the limit, we will show the limit message
        return getBulkEditMax() != searchResultInfo.total;
    }

    /**
     * Returns the maximum number of issues the user is allowed to bulk edit. Possibly the number of search results that
     * were returned, but no more than the number configured by {@link APKeys#JIRA_BULK_EDIT_LIMIT_ISSUE_COUNT} if it is
     * set to a positive number.
     *
     * @return the number of issues that can be bulk edited.
     */
    public int getBulkEditMax()
    {
        return getBulkEditMax(searchResultInfo.total, applicationProperties.getDefaultBackedString(APKeys.JIRA_BULK_EDIT_LIMIT_ISSUE_COUNT));
    }

    int getBulkEditMax(final int searchTotal, final String bulkEditSetting)
    {
        int max = searchTotal;
        if (bulkEditSetting != null)
        {
            try
            {
                int configuredMax = Integer.parseInt(bulkEditSetting);
                if (configuredMax > 0)
                {
                    max = Math.min(searchTotal, configuredMax);
                }
            }
            catch (NumberFormatException e)
            {
                // can't use this as a number
                log.warn("Ignoring JIRA application property " + APKeys.JIRA_BULK_EDIT_LIMIT_ISSUE_COUNT
                        + " because it cannot be parsed as a number: " + bulkEditSetting);
            }
        }
        return max;
    }

    /**
     * Determines if the current search request has a column layout. Used in the header of the IssueNavigator
     */
    public boolean isHasSearchRequestColumnLayout() throws ColumnLayoutStorageException
    {
        return searchRequestInfo != null && columnLayoutManager.hasColumnLayout(searchRequestInfo.filterId);
    }

    public boolean isOwnerOfSearchRequest()
    {
        return (searchRequestInfo != null) && (authContext.getUser() != null) && authContext.getUser().getKey().equals(searchRequestInfo.ownerUserKey);
    }

    /**
     * Determines whether the "Use Your Columns" link should be shown to the user. This will happen IF the search
     * request has its own column layout AND If the user has NOT chosen to override the search request's column layout
     */
    public boolean isShowOverrideColumnLayout() throws ColumnLayoutStorageException
    {
        if (searchRequestInfo != null)
        {
            return isHasSearchRequestColumnLayout() && searchRequestInfo.useColumns;
        }
        else
        {
            // No search request - this method should never be called when search request has
            // not been created yet
            throw new IllegalStateException("Search Request does not exist.");
        }
    }

    private String getText(String key)
    {
        return authContext.getI18nHelper().getText(key);
    }

    private String getText(String key, Object values)
    {
        return authContext.getI18nHelper().getText(key, values);
    }
}
