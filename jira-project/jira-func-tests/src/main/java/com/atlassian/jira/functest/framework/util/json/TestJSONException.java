package com.atlassian.jira.functest.framework.util.json;

/**
 * The JSONException is thrown by the JSON.org classes then things are amiss.
 *
 * @author JSON.org
 * @version 2
 * @since v3.13
 */
public class TestJSONException extends Exception
{
    private Throwable cause;

    /**
     * Constructs a JSONException with an explanatory message.
     *
     * @param message Detail about the reason for the exception.
     */
    public TestJSONException(String message)
    {
        super(message);
    }

    public TestJSONException(Throwable t)
    {
        super(t.getMessage());
        this.cause = t;
    }

    public Throwable getCause()
    {
        return this.cause;
    }
}
