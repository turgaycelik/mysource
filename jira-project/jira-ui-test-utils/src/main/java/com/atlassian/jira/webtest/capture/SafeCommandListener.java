package com.atlassian.jira.webtest.capture;

import org.apache.log4j.Logger;

/**
 * Command listener that make sure exceptions don't propagate up the call chain.
 *
 * @since v4.2
 */
class SafeCommandListener implements FFMpegCommandListener
{
    private static final Logger log = Logger.getLogger(SafeCommandListener.class);

    private final FFMpegCommandListener delegate;

    SafeCommandListener(final FFMpegCommandListener delegate)
    {
        this.delegate = delegate;
    }

    public void start()
    {
        try
        {
            delegate.start();
        }
        catch (Exception e)
        {
            log.error("Unexpected error while handling 'start' command.", e);
        }
    }

    public void outputLine(final String line)
    {
        try
        {
            delegate.outputLine(line);
        }
        catch (Exception e)
        {
            log.error("Unexpected error while handling 'outputLine' command.", e);
        }
    }

    public void progress(final FFMpegProgressEvent event)
    {
        try
        {
            delegate.progress(event);
        }
        catch (Exception e)
        {
            log.error("Unexpected error while handling 'progress' command.", e);
        }
    }

    public void end(final int exitCode)
    {
        try
        {
            delegate.end(exitCode);
        }
        catch (Exception e)
        {
            log.error("Unexpected error while handling 'end' command.", e);
        }
    }
}
