package com.atlassian.jira.jql.parser.antlr;

import com.atlassian.jira.jql.parser.JqlParseErrorMessage;

/**
 * A {@link RuntimeException} that contains a {@link JqlParseErrorMessage}. This can be used
 * to stop ANTLR in its tracks. 
 *
 * @since v4.0
 */
public class RuntimeRecognitionException extends RuntimeException
{
    private final JqlParseErrorMessage errorMessage;

    RuntimeRecognitionException(JqlParseErrorMessage errorMessage)
    {
        super();
        this.errorMessage = errorMessage;
    }

    RuntimeRecognitionException(JqlParseErrorMessage errorMessage, Throwable cause)
    {
        super(cause);
        this.errorMessage = errorMessage;
    }

    public JqlParseErrorMessage getParseErrorMessage()
    {
        return errorMessage;
    }
}
