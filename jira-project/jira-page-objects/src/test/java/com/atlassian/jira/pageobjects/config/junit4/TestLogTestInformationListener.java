package com.atlassian.jira.pageobjects.config.junit4;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.buildeng.hallelujah.api.model.TestCaseFailure;
import com.atlassian.jira.functest.framework.WebTestDescription;
import com.atlassian.jira.functest.framework.suite.JUnit4WebTestDescription;
import com.atlassian.jira.functest.framework.suite.WebTestDescriptionList;
import com.atlassian.jira.hallelujah.HallelujahTestFailureException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test case for {@link com.atlassian.jira.pageobjects.config.junit4.LogTestInformationListener}.
 *
 * @since v4.4
 */
public class TestLogTestInformationListener
{

    private WebTestDescription mockSuiteDescription;

    private final List<String> logs = Lists.newArrayList();
    private boolean startCollecting = false;


    @Before
    public void initMocks()
    {
        final Description mainSuite = Description.createSuiteDescription("Test Suite");
        mainSuite.addChild(createSuite(MockTestOne.class));
        mainSuite.addChild(createSuite(MockTestTwo.class));
        mockSuiteDescription = new JUnit4WebTestDescription(mainSuite);
    }

    private Description createSuite(final Class<?> testClass)
    {
        final Description suite = Description.createSuiteDescription(testClass);
        for (final Method method : Ordering.from(String.CASE_INSENSITIVE_ORDER).onResultOf(new com.google.common.base.Function<Method, String>()
				{
					@Override
					public String apply(final Method method)
					{
						return method.getName();
					}
				}).immutableSortedCopy(ImmutableList.copyOf(testClass.getMethods())))
        {
            if (method.isAnnotationPresent(Test.class))
            {
                suite.addChild(Description.createTestDescription(testClass, method.getName(), method.getAnnotations()));
            }
        }
        return suite;
    }

    @Test
    public void shouldLogSuiteStart()
    {
        startCollectingLogs();
        new Tested().suiteStarted(mockSuiteDescription);
        assertLog("\n===== Suite 'Test Suite' has started\n",
                "===== Running 4 tests in total",
                "testOneOne(com.atlassian.jira.pageobjects.config.junit4.TestLogTestInformationListener$MockTestOne)",
                "testOneTwo(com.atlassian.jira.pageobjects.config.junit4.TestLogTestInformationListener$MockTestOne)",
                "testTwoOne(com.atlassian.jira.pageobjects.config.junit4.TestLogTestInformationListener$MockTestTwo)",
                "testTwoTwo(com.atlassian.jira.pageobjects.config.junit4.TestLogTestInformationListener$MockTestTwo)"
        );

    }

    @Test
    public void shouldLogSingleTestFailure()
    {
        final Tested tested = new Tested();
        tested.suiteStarted(mockSuiteDescription);
        final WebTestDescription test = new WebTestDescriptionList(mockSuiteDescription).singleTests().get(0);
        tested.testStarted(test);
        startCollectingLogs();
        final String stackTraceMock = "Test Stack Trace";
        tested.testFailure(test, errorWithStackTrace("Test", stackTraceMock));
        tested.testFinished(test);

        assertLogMatches("===== Test Finished FAIL"
                + escape(" : testOneOne(com.atlassian.jira.pageobjects.config.junit4.TestLogTestInformationListener$MockTestOne)")
                + escape(" #1 of 4 (25.0%)")
                + escape(" : Errors 1 (25.0%)")
                + " : Run time \\d+s"
                + " : Suite time \\d+s"
                + " : Max Mem \\d+"
                + " : Total Mem \\d+"
                + " : Free Mem \\d+"
                + escape("\nFAILURE:\n" + stackTraceMock)
                + escape("\n______________________________\n\n")
                + escape("Test Failures So Far:\n")
                + escape("===== FAIL : testOneOne(com.atlassian.jira.pageobjects.config.junit4.TestLogTestInformationListener$MockTestOne)\n")
                + "\n______________________________\n\n"
        );

    }

    @Test
    public void shouldLogTestSuiteFailure()
    {
        final Tested tested = new Tested();
        tested.suiteStarted(mockSuiteDescription);
        startCollectingLogs();
        // we simulate how JUnit4 behaves - upon failure in test class setup (e.g. @BeforeClass) only this event is raised
        final String stackTraceMock = "Test Stack Trace";
        tested.testError(mockSuiteDescription.children().iterator().next(), errorWithStackTrace("Test", stackTraceMock));
        assertLogMatches("===== Test Finished FAIL"
                // TODO add list of single failed tests of this guy?
                + escape(" : com.atlassian.jira.pageobjects.config.junit4.TestLogTestInformationListener$MockTestOne")
                + escape(" #2 of 4 (50.0%)")
                + escape(" : Errors 2 (50.0%)")
                + " : Run time \\d+s"
                + " : Suite time \\d+s"
                + " : Max Mem \\d+"
                + " : Total Mem \\d+"
                + " : Free Mem \\d+"
                + escape("\nFAILURE:\n" + stackTraceMock)
                + escape("\n______________________________\n\n")
                + escape("Test Failures So Far:\n")
                + escape("===== FAIL : com.atlassian.jira.pageobjects.config.junit4.TestLogTestInformationListener$MockTestOne\n")
                + "\n______________________________\n\n"
        );

    }


    @Test
    public void shouldLogSingleTestFailureFromHallelujahClient()
    {
        final Tested tested = new Tested();
        tested.suiteStarted(mockSuiteDescription);
        final WebTestDescription test = new WebTestDescriptionList(mockSuiteDescription).singleTests().get(0);
        tested.testStarted(test);
        startCollectingLogs();
        final HallelujahTestFailureException failure = new HallelujahTestFailureException(
                "Failure message", new TestCaseFailure("type", "message", "Test Stack Trace"));
        tested.testFailure(test, failure);
        tested.testFinished(test);

        assertLogMatches("===== Test Finished FAIL"
                + escape(" : testOneOne(com.atlassian.jira.pageobjects.config.junit4.TestLogTestInformationListener$MockTestOne)")
                + escape(" #1 of 4 (25.0%)")
                + escape(" : Errors 1 (25.0%)")
                + " : Run time \\d+s"
                + " : Suite time \\d+s"
                + " : Max Mem \\d+"
                + " : Total Mem \\d+"
                + " : Free Mem \\d+"
                + escape("\nFAILURE: " + failure.getMessage() + '\n' + failure.getFailure())
                + escape("\n______________________________\n\n")
                + escape("Test Failures So Far:\n")
                + escape("===== FAIL : testOneOne(com.atlassian.jira.pageobjects.config.junit4.TestLogTestInformationListener$MockTestOne)\n")
                + "\n______________________________\n\n"
        );

    }

    private Throwable errorWithStackTrace(final String message, final String stackTrace) {
        return new AssertionError(message) {
            @Override
            public void printStackTrace(final PrintWriter s)
            {
                s.write(stackTrace);
            }
        };
    }

    private String escape(final String pattern)
    {
        return pattern.replaceAll("\\.", "\\\\.").replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)")
                .replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")
                .replaceAll(Pattern.quote("$"), Matcher.quoteReplacement("\\$"));
    }

    private void startCollectingLogs()
    {
        startCollecting = true;
    }

    private void assertLog(final String... expected)
    {
        int i=0;
        for (final String expectedMsg : expected)
        {
            assertEquals("Unexpected message in position " + i, expectedMsg, logs.get(i++));
        }
    }

    private void assertLogMatches(final String regexp)
    {
        assertEquals(1, logs.size());
        final String actual = logs.get(0);
        assertTrue(format("Actual log\n%s\ndoes not match expected regex:\n%s",actual, regexp),
                Pattern.compile(regexp, Pattern.DOTALL).matcher(actual).matches());
    }

    private class Tested extends LogTestInformationListener
    {
        @Override
        protected void log(final String logMessage)
        {
            if (startCollecting)
            {
                logs.add(logMessage);
            }
        }
    }

    public static class MockTestOne
    {

        @Test
        public void testOneOne()
        {
        }

        @Test
        public void testOneTwo()
        {
        }
    }

    public static class MockTestTwo
    {

        @Test
        public void testTwoOne()
        {
        }

        @Test
        public void testTwoTwo()
        {
        }
    }
}