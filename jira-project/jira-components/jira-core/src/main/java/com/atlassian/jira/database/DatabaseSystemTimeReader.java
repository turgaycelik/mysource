package com.atlassian.jira.database;

import java.sql.SQLException;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Reads the current system time from the database.
 *
 * @since 6.3.4
 */
@ExperimentalApi
public interface DatabaseSystemTimeReader
{
    /**
     * @return the current system time according to the database in milliseconds.  Potentially a database-dependent value (it may not
     * necessarily be number of milliseconds since 1970 but must be in milliseconds from a constant offset).
     *
     * @throws SQLException if a database error occurs.
     */
    long getDatabaseSystemTimeMillis()
    throws SQLException;
}
