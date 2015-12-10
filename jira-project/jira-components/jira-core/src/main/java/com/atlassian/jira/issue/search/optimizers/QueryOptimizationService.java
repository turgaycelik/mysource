package com.atlassian.jira.issue.search.optimizers;

import javax.annotation.Nullable;

import com.atlassian.query.Query;

/**
 * Methods to for optimizing {@link com.atlassian.query.Query} objects.
 *
 * @since v6.3
 */
public interface QueryOptimizationService
{
    /**
     * Optimizes both unreleasedVersions() and releasedVersions() functions.
     * The optimization is performed if two conditions have been met: functions has no arguments
     * and projects can be determined from the query.
     * Then those projects are passed as an argument to unreleasedVersions()
     *
     * @param query to be optimized.
     * @return optimized query object or same query if optimization could not be done or null if passed query is null.
     */
    Query optimizeQuery(@Nullable Query query);
}
