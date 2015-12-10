package com.atlassian.jira.webtest.capture;

/**
 * This is a listener that does nothing.
 *
 * @since v4.2
 */
class NoopCommandListener implements FFMpegCommandListener
{
    NoopCommandListener()
    {
    }

    public void start()
    {
    }

    public void outputLine(final String line)
    {
    }

    public void progress(final FFMpegProgressEvent event)
    {
    }

    public void end(final int exitCode)
    {
    }
}
