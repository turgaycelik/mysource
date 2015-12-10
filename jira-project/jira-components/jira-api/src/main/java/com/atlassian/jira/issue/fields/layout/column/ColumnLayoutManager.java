package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.fields.ColumnService;
import com.atlassian.jira.issue.search.SearchRequest;

/**
 * Provides access to persistence services for {@link ColumnLayout} objects for system-wide Issue Navigator default
 * columns, user default columns and columns for {@link SearchRequest Filter Filters}.
 * <p/>
 * Plugin authors should prefer to use {@link ColumnService} where possible instead as that provides high level i18n and
 * permission aspects.
 */
@PublicApi
public interface ColumnLayoutManager
{
    /**
     * Tells whether the system has a default column layout object in the persistent store.
     *
     * @return true only if there is a default column layout.
     * @throws ColumnLayoutStorageException when the persistent store is inaccessible.
     */
    public boolean hasDefaultColumnLayout() throws ColumnLayoutStorageException;

    /**
     * Tells whether the given user has a column layout.
     *
     * @return true only if the given user has a column layout.
     * @throws ColumnLayoutStorageException when the persistent store is inaccessible.
     */
    public boolean hasColumnLayout(User user) throws ColumnLayoutStorageException;

    /**
     * Tells whether the given {@link SearchRequest filter} has columns configured.
     *
     * @return true only if the filter has a column layout.
     * @throws ColumnLayoutStorageException when the persistent store is inaccessible.
     */
    public boolean hasColumnLayout(SearchRequest searchRequest) throws ColumnLayoutStorageException;

    /**
     * Tells whether the filter with the given id has columns configured.
     *
     * @return true only if there is a filter with the id and its has a column layout.
     * @throws ColumnLayoutStorageException when the persistent store is inaccessible.
     */
    public boolean hasColumnLayout(Long filterId) throws ColumnLayoutStorageException;

    /**
     * Get the columns layout for a user, if the user does not have one the default is returned
     *
     * @return Immutable ColumnLayout to be used when displaying
     */
    public ColumnLayout getColumnLayout(User user) throws ColumnLayoutStorageException;

    /**
     * Get the columns layout for a searchRequest, if the searchRequest does not have one the user's columns are
     * returned. If the user does not have one the default is returned
     *
     * @return Immutable ColumnLayout to be used when displaying
     */
    public ColumnLayout getColumnLayout(User remoteUser, SearchRequest searchRequest)
            throws ColumnLayoutStorageException;

    /**
     * Get an editable default column layout for the system
     */
    public EditableDefaultColumnLayout getEditableDefaultColumnLayout() throws ColumnLayoutStorageException;

    /**
     * Get an editable column layout for the user, returns null if they do not have one
     *
     * @return EditableColumnLayout if there is one for the user otherwise return a new one generated from the default
     */
    public EditableUserColumnLayout getEditableUserColumnLayout(User user) throws ColumnLayoutStorageException;

    /**
     * Get an editable column layout for the searchRequest, returns null if it does not have one
     *
     * @return EditableColumnLayout if there is one for the searchRequest otherwise return a new one generated from the
     *         default
     */
    public EditableSearchRequestColumnLayout getEditableSearchRequestColumnLayout(User user, SearchRequest searchRequest)
            throws ColumnLayoutStorageException;

    /**
     * Writes the default column layout to permanent storage
     */
    public void storeEditableDefaultColumnLayout(EditableDefaultColumnLayout editableDefaultColumnLayout)
            throws ColumnLayoutStorageException;

    /**
     * Writes the default column layout to permanent storage
     */
    public void storeEditableUserColumnLayout(EditableUserColumnLayout editableUserColumnLayout)
            throws ColumnLayoutStorageException;

    /**
     * Writes the default column layout to permanent storage
     */
    public void storeEditableSearchRequestColumnLayout(EditableSearchRequestColumnLayout editableSearchRequestColumnLayout)
            throws ColumnLayoutStorageException;

    /**
     * Sets the ColumnLayout in use for users who have not defined their own, also known as the System column layout.
     *
     * @throws ColumnLayoutStorageException if the persistent store is inaccessible.
     */
    public void restoreDefaultColumnLayout() throws ColumnLayoutStorageException;

    public void restoreUserColumnLayout(User user) throws ColumnLayoutStorageException;

    public void restoreSearchRequestColumnLayout(SearchRequest searchRequest) throws ColumnLayoutStorageException;

    /**
     * Get the default Layout, and filter out the columns which a user cannot see
     */
    public ColumnLayout getDefaultColumnLayout(User remoteUser) throws ColumnLayoutStorageException;

    ColumnLayout getDefaultColumnLayout() throws ColumnLayoutStorageException;

    public void refresh();
}
