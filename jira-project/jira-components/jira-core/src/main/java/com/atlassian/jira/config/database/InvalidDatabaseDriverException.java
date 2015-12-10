package com.atlassian.jira.config.database;

/**
 * Thrown if JIRA is requested to configure database using an invalid JDBC driver (that cannot be loaded)
 *
 * @since v5.2
 */
public class InvalidDatabaseDriverException extends RuntimeException
{
    private final String className;

    public InvalidDatabaseDriverException(String className)
    {
        this.className = className;
    }

    public InvalidDatabaseDriverException(String className, String message)
    {
        super(message);
        this.className = className;
    }

    public InvalidDatabaseDriverException(String className, String message, Throwable cause)
    {
        super(message, cause);
        this.className = className;
    }

    public InvalidDatabaseDriverException(String className, Throwable cause)
    {
        super(cause);
        this.className = className;
    }

    public String driverClassName()
    {
        return className;
    }


}
