package com.atlassian.jira.functest.framework.dump;

import com.atlassian.jira.functest.framework.FuncTestWebClientListener;
import com.atlassian.jira.functest.framework.util.testcase.TestCaseKit;
import junit.framework.TestCase;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains common code for generated test messages and the like.  This is used by both the old JIRAWebtest code and the
 * new FuncTestCase code.
 * <p/>
 * This code uses static variables however thats OK because the func tests are single threaded and run one after the
 * other, so statics are fine.
 *
 * @since v4.0
 */
public class TestInformationKit extends com.atlassian.jira.testkit.client.dump.TestInformationKit
{
    private static long suiteStartTime = System.currentTimeMillis();
    private static long suiteTotalTestCases = 0;
    private static long suiteRanSoFar = 0;
    private static long suiteTestErrorCount = 0;
    private static String currentTestName = null;

    private static TestCaseInfo suiteTotals = new TestCaseInfo("suiteTotals", true, 0, 0, 0, 0, 0, 0, 0, 0);

    private static List<String> failingTestCases = new ArrayList<String>();
    private static List<TestCaseInfo> allTestsRun = new ArrayList<TestCaseInfo>();

    private static final String FTC_PREFIX = "===FTC";

    public synchronized static void startTestSuite(int totalTestCases)
    {
        suiteTotalTestCases = totalTestCases;
        suiteStartTime = System.currentTimeMillis();
    }

    public synchronized static String getStartMsg(TestCase testCase, String tenant)
    {
        suiteRanSoFar++;
        currentTestName = TestCaseKit.getFullName(testCase);

        StringBuilder message = new StringBuilder()
                .append(FTC_PREFIX);
        if (tenant != null)
        {
            message.append(" (").append(tenant).append(")");
        }
        message.append(" Started : ")
                .append(currentTestName)
                .append(" #")
                .append(suiteRanSoFar)
                .append(" of ")
                .append(suiteTotalTestCases);
        return message.toString();
    }

    public static String getEndMsg(TestCase testCase, String tenant, long runtimeMS)
    {
        return getEndMsg(testCase, tenant, runtimeMS, null, null);
    }

    public static String getEndMsg(TestCase testCase, String tenant, long runtimeMS, FuncTestWebClientListener webClientListener)
    {
        return getEndMsg(testCase, tenant, runtimeMS, webClientListener, null);
    }

    public static String getEndMsg(TestCase testCase, String tenant, long runtimeMS, Throwable problemT)
    {
        return getEndMsg(testCase, tenant, runtimeMS, null, problemT);
    }

    public synchronized static String getEndMsg(TestCase testCase, String tenant, long runtimeMS, FuncTestWebClientListener webClientListener, Throwable problemT)
    {
        final float runtimeMSF = runtimeMS / 1000f;
        final float suiteTimeMSF = (System.currentTimeMillis() - suiteStartTime) / 1000f;
        final DecimalFormat fmt = new DecimalFormat("#0.00");
        final String runtimeMsg = (runtimeMSF == 1 ? "1 second" : fmt.format(runtimeMSF) + " seconds");
        final String suitTimeMsg = (suiteTimeMSF == 1 ? "1 second" : fmt.format(suiteTimeMSF) + " seconds");
        final Runtime rt = Runtime.getRuntime();
        final boolean testPassed = problemT == null;

        if (!testPassed)
        {
            suiteTestErrorCount++;
        }

        DecimalFormat dfPerCent = new DecimalFormat("#0.0#%");

        final double perDone = suiteTotalTestCases == 0 ? 0 : (double) suiteRanSoFar / (double) suiteTotalTestCases;
        final double perErrors = suiteTotalTestCases == 0 ? 0 : (double) suiteTestErrorCount / (double) suiteTotalTestCases;

        final String testName = TestCaseKit.getFullName(testCase);

        StringBuilder sb = new StringBuilder(FTC_PREFIX);
        if (tenant != null)
        {
            sb.append(" (").append(tenant).append(")");
        }
        sb.append(" Finished")
                .append(problemT == null ? "" : " FAIL")
                .append(" : ")
                .append(testName)
                .append(" #").append(suiteRanSoFar)
                .append(" of ").append(suiteTotalTestCases)
                .append(" (").append(dfPerCent.format(perDone)).append(")")

                .append(" : Errors ").append(suiteTestErrorCount)
                .append(" (").append(dfPerCent.format(perErrors)).append(")")

                .append(" : Run time ").append(runtimeMsg)

                .append(" : Suite time ").append(suitTimeMsg);

        final TestCaseInfo testCaseInfo;
        if (webClientListener != null)
        {
            final long httpRequestCount = webClientListener.getRequestCount();
            final long httpTimeMS = webClientListener.getRequestTime();
            final long http100thMS = webClientListener.getPercentileRequestTime(100);
            final long http90thMS = webClientListener.getPercentileRequestTime(90);
            final long http50thMS = webClientListener.getPercentileRequestTime(50);
            final double msPerRequest = httpRequestCount == 0 ? 0 : (double) httpTimeMS / (double) httpRequestCount;

            final long parseTimeNanos = webClientListener.getParseTimeNanos();
            final long parseCount = webClientListener.getParseCount();


            sb.append("")
                    .append(" : HTTP Count ").append(httpRequestCount)
                    .append(" : HTTP Time ").append(httpTimeMS).append("ms")
                    .append(" : HTTP Ave ").append(fmt.format(msPerRequest / 1000d)).append(" ms/request")
                    .append(" : HTTP 100th ").append(http100thMS).append("ms")
                    .append(" : HTTP 90th ").append(http90thMS).append("ms")
                    .append(" : HTTP 50th ").append(http50thMS).append("ms")
                    .append(" : Parse Time ").append(parseTimeNanos).append("nanos")
                    .append(" : Parse Count ").append(parseCount);

            testCaseInfo = new TestCaseInfo(testName, testPassed, runtimeMS, httpRequestCount, httpTimeMS, http100thMS, http90thMS, http50thMS, parseTimeNanos, parseCount);

            suiteTotals = suiteTotals.add(testCaseInfo);
        }
        else
        {
            testCaseInfo = new TestCaseInfo(testName, testPassed, runtimeMS, -1, -1, -1, -1, -1, -1, -1);
        }

        allTestsRun.add(testCaseInfo);


        sb.append("")
                .append(" : Max Mem ").append(rt.maxMemory())
                .append(" : Total Mem ").append(rt.totalMemory())
                .append("  : Free Mem ").append(rt.freeMemory());

        if (problemT != null)
        {
            sb.append("\n [").append(problemT).append("]");
            addFailingTest(testCase);
        }
        if (failingTestCases.size() > 0)
        {
            sb.append("\n______________________________\n");
            sb.append("Test Failures So Far :\n");
            for (String failingTestCase : failingTestCases)
            {
                sb.append(FTC_PREFIX).append(" FAIL : ").append(failingTestCase).append("\n");
            }
        }
        sb.append("\n______________________________\n");

        return sb.toString();
    }

    private final static int MAX = 15;

    private static void addFailingTest(final TestCase testCase)
    {
        final String testName = testCase.getClass().getName() + "." + testCase.getName();
        final int size = failingTestCases.size();
        if (size <= MAX)
        {
            if (size == MAX)
            {
                failingTestCases.add("More than " + MAX + " failures...");
            }
            else
            {
                failingTestCases.add(testName);
            }
        }
    }

    public synchronized static List<TestCaseInfo> getAllTestsRun()
    {
        return allTestsRun;
    }

    public synchronized static TestCaseInfo getTotals()
    {
        return suiteTotals;
    }

    public static class TestCaseInfo implements Comparable
    {
        private final String testName;
        private final boolean testPassed;
        private final long runtimeMS;

        final long httpRequestCount;
        final long httpTimeMS;
        final long http100thMS;
        final long http90thMS;
        final long http50thMS;
        final long parseTimeNanos;
        final long parseCount;
        final double msPerRequest;

        public TestCaseInfo(final String testName, final boolean testPassed, final long runtimeMS, final long httpRequestCount, final long httpTimeMS, final long http100thMS, final long http90thMS, final long http50thMS, long parseTimeNanos, long parseCount)
        {
            this.testName = testName;
            this.testPassed = testPassed;
            this.runtimeMS = runtimeMS;
            this.httpRequestCount = httpRequestCount;
            this.httpTimeMS = httpTimeMS;
            this.http100thMS = http100thMS;
            this.http90thMS = http90thMS;
            this.http50thMS = http50thMS;
            this.msPerRequest = httpRequestCount == 0 ? 0 : (double) httpTimeMS / (double) httpRequestCount;
            this.parseTimeNanos = parseTimeNanos;
            this.parseCount = parseCount;
        }

        public int compareTo(final Object o)
        {
            TestCaseInfo that = (TestCaseInfo) o;
            return (int) (this.runtimeMS - that.runtimeMS);
        }

        public String getTestName()
        {
            return testName;
        }

        public boolean isTestPassed()
        {
            return testPassed;
        }

        public long getRuntimeMS()
        {
            return runtimeMS;
        }

        public long getHttpRequestCount()
        {
            return httpRequestCount;
        }

        public long getHttpTimeMS()
        {
            return httpTimeMS;
        }

        public long getHttp100thMS()
        {
            return http100thMS;
        }

        public long getHttp90thMS()
        {
            return http90thMS;
        }

        public long getHttp50thMS()
        {
            return http50thMS;
        }

        public double getMsPerRequest()
        {
            return msPerRequest;
        }

        public long getParseTimeNanos()
        {
            return parseTimeNanos;
        }

        public long getParseTimeMS()
        {
            float v = (float) parseTimeNanos / 1000000f;
            return (long) v;
        }

        public long getParseCount()
        {
            return parseCount;
        }

        public TestCaseInfo add(TestCaseInfo right)
        {
            return new TestInformationKit.TestCaseInfo(this.getTestName(), true,
                    this.getRuntimeMS() + right.getRuntimeMS(),
                    this.getHttpRequestCount() + right.getHttpRequestCount(),
                    this.getHttpTimeMS() + right.getHttpTimeMS(),
                    0, 0, 0,
                    this.getParseTimeNanos() + right.getParseTimeNanos(),
                    this.getParseCount() + right.getParseCount());
        }
    }
}
