package com.atlassian.jira.webtest.capture;

import com.atlassian.jira.functest.framework.WebTestDescription;

import java.io.File;
import java.io.IOException;

/**
 * Listens to tests and writes subtitles out in <a href="http://en.wikipedia.org/wiki/SubRip">SubRip</a> format
 *
 * @since v4.2
 */
class SubRipSubtitleListener implements TimedTestListener
{
    private final SubRipSubtitle subtitle;

    private long startTime = 0;
    private String nextMessage = null;

    SubRipSubtitleListener(final File file) throws IOException
    {
        subtitle = new SubRipSubtitle(file);
    }

    public void start(final long clockMs)
    {
    }

    public void startTest(final WebTestDescription test, final long clockMs)
    {
        this.nextMessage = null;

        //Make se we don't go back in time.
        if (clockMs > this.startTime)
        {
            this.startTime = clockMs;
        }
    }

    public void addError(final WebTestDescription test, final Throwable t, final long clockMs)
    {
        nextMessage = "ERROR";
    }

    public void addFailure(final WebTestDescription test, final Throwable t, final long clockMs)
    {
        nextMessage = "FAILURE";
    }

    public void endTest(final WebTestDescription test, final long clockMs)
    {
        outputSubtitle(test, clockMs);
    }

    private void outputSubtitle(final WebTestDescription test, long clockMs)
    {
        if (startTime < clockMs)
        {
            String message = getNiceName(test);
            if (nextMessage != null)
            {
                message = nextMessage + " " + message;
            }
            subtitle.writeSubTitle(message, startTime, clockMs);
            startTime = clockMs + 1;
        }
        nextMessage = null;
    }

    public void close(final long clockMs)
    {
        subtitle.close();
    }

    private static String getNiceName(final WebTestDescription test)
    {
        return TestNameUtils.getSubtitleForTest(test);
    }
}
