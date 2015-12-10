package com.atlassian.jira.web.exception;

/**
 * Thrown when a Jsp is not invoked through a backing {@link webwork.action.Action}
 *
 * @since v4.1
 */
public class InvalidDirectJspCallException extends RuntimeException
{
    private final String pageName;

    public InvalidDirectJspCallException(final String pageName)
    {
        super();
        this.pageName = pageName;
    }

    public InvalidDirectJspCallException(final String message, final String pageName)
    {
        super(message);
        this.pageName = pageName;
    }

    public InvalidDirectJspCallException(final String message, final Throwable cause, final String pageName)
    {
        super(message, cause);
        this.pageName = pageName;
    }

    public InvalidDirectJspCallException(final Throwable cause, final String pageName)
    {
        super(cause);
        this.pageName = pageName;
    }

    public String getPageName()
    {
        return pageName;
    }
}
