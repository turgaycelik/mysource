package com.atlassian.jira.issue.search;

import com.atlassian.annotations.ExperimentalApi;
import org.apache.lucene.search.Collector;

/**
 * Interface to allow classes that extend {@link Collector} to be informed of the total number of hits.
 *
 * This is only recognised by the {@link com.atlassian.jira.issue.search.providers.LuceneSearchProvider} methods
 *     {@link com.atlassian.jira.issue.search.providers.LuceneSearchProvider#searchAndSort(com.atlassian.query.Query, com.atlassian.jira.user.ApplicationUser, org.apache.lucene.search.Collector, com.atlassian.jira.web.bean.PagerFilter)}
 *     {@link com.atlassian.jira.issue.search.providers.LuceneSearchProvider#searchAndSort(com.atlassian.query.Query, com.atlassian.crowd.embedded.api.User, org.apache.lucene.search.Collector, com.atlassian.jira.web.bean.PagerFilter)}
 *     {@link com.atlassian.jira.issue.search.providers.LuceneSearchProvider#searchAndSortOverrideSecurity(com.atlassian.query.Query, com.atlassian.crowd.embedded.api.User, org.apache.lucene.search.Collector, com.atlassian.jira.web.bean.PagerFilter)}
 *     {@link com.atlassian.jira.issue.search.providers.LuceneSearchProvider#searchAndSortOverrideSecurity(com.atlassian.query.Query, com.atlassian.jira.user.ApplicationUser, org.apache.lucene.search.Collector, com.atlassian.jira.web.bean.PagerFilter)}
 *
 *
 * If you wish to search for the top 500 results, but also know the total hits.
 * <code>
 *  collector = new MyCollector() // Implementing TotalHitsAwareCollector
 *  PagerFilter filter = new PagerFilter(1, 500);
 *  searchProvider.searchAndSort(parseResult.getQuery(), user, collector, filter);
 * </code>
 *
 * This will call setTotalHits(int x) on your collector.
 *
 * @since v6.0.1
 */
@ExperimentalApi
public interface TotalHitsAwareCollector
{
    /**
     * Set the total hits.
     * This may be larger than {@link com.atlassian.jira.web.bean.PagerFilter#getMax()} requested when the search is invoked.
     * There is no ordering guarenteed between calls to this method and calls to {@link Collector#collect(int)}.
     * This method will be called even if collect() is not (e.g. there are no results).
     * @param totalHits
     */
    void setTotalHits(final int totalHits);
}
