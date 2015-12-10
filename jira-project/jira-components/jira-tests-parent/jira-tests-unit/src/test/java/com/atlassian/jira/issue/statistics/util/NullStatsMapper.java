package com.atlassian.jira.issue.statistics.util;

import java.util.Comparator;

import com.atlassian.jira.issue.comparator.NullComparator;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.StatisticsMapper;

/**
 * This mapper treats all values as valid, field is always a part of issue.
 * 
 * @since v3.11
 */
public class NullStatsMapper implements StatisticsMapper
{

    /**
     * Always throws UnsupportedOperationException
     *
     * @return throws new UnsupportedOperationException
     */
    public String getDocumentConstant() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the same reference to documentValue parameter
     *
     * @param documentValue documant value
     * @return the same reference to documentValue parameter
     */
    public Object getValueFromLuceneField(String documentValue)
    {
        return documentValue;
    }

    /**
     * Creates and returns a new {@link NullComparator} instance
     *
     * @return a new NullComparator instance
     */
    public Comparator getComparator()
    {
        return new NullComparator();
    }

    /**
     * Always returns true
     *
     * @param value ignored
     * @return true
     */
    public boolean isValidValue(Object value)
    {
        return true;
    }

    /**
     * Always returns true
     *
     * @return true
     */
    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return true;
    }

    /**
     * Always throws UnsupportedOperationException
     *
     * @param value         ignored
     * @param searchRequest ignored
     * @return throws new UnsupportedOperationException
     */
    public SearchRequest getSearchUrlSuffix(Object value, SearchRequest searchRequest)
            throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }
}
