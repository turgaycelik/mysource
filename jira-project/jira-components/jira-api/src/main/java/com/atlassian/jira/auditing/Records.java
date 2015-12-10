package com.atlassian.jira.auditing;

import com.atlassian.annotations.ExperimentalApi;

import java.util.List;

/**
 * @since v6.2
 */
@ExperimentalApi
public interface Records
{
    /**
     * @deprecated Use {@link #getResults} instead. Since v6.3.4.
     */
    @Deprecated
    Iterable<AuditRecord> getRecords();

    /**
     * Get list of records
     *
     * @since v6.3.4
     */
    List<AuditRecord> getResults();

    long getCount();

    int getMaxResults();
}
