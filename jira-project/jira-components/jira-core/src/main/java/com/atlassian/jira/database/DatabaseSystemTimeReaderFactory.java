package com.atlassian.jira.database;

import javax.annotation.Nonnull;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Factory for choosing an appropriate {@link DatabaseSystemTimeReader} for the current database type.
 *
 * @since 6.3.4
 */
@ExperimentalApi
public interface DatabaseSystemTimeReaderFactory
{
    /**
     * Chooses a database system time reader for the current database.
     *
     * @return a database system time reader.
     */
    @Nonnull
    DatabaseSystemTimeReader getReader();
}
