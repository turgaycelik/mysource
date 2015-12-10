package com.atlassian.jira.webtest.capture;

import com.atlassian.jira.functest.framework.WebTestDescription;

import java.io.File;
import java.io.IOException;

/**
 * A listener that generates a chapter XML file.
 *
 * @since v4.2
 */
class MkvChapterTimedListener implements TimedTestListener
{
    private final MkvChapterBuilder builder;
    private MkvChapterBuilder.ChapterBuilder currentSuite;
    private MkvChapterBuilder.SubChapterBuilder currentTest;
    private long lastTestEndTime = 0;
    private Class<?> lastSuite;

    MkvChapterTimedListener(final File file) throws IOException
    {
        builder = new MkvChapterBuilder(file);
    }

    public void start(final long clockMs)
    {
    }

    public void startTest(final WebTestDescription test, final long clockMs)
    {
        if (lastSuite == null || lastSuite != test.testClass())
        {
            buildCurrentSuite();
            currentSuite = builder.chapter();
            currentSuite.startTime(clockMs).endTime(clockMs).text(TestNameUtils.getSuiteName(test));
            lastSuite = test.testClass();
        }
        currentTest = currentSuite.subChapter().startTime(clockMs).endTime(clockMs).text(TestNameUtils.getTestName(test));
    }

    public void addError(final WebTestDescription test, final Throwable t, final long clockMs)
    {
    }

    public void addFailure(final WebTestDescription test, final Throwable t, final long clockMs)
    {
    }

    public void endTest(final WebTestDescription test, final long clockMs)
    {
        lastTestEndTime = clockMs;
        currentTest.endTime(clockMs);
    }

    public void close(final long clockMs)
    {
        buildCurrentSuite();
        builder.close();
    }

    private void buildCurrentSuite()
    {
        if (currentSuite != null)
        {
            if (currentSuite.hasChildren())
            {
                currentSuite.endTime(lastTestEndTime);
                builder.commit();
            }
            else
            {
                builder.rollback();
            }
        }
    }
}
