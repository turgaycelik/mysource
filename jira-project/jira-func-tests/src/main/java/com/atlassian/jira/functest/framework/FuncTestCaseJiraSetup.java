package com.atlassian.jira.functest.framework;

import com.atlassian.jira.functest.framework.setup.JiraSetupInstanceHelper;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import junit.framework.TestCase;
import net.sourceforge.jwebunit.WebTester;

/**
 * This class is responsible for "setting up"  and "tearing down" JIRA from a functional test point of view.   The
 * {@link com.atlassian.jira.functest.framework.FuncTestCase} uses this class in its setUp() and tearDown() method.  if
 * you dont want to derive from {@link com.atlassian.jira.functest.framework.FuncTestCase}, you can call this class
 * directly.
 *
 * @since v3.13
 */
public class FuncTestCaseJiraSetup extends AbstractFuncTestUtil
{
    private long startTime;
    private static long classStartTime = System.currentTimeMillis();

    private final Navigation navigation;

    /**
     * Call this in your TestCase setUp() method to setup JIRA from the WebTester point of view. This will check if JIRA
     * is "setup" and if not it will setup JIRA in such a way as to be useable.
     *
     * @param testCase the JUnit {@link junit.framework.TestCase} in play
     * @param tester the {@link net.sourceforge.jwebunit.WebTester}
     * @param environmentData the {@link com.atlassian.jira.webtests.util.JIRAEnvironmentData} in play
     * @param navigation a {@link com.atlassian.jira.functest.framework.Navigation}
     * @param webClientListener the web client listener to record traffic with
     * @param skipSetup
     */
    public FuncTestCaseJiraSetup(TestCase testCase, WebTester tester, JIRAEnvironmentData environmentData, Navigation navigation,
            final FuncTestWebClientListener webClientListener, boolean skipSetup)
    {
        super(tester, environmentData, 2);
        this.environmentData = environmentData;
        startTime = System.currentTimeMillis();
        this.navigation = navigation;

        if (!skipSetup)
        {
            new JiraSetupInstanceHelper(tester, environmentData).ensureJIRAIsReadyToGo(webClientListener);
        }
    }

    /**
     * Called during test tearDown() to logout and cleanup any resources
     *
     * @param testCase the Junit {@link junit.framework.TestCase}
     */
    public void tearDown(TestCase testCase)
    {
        String testName = testCase.getClass().getName() + "." + testCase.getName();
        try
        {
            navigation.logout();
        }
        catch (RuntimeException rte)
        {
            log("Could not logout on '" + testName + ".tearDown()'");
        }
    }

    /**
     * Returns how long the test has been running for since setUp()
     *
     * @return how long the test has been running for since setUp()
     */
    public long getRuntimeMillis()
    {
        return getRuntimeMillis(this.startTime);
    }

    /**
     * Returns how long the test has been running for since startTime
     *
     * @param startTime the time that test started
     * @return how long the test has been running for since startTime
     */
    public static long getRuntimeMillis(long startTime)
    {
        return (System.currentTimeMillis() - startTime);
    }

    /**
     * @return time since this class (and hence the test suite) was initialised.
     */
    public static long getSuiteRuntimeMillis()
    {
        return (System.currentTimeMillis() - classStartTime);
    }

    static String getTestName(TestCase testCase)
    {
        return testCase.getClass().getName() + "." + testCase.getName();
    }
}
