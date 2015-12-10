package com.atlassian.jira.config.database.jdbcurlparser;

import com.atlassian.jira.exception.ParseException;

/**
 * Implementations of this interface provide DB-specific JDBC url parsing.
 *
 */
public interface JdbcUrlParser
{
    DatabaseInstance parseUrl(final String jdbcUrl) throws ParseException;

    /**
     * Returns the JDBC URL for this DB config.
     *
     * @param hostname the hostname
     * @param port the TCP/IP port number
     * @param instance the DB "instance"
     * @return the JDBC URL for this DB config.
     *
     * @throws ValidationException If the underlying configuration is invalid for this DB type. eg for Postgres, "Database" (instance) is a required field
     */
    String getUrl(String hostname, String port, String instance) throws ParseException;
}
