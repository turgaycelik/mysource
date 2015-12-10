package com.atlassian.jira.issue.statistics;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.SearchRequest;

/**
 * Allow mapping from Lucene indexes, back to the fields that they came from.
 * <p/>
 * Any 'field' that implements this is capable of having a statistic calculated from it.
 *
 * @see com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator
 */
@PublicSpi
public interface StatisticsMapper<T> extends LuceneFieldSorter<T>
{
    /**
     * Check whether this value is valid for this particular search.  This is useful if you do not wish to display
     * all the values that are indexed (eg - only show released versions)
     *
     * @param value This is the same value that will be returned from {@link #getValueFromLuceneField(String)}
     * @return true if this value is valid for this particular search
     */
    boolean isValidValue(T value);

    /**
     * Check if the field is always part of an issues data. This should only return false in the case of a
     * custom field where the value does not have to be set for each issue.
     *
     * @return true if this mapper will always be part of an issues data
     */
    boolean isFieldAlwaysPartOfAnIssue();

    /**
     * Get a suffix for the issue navigator, which allows for filtering on this value.
     * <p/>
     * eg. a project field would return a SearchRequest object who's getQueryString method will produce
     * <code>pid=10240</code>
     * <p/>
     * Note that values returned from implementations should return values that are URLEncoded.
     *
     * @param value         This is the same value that will be returned from {@link #getValueFromLuceneField(String)}
     * @param searchRequest is the search request that should be used as the base of the newly generated
     *                      SearchRequest object. If this parameter is null then the return type will also be null.
     * @return a SearchRequest object that will generate the correct issue navigator url to search
     *         the correct statistics set, null otherwise.
     * @see java.net.URLEncoder#encode(String)
     */
    SearchRequest getSearchUrlSuffix(T value, SearchRequest searchRequest);
}
