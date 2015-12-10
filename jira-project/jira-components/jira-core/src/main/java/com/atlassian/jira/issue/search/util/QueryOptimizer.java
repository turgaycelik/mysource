package com.atlassian.jira.issue.search.util;

import com.atlassian.query.Query;

/**
 * An interface for a Query Optimizer which performs operations on a query which don't change the semantic of the query,
 * but optimize the structure of the query.
 * E.g.: Remove redundant clauses
 *
 * @since v4.0
 */
public interface QueryOptimizer
{
    /**
     * Optimize a {@link com.atlassian.query.Query}
     *
     * @param query the query to optimize
     * @return the optimized query
     */
    Query optimizeQuery(Query query);
}
