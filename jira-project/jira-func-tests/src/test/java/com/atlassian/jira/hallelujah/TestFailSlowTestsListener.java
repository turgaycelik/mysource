package com.atlassian.jira.hallelujah;

import com.atlassian.buildeng.hallelujah.api.model.TestCaseFailure;
import com.atlassian.buildeng.hallelujah.api.model.TestCaseResult;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test for FailSlowTestsListener
 * @since v6.0
 */
public class TestFailSlowTestsListener
{
    private FailSlowTestsListener listener;

    @Before
    public void setUp() throws Exception
    {
        listener = new FailSlowTestsListener(1, TimeUnit.MINUTES);
    }

    @Test
    public void shouldAdd5MinutesToFirstTestTimeout()
    {
        final TestCaseResult result = getTestCaseResult("1", TimeUnit.MINUTES.toSeconds(5));
        listener.onResultReceived(null, null, result);
        assertTrue("Test should not fail", result.passed);
    }

    @Test
    public void shouldFailTestIfFirstTookMoreThanLimitAndExpectedJIRAStartup()
    {
        final TestCaseResult result = getTestCaseResult("1", TimeUnit.MINUTES.toSeconds(10));
        listener.onResultReceived(null, null, result);
        assertFalse("Test should fail", result.passed);
        assertNotNull("Failure should be set",result.failure);
        assertThat(result.failure.message,CoreMatchers.is("Test failed because its execution exceeded 60000 ms"));
        assertThat(result.failure.type,CoreMatchers.is("timeout"));
        assertThat(result.failure.log,CoreMatchers.is(""));
    }

    @Test
    public void shouldCountSeparatelyFirstTestsForRunners()
    {
        for (int i = 0; i < 10; i++)
        {
            testTwoResultsWithMaximumTimeouts("" + i);
        }
    }

    @Test
    public void shouldFailSecondTestIfItDidNotFinishedWithinLimit()
    {
        final TestCaseResult result1 = getTestCaseResult("1", TimeUnit.MINUTES.toSeconds(4));
        final TestCaseResult result2 = getTestCaseResult("1", TimeUnit.MINUTES.toSeconds(4));
        listener.onResultReceived(null, null, result1);
        listener.onResultReceived(null, null, result2);
        assertTrue("Test should not fail", result1.passed);
        assertFalse("Test should fail", result2.passed);
    }

    @Test
    public void shouldNotChangeMessageIfFailedTestTookLongerThanTimeout()
    {
        final TestCaseResult result = getTestCaseResult("1", TimeUnit.MINUTES.toSeconds(10));
        result.passed = false;
        final TestCaseFailure testCaseFailure = new TestCaseFailure("type1", "message", "log");
        result.failure = testCaseFailure;
        listener.onResultReceived(null, null, result);
        assertFalse("Test should fail", result.passed);
        assertThat(result.failure, CoreMatchers.is(testCaseFailure));
    }

    private void testTwoResultsWithMaximumTimeouts(String runnerId)
    {
        final TestCaseResult result1 = getTestCaseResult(runnerId, TimeUnit.MINUTES.toSeconds(5));
        final TestCaseResult result2 = getTestCaseResult(runnerId, TimeUnit.MINUTES.toSeconds(1));
        listener.onResultReceived(null, null, result1);
        listener.onResultReceived(null, null, result2);
        assertTrue("Test should not fail", result1.passed);
        assertTrue("Test should not fail", result2.passed);
    }

    private TestCaseResult getTestCaseResult(String runnerId, long durationInSeconds)
    {
        final TestCaseResult testCaseResult = new TestCaseResult();
        testCaseResult.setRunnerId(runnerId);
        testCaseResult.passed = true;
        testCaseResult.duration = 1.0 * durationInSeconds;
        return testCaseResult;
    }
}
