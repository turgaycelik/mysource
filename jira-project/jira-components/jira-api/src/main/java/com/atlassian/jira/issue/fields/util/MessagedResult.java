package com.atlassian.jira.issue.fields.util;

/**
 * This class wraps a boolean result and allows the result creator to specify a string message
 * about the result and allows a false result to be classified as Normal, a Warning, or a Fatal error.
 */
public class MessagedResult
{
    public static final int NORMAL = 0;
    public static final int WARNING = 1;
    public static final int FATAL = 2;

    private int state;
    private String message;
    private boolean result;

    /**
     * Constructs a message result with a null message whose state is normal.
     * @param result was the result of the operation
     */
    public MessagedResult(boolean result)
    {
        this.result = result;
        this.state = NORMAL;
    }

    /**
     * Constructs a message result whose state is normal
     * @param result was the result of the operation
     * @param message the message about the state and the result
     */
    public MessagedResult(boolean result, String message)
    {
        this.result = result;
        this.message = message;
        if(!result)
        {
            this.state = WARNING;
        }
    }

    /**
     * Used to construct a messaged result.
     * @param result was the result of the operation
     * @param message the message about the state and the result
     * @param state can be NORMAL, WARNING or FATAL if result is false, otherwise this is ignored
     */
    public MessagedResult(boolean result, String message, int state)
    {
        this.result = result;
        this.message = message;
        if(!result)
        {
            if(isValidState(state))
            {
                this.state = state;
            }
            else
            {
                throw new IllegalArgumentException("The provided state: " + state + " is not a valid state.");
            }
        }
    }

    /**
     * Use this to specify the state of the html message. Error indicates
     * that the operation can not proceed, Warning inidicates that the
     * message will be displayed to the user but that the operation can
     * proceed, and Normal means that everything is ok.
     * @param html
     * @param state the state of the display html.
     */
    public MessagedResult(String html, int state)
    {
        this.message = html;
        if(isValidState(state))
        {
            this.state = state;
        }
        else
        {
            throw new IllegalArgumentException("The provided state: " + state + " is not a valid state.");
        }
    }

    public boolean getResult()
    {
        return result;
    }

    public void setResult(boolean result)
    {
        this.result = result;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public boolean isFatal()
    {
        return !result && (state == FATAL);
    }

    public boolean isWarning()
    {
        return !result && (state == WARNING);
    }

    private boolean isValidState(int state)
    {
        return !(state != NORMAL && state != WARNING && state != FATAL);
    }
}
