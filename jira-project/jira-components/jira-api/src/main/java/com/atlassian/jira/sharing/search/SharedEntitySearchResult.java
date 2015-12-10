package com.atlassian.jira.sharing.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.collect.EnclosedIterable;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A pair that contains the result status of SharedEntity search. It contains the actual results and a boolean value
 * that indicates whether or not more results are still available. It also contains the total number of results found,
 * which may be greater than the original page size requested.
 *
 * @since v3.13
 */
@PublicApi
public class SharedEntitySearchResult<E extends SharedEntity> implements EnclosedIterable<E>
{
    private final EnclosedIterable<E> results;
    private final boolean hasNext;
    private final int totalResultCount;

    public SharedEntitySearchResult(final EnclosedIterable<E> results, final boolean hasNext, final int totalResultCount)
    {
        this.results = notNull("results", results);
        this.hasNext = hasNext;
        this.totalResultCount = totalResultCount;
    }

    /**
     * Prefer the {@link #foreach(Consumer)} method if the result size is large as it doesn't load everything into memory.
     *
     * @return a list of results
     */
    public List<E> getResults()
    {
        return new ListResolver<E>().get(results);
    }

    public boolean hasMoreResults()
    {
        return hasNext;
    }

    /**
     * @return the total number of results found in a search. This will always be >= {@link #size()}
     */
    public int getTotalResultCount()
    {
        return totalResultCount;
    }

    //
    // implement CloseableIterable
    //

    public final void foreach(final Consumer<E> sink)
    {
        results.foreach(sink);
    }

    public int size()
    {
        return results.size();
    }

    public boolean isEmpty()
    {
        return results.isEmpty();
    }
}
