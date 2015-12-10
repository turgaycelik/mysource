package com.atlassian.jira.bc.issue.fields;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayout;
import com.atlassian.jira.user.ApplicationUser;

import java.util.List;

/**
 * Provides access and manipulation of column configuration for filters and default column configuration for users.
 *
 * @since v6.1
 */
public interface ColumnService
{
    /**
     * Retreive the effective {@link ColumnLayout} for userWithColumns. Users may not have set their default columns in
     * which case they will be seeing the system's default columns. In that case the outcome contain the default
     * columns.
     *
     * @param serviceUser for this service request.
     * @param userWithColumns the user whose columns are being requested.
     * @return the {@link ColumnLayout} for the user.
     */
    ServiceOutcome<ColumnLayout> getColumnLayout(ApplicationUser serviceUser, ApplicationUser userWithColumns);

    /**
     * Get the column layout for the given filter. If there is no column layout for the filter, the outcome will contain
     * null.
     *
     * @param serviceUser for this service request.
     * @param filterId the id of the filter to get the column layout for.
     * @return the column layout or null.
     */
    ServiceOutcome<ColumnLayout> getColumnLayout(ApplicationUser serviceUser, Long filterId);

    /**
     * Get the system's default columns.
     *
     * @param serviceUser for this service request. User must have admin access in order to perform this operation
     * @return the column layout or null.
     */
    ServiceOutcome<ColumnLayout> getDefaultColumnLayout(ApplicationUser serviceUser);

    /**
     * Sets a user's {@link ColumnLayout}.
     *
     * @param serviceUser for this service request.
     * @param userWithColumns the use whose columns are being set.
     * @param fieldIds the list of field ids to set as the user's ColumnLayout.
     * @return com.atlassian.jira.bc.ServiceResult
     */
    ServiceResult setColumns(ApplicationUser serviceUser, ApplicationUser userWithColumns, List<String> fieldIds);

    /**
     * Sets the given filter {@link ColumnLayout} to the given fields.
     *
     * @param serviceUser for this service request
     * @param filterId id of the filter
     * @param fieldIds the list of field ids to set as the filter's ColumnLayout
     * @return com.atlassian.jira.bc.ServiceResult
     */
    ServiceResult setColumns(ApplicationUser serviceUser, Long filterId, List<String> fieldIds);

    /**
     * Sets the system's default columns to the given {@code fieldIds}
     *
     * @param serviceUser for this service request. User must have admin access in order to perform this operation
     * @param fieldIds the list of field ids to set as the system's default ColumnLayout
     * @return com.atlassian.jira.bc.ServiceResult
     */
    ServiceResult setDefaultColumns(ApplicationUser serviceUser, List<String> fieldIds);

    /**
     * Resets a user's {@link ColumnLayout}.
     *
     * @param serviceUser for this service request.
     * @param userWithColumns the use whose columns are being reset.
     * @return com.atlassian.jira.bc.ServiceResult
     */
    ServiceResult resetColumns(ApplicationUser serviceUser, ApplicationUser userWithColumns);

    /**
     * Resets the given filter to no longer have its own {@link ColumnLayout}.
     *
     * @param serviceUser for this service request
     * @param filterId id of the filter
     * @return com.atlassian.jira.bc.ServiceResult
     */
    ServiceResult resetColumns(ApplicationUser serviceUser, Long filterId);
}
