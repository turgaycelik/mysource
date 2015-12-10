package com.atlassian.jira.webtest.capture;

import com.atlassian.jira.functest.framework.WebTestDescription;

import java.io.File;
import java.io.IOException;

/**
 * Outputs chapters in the common chapter format. That is: CHAPTERXX=hh:mm:ss.sss CHPATERXXNAME=Chapter Title
 *
 * @since v4.2
 */
class CommonChapterTimedListener implements TimedTestListener
{
    private final CommonChapter printer;
    private long lastStart = -1;

    CommonChapterTimedListener(final File file) throws IOException
    {
        printer = new CommonChapter(file);
    }

    public void start(final long clockMs)
    {
    }

    public void startTest(final WebTestDescription test, final long clockMs)
    {
        //Make se we don't go back in time.
        if (clockMs > this.lastStart)
        {
            printer.writeChapter(test.name(), clockMs);
            lastStart = clockMs + 1;
        }
    }

    public void addError(final WebTestDescription test, final Throwable t, final long clockMs)
    {
    }

    public void addFailure(final WebTestDescription test, final Throwable t, final long clockMs)
    {
    }

    public void endTest(final WebTestDescription test, final long clockMs)
    {
    }

    public void close(final long clockMs)
    {
        printer.close();
    }
}
