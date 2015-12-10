package com.atlassian.jira.rest.v2.avatar;

/**
 * Exception indicating data validation failuer.
 *
 * @since v6.3
 */
public class ValidationException extends Exception
{
    public ValidationException(final String s)
    {
        super(s);
    }

    public ValidationException(final String s, final Throwable throwable)
    {
        super(s, throwable);
    }
}
