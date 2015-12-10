package com.atlassian.jira.issue.search;

import org.apache.lucene.search.IndexSearcher;

/**
 * Provides low-level searches that can be used to query indexes.  In JIRA's case these are
 * Lucene {@link IndexSearcher}s.
 * <p/>
 * <b>NOTE:</b> This class should perhaps be re-name to 'SearcherFactory' as {@link SearchProvider}s are something
 * a little different in JIRA.
 */
public interface SearchProviderFactory
{
    public static final String ISSUE_INDEX = "issues";
    public static final String COMMENT_INDEX = "comments";
    public static final String CHANGE_HISTORY_INDEX = "changes";

    /**
     * Get a Lucene {@link IndexSearcher} that can be used to search a Lucene index.
     * <p/>
     * At the moment the possible values for the searcherName argument are {@link #ISSUE_INDEX} (to search the
     * issue index) and {@link #COMMENT_INDEX} (to search the comment index).
     */
    public IndexSearcher getSearcher(String searcherName);
}
