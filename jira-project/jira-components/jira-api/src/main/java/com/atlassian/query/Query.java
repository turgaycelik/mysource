package com.atlassian.query;

import com.atlassian.annotations.PublicApi;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.order.OrderBy;

/**
 * The representation of a query.
 *
 */
@PublicApi
public interface Query
{
    /**
     * @return the main clause of the search which can be any number of nested clauses that will make up the full
     * search query. Null indicates that no where clause is available and all issues should be returned.
     */
    Clause getWhereClause();

    /**
     * @return the sorting portion of the search which can be any number of
     * {@link com.atlassian.query.order.SearchSort}s that will make up the full order by clause. Null indicates that
     * no order by clause has been entered and we will not sort the query, empty sorts will cause the default
     * sorts to be used.
     */
    OrderBy getOrderByClause();

    /**
     * @return the original query string that the user inputted into the system. If not provided, will return null.
     */
    String getQueryString();
}
