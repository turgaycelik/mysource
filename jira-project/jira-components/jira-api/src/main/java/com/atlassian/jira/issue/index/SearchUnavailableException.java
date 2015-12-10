package com.atlassian.jira.issue.index;

/**
 * Searching is disabled, cannot get a searcher.
 */
public class SearchUnavailableException extends IllegalStateException
{
    private final boolean indexingEnabled;

    SearchUnavailableException(final RuntimeException ex, final boolean indexingEnabled)
    {
        super(ex);
        this.indexingEnabled = indexingEnabled;
    }

    public boolean isIndexingEnabled()
    {
        return indexingEnabled;
    }
}
