package com.atlassian.jira.hallelujah;

import com.atlassian.buildeng.hallelujah.api.model.TestCaseFailure;
import com.atlassian.buildeng.hallelujah.api.model.TestCaseResult;
import com.atlassian.buildeng.hallelujah.api.server.AbstractServerListener;
import com.atlassian.buildeng.hallelujah.api.server.ServerTestCaseProvider;
import com.atlassian.buildeng.hallelujah.api.server.ServerTestCaseResultCollector;
import com.google.common.collect.Maps;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * This listener simply fails test if it took longer than {@link #maxAllowedDurationMS}
 *
 * @since v6.0
 */
public class FailSlowTestsListener extends AbstractServerListener
{
    private static final long SECOND_IN_MS = TimeUnit.SECONDS.toMillis(1l);
    private static final long MAX_EXPECTED_JIRA_SETUP = TimeUnit.MINUTES.toMillis(5);
    private static final String TIMEOUT_MESSAGE = "timeout";

    private final long maxAllowedDurationMS;
    private final ConcurrentMap<String, Boolean> visitedRunners = Maps.newConcurrentMap();
    private final String failMessage;

    public FailSlowTestsListener(long maxAllowedDuration, TimeUnit unit)
    {
        this.maxAllowedDurationMS = unit.toMillis(maxAllowedDuration);
        failMessage = "Test failed because its execution exceeded " + maxAllowedDurationMS + " ms";
    }

    @Override
    public boolean onResultReceived(ServerTestCaseResultCollector serverTestCaseResultCollector, ServerTestCaseProvider serverTestCaseProvider, TestCaseResult testCaseResult)
    {
        final long maxTime;
        if (visitedRunners.putIfAbsent(testCaseResult.runnerId, Boolean.TRUE) == null)
        {
            //this is the first test that was run in this runner so it started JIRA lets add 5 minutes not to fail prematurely
            maxTime = maxAllowedDurationMS + MAX_EXPECTED_JIRA_SETUP;
        }
        else
        {
            maxTime = maxAllowedDurationMS;
        }

        if (testCaseResult.passed && timeExceded(maxTime, testCaseResult))
        {
            testCaseResult.passed = false;
            testCaseResult.failure = getTestCaseFailure();
        }
        return true;
    }

    private TestCaseFailure getTestCaseFailure()
    {
        final TestCaseFailure failure = new TestCaseFailure();
        failure.message = failMessage;
        failure.type = TIMEOUT_MESSAGE;
        failure.log = "";
        return failure;

    }

    private boolean timeExceded(long maxTime, TestCaseResult testCaseResult)
    {
        return testCaseResult.duration * SECOND_IN_MS > maxTime;
    }
}
