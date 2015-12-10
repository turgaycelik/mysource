package com.atlassian.query.order;

import com.atlassian.annotations.PublicApi;
import javax.annotation.Nonnull;

import java.util.List;

/**
 * Represents the ordering portion of the a search query. The results can be sorted by fields in either a
 * {@link com.atlassian.query.order.SortOrder#ASC ascending} or
 * {@link com.atlassian.query.order.SortOrder#DESC descending} order. The actual sort is made up of a list of
 * (field, order) pair(s). Each of the pair is represented by a {@link com.atlassian.query.order.SearchSort} object.
 */
@PublicApi
public interface OrderBy
{
    /**
     * @return a list of SearchSort objects that represent the specified sorting requested for this OrderBy clause. Cannot
     * be null.
     */
    @Nonnull
    List<SearchSort> getSearchSorts();
}
