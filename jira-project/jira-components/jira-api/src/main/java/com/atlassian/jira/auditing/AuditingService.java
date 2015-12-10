package com.atlassian.jira.auditing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;

/**
 * Service for managing auditing records
 *
 * @since v6.3
 */
@ExperimentalApi
public interface AuditingService
{
    /**
     * @param offset the offset from which search starts
     * @param maxResults maximum number of returned results
     * @param filter defines how records should be filtered out before being returned, if null records will not be filtered
     * @return Records instance wrapped in ServiceOutcome
     */
    @Nonnull
    ServiceOutcome<Records> getRecords(@Nullable Integer offset,
            @Nullable Integer maxResults, @Nullable AuditingFilter filter);

    /**
     * Store record in Auditing Log
     *
     * @param category - category of the log record
     * @param summary - description of the change
     * @param objectItem - changed object
     * @param values - changed values of the object item
     * @param associatedItems - other objects that were affected by this change
     *
     * @param associatedItems @return
     */

    @Nonnull
    ErrorCollection storeRecord(@Nullable String category, @Nullable String summary,
            @Nullable AssociatedItem objectItem, @Nullable Iterable<ChangedValue> values,
            @Nullable Iterable<AssociatedItem> associatedItems);    /**


     /**
     * Store record in Auditing Log along with plugin name created the event
     *
     * @param category - category of the log record
     * @param summary - description of the change
     * @param eventSourceKey - key of plugin which creates the record
     * @param objectItem - changed object
     * @param values - changed values of the object item
     * @param associatedItems - other objects that were affected by this change
     *
     * @param associatedItems @return
     */
    @Nonnull
    ErrorCollection storeRecord(@Nonnull String category, @Nonnull String summary,  @Nonnull String eventSourceKey,
            @Nullable AssociatedItem objectItem, @Nullable Iterable<ChangedValue> values,
            @Nullable Iterable<AssociatedItem> associatedItems);

    /**
     * Returns total number of audit records
     *
     * @return total number of records
     */
    @Nonnull
    Long getTotalNumberOfRecords();
}
