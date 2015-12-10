package com.atlassian.jira.util.json;

/**
 * The JSONException is thrown by the JSON.org classes then things are amiss.
 *
 * @author JSON.org
 * @version 2
 * @since v3.13
 */
public class JSONException extends Exception
{
    private Throwable cause;

    /**
     * Constructs a JSONException with an explanatory message.
     *
     * @param message Detail about the reason for the exception.
     */
    public JSONException(final String message)
    {
        super(message);
    }

    public JSONException(final Throwable t)
    {
        super(t.getMessage());
        cause = t;
    }

    public Throwable getCause()
    {
        return cause;
    }
}
