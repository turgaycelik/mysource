package com.atlassian.jira.pageobjects.config.junit4;

import com.atlassian.jira.hallelujah.HallelujahTestFailureException;
import com.atlassian.jira.testkit.client.log.FuncTestOut;
import com.atlassian.jira.functest.framework.WebTestDescription;
import com.atlassian.jira.functest.framework.WebTestListener;
import com.atlassian.jira.functest.framework.suite.WebTestDescriptionList;
import com.atlassian.jira.functest.framework.util.text.MsgOfD;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.atlassian.jira.util.dbc.Assertions.stateTrue;
import static java.lang.String.format;

/**
 * A {@link com.atlassian.jira.functest.framework.WebTestListener} that logs information about running
 * tests.
 *
 * @since v4.4
 */
public class LogTestInformationListener implements WebTestListener
{
    private static final String DEFAULT_LOG_PREFIX = "=====";

    private static final String SECONDS_PATTERN = "%ds";
    private static final String MINUTES_PATTERN = "%dm%ds";
    private static final String ONE_SECOND = "1s";

    private final static int MAX_FAILURES_TO_SHOW = 15;

    private final String logPrefix;

    // testFinished is called on test errors/failures as well so we need to store the error to know what really happenned
    private Throwable testError;

    private int totalTestCount;
    private int testRunCount;
    private int testErrorCount;

    private long suiteStartTime;
    private long testStartTime;

    private final List<String> failedTests = Lists.newArrayList();

    public LogTestInformationListener()
    {
        this(DEFAULT_LOG_PREFIX);
    }

    public LogTestInformationListener(String logPrefix)
    {
        this.logPrefix = logPrefix;
    }


    @VisibleForTesting
    protected void log(String logMessage)
    {
        FuncTestOut.log(logMessage);
    }

    @Override
    public void suiteStarted(WebTestDescription suiteDescription)
    {
        totalTestCount = suiteDescription.testCount();
        suiteStartTime = System.currentTimeMillis();
        log("\n" + logPrefix + " Suite '" + suiteDescription.name() + "' has started\n");
        List<WebTestDescription> singleTests = new WebTestDescriptionList(suiteDescription).singleTests();
        log(logPrefix + " Running " + singleTests.size() + " tests in total");
        for (WebTestDescription singleTest : singleTests)
        {
            log(singleTest.name());
        }
    }

    @Override
    public void suiteFinished(WebTestDescription suiteDescription)
    {
        final long suiteRunTime = System.currentTimeMillis() - suiteStartTime;
        final StringBuilder logBuilder =  new StringBuilder("\n")
                .append("__________________________________________________________________________________________\n")
                .append("\n")
                .append("Summary of ").append(suiteDescription.name()).append("\n")
                .append("\t");
        appendRunTime(padRight(logBuilder, "Suite Run Time", 40), suiteRunTime).append("\n");
        padRight(logBuilder, "Tests Run", 40).append(testRunCount).append("\n");
        appendErrorCount(padRight(logBuilder, "Errors", 40), "").append("\n");
        logBuilder.append(new MsgOfD());
        log(logBuilder.toString());
    }

    @Override
    public void testStarted(WebTestDescription description)
    {
        StringBuilder message = new StringBuilder(logPrefix)
                .append(" Test Started : ")
                .append(description.name())
                .append(" #")
                .append(testRunCount + 1)
                .append(" of ")
                .append(totalTestCount);
        log(message.toString());
        resetStartTime();
    }

    @Override
    public void testFinished(WebTestDescription description)
    {
        // this SUX cause it relies on the order of listener calls (first testError/testFailure, then testFinished)
        // but JUnit4 gives us no other way...
        incrementTestCounts(description);
        if (hasError())
        {
            addFailedTest(description);
        }
        log(testFinshedMessage(description));
        resetError();
    }

    @Override
    public void testError(WebTestDescription description, Throwable error)
    {
        setError(error);
        // hack - if the whole suite fails, JUnit4 doesn't call testStarted/testFinished :/
        if (description.isSuite())
        {
            resetStartTime();
            testFinished(description);
        }
    }

    @Override
    public void testFailure(WebTestDescription description, Throwable failure)
    {
        setError(failure);
        // hack - if the whole suite fails, JUnit4 doesn't call testStarted/testFinished :/
        if (description.isSuite())
        {
            resetStartTime();
            testFinished(description);
        }
    }

    private void resetStartTime()
    {
        testStartTime = System.currentTimeMillis();
    }

    private void incrementTestCounts(WebTestDescription description)
    {
        final int testCount = description.testCount();
        testRunCount += testCount;
        if (hasError())
        {
            testErrorCount += testCount;
        }
    }

    private boolean hasError()
    {
        return testError != null;
    }

    private void resetError()
    {
        testError = null;
    }

    private void setError(Throwable error)
    {
        testError = error;
    }

    private void addFailedTest(WebTestDescription failed)
    {
        if (testErrorCount < MAX_FAILURES_TO_SHOW)
        {
            failedTests.add(failed.name());
        }
        else if (testErrorCount == MAX_FAILURES_TO_SHOW)
        {
            failedTests.add("More than " + MAX_FAILURES_TO_SHOW + " failures...");
        }
    }

    private String testFinshedMessage(WebTestDescription testDescription)
    {
        StringBuilder answer = new StringBuilder(logPrefix).append(" Test Finished");
        if (hasError())
        {
            answer.append(" FAIL");
        }
        answer.append(" : ").append(testDescription.name());
        appendTestCount(answer);
        appendErrorCount(answer, " : Errors ");
        appendTestRunTime(answer);
        appendSuiteRunTime(answer);
        appendRunTimeMemory(answer);
        if (hasError())
        {
            appendFailure(answer, testError);
        }
        appendSeparator(answer);
        appendFailuresSoFar(answer);
        return answer.toString();
    }

    private void appendTestCount(StringBuilder logBuilder)
    {
        logBuilder.append(" #").append(testRunCount).append(" of ").append(totalTestCount)
                .append(" (").append(perCentOfTotalCount(testRunCount)).append(")");
    }

    private StringBuilder appendErrorCount(StringBuilder logBuilder, String header)
    {
        return logBuilder.append(header).append(testErrorCount)
                .append(" (").append(perCentOfTotalCount(testErrorCount)).append(")");
    }

    private String perCentOfTotalCount(int testCount)
    {
        final NumberFormat format = NumberFormat.getPercentInstance(Locale.ENGLISH);
        format.setMinimumFractionDigits(1);
        final double testPerCent = (double) testCount / (double) totalTestCount;
        return format.format(testPerCent);
    }

    private void appendTestRunTime(StringBuilder logBuilder)
    {
        final long testTime = System.currentTimeMillis() - testStartTime;
        logBuilder.append(" : Run time ");
        appendRunTime(logBuilder, testTime);
    }

    private StringBuilder appendSuiteRunTime(StringBuilder logBuilder)
    {
        logBuilder.append(" : Suite time ");
        final long suiteTime = System.currentTimeMillis() - suiteStartTime;
        appendRunTime(logBuilder, suiteTime);
        return logBuilder;
    }

    private StringBuilder appendRunTime(StringBuilder logBuilder, long timeToLog)
    {
        logBuilder.append(toReadableTime(timeToLog));
        return logBuilder;
    }

    private String toReadableTime(long timeToLog)
    {
        if (timeToLog <= 1000)
        {
            return ONE_SECOND;
        }
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(timeToLog);
        if (seconds <= 60)
        {
            return format(SECONDS_PATTERN, seconds);
        }
        else
        {
            final long realSeconds = seconds % 60;
            final long minutes = TimeUnit.SECONDS.toMinutes(seconds);
            return format(MINUTES_PATTERN, minutes, realSeconds);
        }
    }

    private void appendRunTimeMemory(StringBuilder logBuilder)
    {
        final Runtime runtime = Runtime.getRuntime();
        logBuilder.append(" : Max Mem ").append(runtime.maxMemory())
                .append(" : Total Mem ").append(runtime.totalMemory())
                .append(" : Free Mem ").append(runtime.freeMemory());
    }

    private void appendFailure(StringBuilder logBuilder, Throwable failure)
    {
        stateTrue("Failure can't be null", failure != null);
        logBuilder.append("\nFAILURE:");
        if (failure instanceof HallelujahTestFailureException) {
            final HallelujahTestFailureException hallelujahFailure = (HallelujahTestFailureException) failure;
            logBuilder.append(" ").append(hallelujahFailure.getMessage()).append("\n");
            logBuilder.append(hallelujahFailure.getFailure());
        }
        else {
            logBuilder.append("\n").append(ExceptionUtils.getStackTrace(failure));
        }
    }

    private void appendFailuresSoFar(StringBuilder logBuilder)
    {
        if (failedTests.size() > 0)
        {
            logBuilder.append("Test Failures So Far:\n");
            for (String failedTest : failedTests)
            {
                logBuilder.append(logPrefix).append(" FAIL : ").append(failedTest).append("\n");
            }
            appendSeparator(logBuilder);
        }
    }

    private void appendSeparator(StringBuilder logBuilder)
    {
        logBuilder.append("\n______________________________\n\n");
    }

    private static StringBuilder padRight(StringBuilder logBuilder, String stringToAdd, int padTo)
    {
        logBuilder.append(stringToAdd).append(StringUtils.repeat(" ", padTo - stringToAdd.length()));
        return logBuilder;
    }
}
