package com.atlassian.jira.web.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.search.SearchRequest;

import java.util.List;

/**
 * Provides the columns to be displayed on the results table, given a {@link SearchRequest} and a {@link User}
 */
public class ColumnLayoutItemProvider
{
    /**
     * Gets the columns to be displayed on the result table for a given user and a given search request.
     * @param user The user
     * @param searchRequest The search request
     * @return A list with the columns to be displayed
     * @throws Exception
     */
    public List<ColumnLayoutItem> getColumns(final User user, final SearchRequest searchRequest) throws Exception
    {
        return getColumnLayout(user, searchRequest).getColumnLayoutItems();
    }

    /**
     * Returns the list of columns that an user sees by default on his searches.
     * @param user The user
     * @return A list with the columns to be displayed
     * @throws Exception
     */
    public List<ColumnLayoutItem> getUserColumns(final User user) throws Exception
    {
        return getColumnLayout(user, null).getAllVisibleColumnLayoutItems(user);
    }

    private ColumnLayout getColumnLayout(final User user, final SearchRequest searchRequest) throws ColumnLayoutStorageException
    {
        // Check whether the search request is saved and whether the user has selected to override search requests column layout
        if (searchRequest != null && searchRequest.isLoaded() && searchRequest.useColumns())
        {
            // if not (useColumns for search request is true) use the search request's column layout
            return ComponentAccessor.getColumnLayoutManager().getColumnLayout(user, searchRequest);
        }
        else
        {
            // if the filter columns are overriden use the user's column layout (or the system default if the user does not have a
            // personal column layout).
            return ComponentAccessor.getColumnLayoutManager().getColumnLayout(user);
        }
    }
}
