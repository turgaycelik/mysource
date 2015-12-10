package com.atlassian.jira.exception;

/**
 * Indicates an error in parsing.
 *
 * @since v3.13
 */
public class ParseException extends Exception
{
    public ParseException(final String message)
    {
        super(message);
    }

}
