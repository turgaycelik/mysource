package com.atlassian.jira.index;

/**
 * Exception indicating some errors occurred during the indexing process.
 *
 * @since v6.1
 */
public class IndexingFailureException extends RuntimeException
{
    private final int failures;

    public IndexingFailureException(final int failures)
    {
        this.failures = failures;
    }

    @Override
    public String getMessage()
    {
        return String.format("Indexing completed with %1$d errors", failures);
    }
}
