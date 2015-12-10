package com.atlassian.jira.webtest.capture;

/**
 * Exception that is thrown on FFMpeg error.
 *
 * @since v4.2
 */
public class FFMpegException extends Exception
{
    public FFMpegException()
    {
        super();
    }

    public FFMpegException(final String message)
    {
        super(message);
    }

    public FFMpegException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public FFMpegException(final Throwable cause)
    {
        super(cause);
    }
}
