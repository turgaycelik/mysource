package com.atlassian.jira.local.listener;

import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.lang.StringUtils;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gathers stats on tests
 *
 * @since v4.3
 */
public class StatsGatheringRunListener extends RunListener
{
    private static final int SLOW_TEST_THRESHOLD = 100;

    private static class TestInfo implements Comparable
    {
        private final long startTime;
        private long runTime;
        private final String description;

        private TestInfo(Description description)
        {
            this.description = makeName(description);
            this.startTime = System.currentTimeMillis();
        }


        private long snapshotRunTime()
        {
            runTime = System.currentTimeMillis() - startTime;
            return runTime;
        }

        @Override
        public int compareTo(Object o)
        {
            return compareTo((TestInfo) o);
        }

        public int compareTo(TestInfo that)
        {
            long rc = that.runTime - this.runTime;
            if (rc == 0)
            {
                return this.description.compareTo(that.description);
            }
            return (rc > 0 ? 1 : -1);
        }
    }

    private Map<String, TestInfo> tests = new HashMap<String, TestInfo>();
    private Map<String, TestInfo> slowTests = new HashMap<String, TestInfo>();
    private Map<String, TestInfo> ignoredTests = new HashMap<String, TestInfo>();
    private List<Long> runTimes = new ArrayList<Long>();
    private long slowTestTime = 0;
    private long legacyMockTestTime = 0;
    private long legacyMockTestCount = 0;

    private static String makeName(Description description)
    {
        final Class clazz = description.getTestClass();
        final String methodName = description.getMethodName();
        if (clazz != null && StringUtils.isNotBlank(methodName))
        {
            return clazz.getSimpleName() + "." + methodName;
        }
        return description.getDisplayName();
    }


    private float pct(long total, long of)
    {
        return (((float) of / (float) total) * 100);
    }

    @Override
    public void testRunFinished(Result result) throws Exception
    {
        String dumpStats = System.getProperty("unit.test.dumpstats");
        if (dumpStats == null || !Boolean.valueOf(dumpStats))
        {
            return;
        }

        System.out.flush();

        heading("UNIT TEST RUN INFORMATION");

        final long totalRunTime = result.getRunTime();
        
        System.out.printf("Tests Run                        : %d\n", result.getRunCount());
        System.out.printf("Tests Run Time                   : %d seconds\n", totalRunTime / 1000);
        System.out.printf("Average Run Time                 : %.2f ms\n", ((float) totalRunTime / (float) result.getRunCount()));
        System.out.printf("Median Run Time                  : %d ms\n", findMedian(runTimes));
        System.out.printf("Tests Failures                   : %d\n", result.getFailureCount());
        System.out.printf("LegacyJiraMockTestCase Instances : %d\n", legacyMockTestCount);
        System.out.printf("LegacyJiraMockTestCase Time      : %d seconds /  %.2f%%\n", legacyMockTestTime / 1000, pct(totalRunTime, legacyMockTestTime ));

        List<TestInfo> slowTests = calculateSlowTests();
        int slowCount = slowTests.size();
        if (slowCount > 0)
        {
            heading("The following tests are considered SLOW (ie. > " + SLOW_TEST_THRESHOLD + "ms)");
            System.out.printf("\tTotal :  %d tests /  %.2f%%\n", slowCount, pct(result.getRunCount(), slowCount));
            System.out.printf("\tTime  :  %d seconds /  %.2f%%\n", slowTestTime/1000, pct(totalRunTime, slowTestTime));
            System.out.printf("\n");

            for (TestInfo slowTest : slowTests)
            {
                System.out.printf("\t %s \t%dms\n", pad100(slowTest.description), slowTest.runTime);
            }
        }
        if (!ignoredTests.isEmpty())
        {
            heading("The following tests are IGNORED");
            for (TestInfo testInfo : ignoredTests.values())
            {
                System.out.printf("\t %s\n", testInfo.description);
            }
        }
    }

    private String pad100(Object o)
    {
        return StringUtils.rightPad(String.valueOf(o), 100);
    }

    private static long findMedian(List<Long> data)
    {
        Collections.sort(data);
        Long result;
        final int size = data.size();
        if (size % 2 == 1)
        {
            result = data.get((int) Math.floor(size / 2));
        }
        else
        {
            Long lowerMiddle = data.get(size / 2);
            Long upperMiddle = data.get(size / 2 - 1);
            result = (lowerMiddle + upperMiddle) / 2;
        }
        return result;
    }

    private void heading(final String msg)
    {
        System.out.printf(""
                + "\n\n"
                + "------------------------------------------------------------------------------------------\n"
                + "%s\n"
                + "------------------------------------------------------------------------------------------\n", msg);
    }

    private List<TestInfo> calculateSlowTests()
    {
        ArrayList<TestInfo> list = new ArrayList<TestInfo>(slowTests.values());
        Collections.sort(list);
        return list;
    }

    @Override
    public void testStarted(Description description) throws Exception
    {
        tests.put(makeName(description), new TestInfo(description));
    }

    @Override
    public void testFinished(Description description) throws Exception
    {
        final String name = makeName(description);
        TestInfo testInfo = Assertions.notNull("testInfo", tests.get(name));
        long runTime = testInfo.snapshotRunTime();
        runTimes.add(runTime);
        if (runTime > SLOW_TEST_THRESHOLD)
        {
            slowTests.put(name, testInfo);
            slowTestTime += runTime;
        }
        tests.remove(name);
        calculateBadTestCount(description, runTime);
    }

    private void calculateBadTestCount(Description description, final long runTime)
    {
        //FIXED REFACTORING
//        final Class<?> testClass = description.getTestClass();
//        if (testClass != null && LegacyJiraMockTestCase.class.isAssignableFrom(testClass))
//        {
//            legacyMockTestCount++;
//            legacyMockTestTime += runTime;
//        }
    }

    @Override
    public void testIgnored(Description description) throws Exception
    {
        final String name = makeName(description);
        TestInfo testInfo = tests.get(name);
        ignoredTests.put(name, testInfo == null ? new TestInfo(description) : testInfo);
    }
}
