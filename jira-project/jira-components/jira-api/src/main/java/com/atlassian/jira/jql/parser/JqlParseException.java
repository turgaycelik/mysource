package com.atlassian.jira.jql.parser;

/**
 * Thrown when an error occurs while parsing a JQL string.
 *
 * @since v4.0
 */
public class JqlParseException extends Exception
{
    private final JqlParseErrorMessage errorMessage;

    public JqlParseException(final JqlParseErrorMessage errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public JqlParseException(final JqlParseErrorMessage errorMessage, final String message)
    {
        super(message);
        this.errorMessage = errorMessage;
    }

    public JqlParseException(final JqlParseErrorMessage errorMessage, final Throwable cause)
    {
        super(cause);
        this.errorMessage = errorMessage;
    }

    public JqlParseErrorMessage getParseErrorMessage()
    {
        return errorMessage;
    }

    public int getLineNumber()
    {
        return errorMessage == null ? -1 : errorMessage.getLineNumber();
    }

    public int getColumnNumber()
    {
        return errorMessage == null ? -1 : errorMessage.getColumnNumber();
    }
}
