package com.atlassian.jira.auditing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.ExperimentalApi;

/**
 * @since v6.2
 */
@ExperimentalApi
public interface AuditingManager
{
    void store(RecordRequest request);

    /**
     * Get records from the database.
     *
     *
     * @param maxId max record id to return (including), doesn't have to represent a real record in the database
     * @param sinceId smallest record id to return (including), doesn't have to represent a real record in the database
     * @param maxResults number of records to get, you cannot get more than 10000 records at once (if you provide larger number it will be automatically capped)
     * @param offset offset from which records are being counted
     * @param filter defines how records should be filtered out before being returned, if null records will not be filtered
     */
    @Nonnull
    Records getRecords(@Nullable Long maxId, @Nullable Long sinceId, @Nullable Integer maxResults, @Nullable Integer offset, @Nullable AuditingFilter filter);

    long countRecords(@Nullable Long maxId, @Nullable Long sinceId);

    /**
     * Does the same as {@link #getRecords(Long, Long, Long)} but filters out all records
     * authored by sysadmins (users who were sysadmins at the time of performing the action)
     */
    @Nonnull
    Records getRecordsWithoutSysAdmin(@Nullable Long maxId, @Nullable Long sinceId, @Nullable Integer maxResults, @Nullable Integer offset, @Nullable AuditingFilter filter);

    long countRecordsWithoutSysAdmin(@Nullable Long maxId, @Nullable Long sinceId);
}
