package com.atlassian.jira.functest.config;

/**
 * Exception thrown if some sort of error occurs.
 *
 * @since v4.1
 */
public class ConfigException extends RuntimeException
{
    public ConfigException()
    {
        super();
    }

    public ConfigException(final String message)
    {
        super(message);
    }

    public ConfigException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public ConfigException(final Throwable cause)
    {
        super(cause);
    }
}
