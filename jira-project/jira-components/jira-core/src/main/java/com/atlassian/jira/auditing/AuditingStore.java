package com.atlassian.jira.auditing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.user.ApplicationUser;

/**
 * Auditing store used to save audit entries in the db.
 *
 * @since v6.2
 */
public interface AuditingStore
{
    void storeRecord(@Nonnull AuditingCategory category, String categoryName, @Nonnull String summaryI18nKey, @Nonnull String eventSource,
                     @Nullable ApplicationUser author, @Nullable String remoteAddress,@Nullable AssociatedItem object,
                     @Nullable Iterable<ChangedValue> changedValues, @Nullable Iterable<AssociatedItem> associatedItems,
                     boolean isAuthorSysAdmin);

    @Nonnull
    Records getRecords(@Nullable Long maxId, @Nullable Long sinceId, @Nullable Integer maxResults, Integer offset,
            @Nullable AuditingFilter filter, boolean includeSysAdminActions);

    long countRecords(@Nullable Long maxId, @Nullable Long sinceId, boolean includeSysAdminActions);

    long removeRecordsOlderThan(long timestamp);

    long countRecordsOlderThan(long timestamp);
}
