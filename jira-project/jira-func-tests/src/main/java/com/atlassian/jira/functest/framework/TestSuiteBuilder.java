package com.atlassian.jira.functest.framework;

import com.atlassian.jira.testkit.client.log.FuncTestOut;
import com.atlassian.jira.functest.framework.dump.FuncTestTimingsListener;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.IteratorEnumeration;
import com.atlassian.jira.webtests.util.EnvironmentAware;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.util.TenantOverridingEnvironmentData;
import com.atlassian.jira.webtests.util.TestCaseMethodNameDetector;
import com.atlassian.jira.webtests.ztests.misc.TestDefaultJiraDataFromInstall;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.junit.Ignore;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.atlassian.jira.functest.framework.util.testcase.TestCaseKit.getFullName;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Divides up a set of tests such that they can be performed in parallel. It does this by taking a set of tests and
 * dividing them up into composite test of roughly even size.
 * <p/>
 * If a passed test is annotated with {@link com.atlassian.jira.functest.framework.Splitable} then its individual test
 * methods will be broken out into the different tests. All other tests have all their test methods within the same
 * batch.
 */
public final class TestSuiteBuilder
{
    private int maxBatch;
    private int batch;
    private boolean log;
    private boolean parallel;
    private long timeout;
    private long delay;
    private Predicate<WebTestDescription> watchPredicate;

    private final Set<Class<? extends TestCase>> testClasses = new LinkedHashSet<Class<? extends TestCase>>();
    private final List<Test> tests = new ArrayList<Test>();

    /**
     * Create a splitter that will return the given batch from the specified number of total batches.
     *
     * @param batch the batch the splitter should return. A value of -1 can be specified when maxBatch is also passed
     * -1. This indicates that no batching should be performed.
     * @param maxBatch the number of batches that splitter should divide the tests into. A value of -1 can be specified
     * when batch is also passed -1. This indicates that no batching should be performed.
     */
    public TestSuiteBuilder(int batch, int maxBatch)
    {
        batch(batch).maxBatch(maxBatch).watch(-1, -1, null).log(false);
    }

    /**
     * Create a no-op splitter, that is, the passed tests will not be batched. Same as @{code TestSplitter(-1, -1) }.
     */
    public TestSuiteBuilder()
    {
        this(-1, -1);
    }

    /**
     * Add the passed tests to the splitter for division.
     *
     * @param tests the set of tests to divide.
     * @return a reference to this.
     */
    public TestSuiteBuilder addTests(Collection<Class<? extends TestCase>> tests)
    {
        testClasses.addAll(tests);
        return this;
    }

    public TestSuiteBuilder addTests(Class<? extends TestCase>... tests)
    {
        return addTests(Arrays.asList(tests));
    }

    public TestSuiteBuilder addTest(Class<? extends TestCase> test)
    {
        testClasses.add(test);
        return this;
    }

    /**
     * Adds an individual test method to the suite.
     * This is currently experimental and only intended to be used in "blame" mode.
     *
     * @param testClass the TestCase class
     * @param methodName method name
     * @return this TestSuiteBuilder
     */
    public TestSuiteBuilder addSingleTestMethod(Class<? extends TestCase> testClass, String methodName)
    {
        tests.add(TestSuite.createTest(testClass, methodName));
        return this;
    }

    public TestSuiteBuilder log(boolean log)
    {
        this.log = log;
        return this;
    }

    public TestSuiteBuilder batch(int batch)
    {
        if (batch == 0)
        {
            throw new IllegalArgumentException("batch == 0");
        }

        this.batch = batch;
        return this;
    }

    public TestSuiteBuilder maxBatch(int maxBatch)
    {
        if (maxBatch == 0)
        {
            throw new IllegalArgumentException("maxBatch == 0");
        }

        this.maxBatch = maxBatch;
        return this;
    }

    public TestSuiteBuilder parallel(boolean parallel)
    {
        this.parallel = parallel;
        return this;
    }

    public TestSuiteBuilder watch(long timeout, long delay, TimeUnit unit, Class<? extends Test>... classes)
    {
        if (timeout == 0)
        {
            throw new IllegalArgumentException("timeout == 0");
        }
        if (timeout < 0)
        {
            this.delay = this.timeout = -1;
            this.watchPredicate = null;
        }
        else
        {
            if (unit == null)
            {
                throw new NullPointerException("unit == null");
            }
            if (delay <= 0)
            {
                throw new NullPointerException("delay <= 0");
            }

            this.timeout = unit.toMillis(timeout);
            this.delay = unit.toMillis(delay);
            if (classes != null && classes.length > 0)
            {
                watchPredicate = new SubClassPredicate(Arrays.asList(classes));
            }
            else
            {
                watchPredicate = Predicates.alwaysTrue();
            }
        }
        return this;
    }

    /**
     * Return true if and only if the splitter will be batching tests.
     *
     * @return true if and only if the splitter will be batching tests.
     */
    private boolean isBatchMode()
    {
        return batch >= 0;
    }

    /**
     * Create the composite test that represents the batch.
     *
     * @return the composite test that represents the batch.
     */
    public Test build()
    {
        Test test;
        if (parallel)
        {
            test = createParallelTests();
        }
        else
        {
            test = isBatchMode() ? createTestBatch(batch, maxBatch) : createAllTest();
        }
        return wrapTest(test);
    }

    private Test wrapTest(Test test)
    {
        WebTestListener listeners;
        if (timeout > 0)
        {
            listeners = CompositeSuiteListener.of(
                    new JiraTestWatchDog(watchPredicate, timeout, delay, TimeUnit.MILLISECONDS, 4, WatchdogLoggingCallback.INSTANCE),
                    new FuncTestTimingsListener());
        }
        else
        {
            listeners = new FuncTestTimingsListener();
        }
        return new SuiteListenerWrapper(test, listeners);
    }

    private Test createParallelTests()
    {
        TestSuite suite = new ParallelEnvironmentTestSuite();
        for (int i = 1; i <= maxBatch; i++)
        {
            suite.addTest(createTestBatch(i, maxBatch));
        }
        return suite;
    }

    private Test createAllTest()
    {
        TestSuite suite = new EnvironmentTestSuite();
        // Adding entire Test Classes (normal mode)
        for (Class<? extends TestCase> testClass : testClasses)
        {
            suite.addTestSuite(testClass);
        }
        // Adding individual tests (only for Blame mode)
        for (Test test : tests)
        {
            suite.addTest(test);
        }

        if (log)
        {
            FuncTestOut.log("** Tests in global **");
            outputTest(suite);
            outputIgnored(suite);
            FuncTestOut.log("** End tests in global **");
        }

        return suite;
    }

    private Test createTestBatch(int batchNo, int maxBatches)
    {
        checkBatchState(batchNo, maxBatches);

        int numberOfTests = 0;
        final List<TestPair> tests = new ArrayList<TestPair>(testClasses.size());

        //Count the number of tests excluding any tests that are going to be split.

        for (Class<? extends TestCase> testClass : testClasses)
        {
            EnvironmentTestSuite currentTest = new EnvironmentTestSuite(testClass);
            if (!isBatchable(testClass))
            {
                //If the test is not batchable then add it to the last batch. This is generally the batch with the
                //least number of tests.
                if (batchNo == maxBatches && currentTest.countTestCases() > 0)
                {
                    tests.add(new TestPair(testClass, currentTest));
                }
            }
            else
            {
                if (isSplittable(testClass))
                {
                    //We don't count split tests as they are already divided as necessary.
                    currentTest = splitSuite(currentTest, batchNo, maxBatches);
                }
                else
                {
                    numberOfTests += currentTest.countTestCases();
                }
                if (!currentTest.allTests().isEmpty())
                {
                    tests.add(new TestPair(testClass, currentTest));
                }
            }
        }

        final TestSuite suite = new EnvironmentTestSuite();

        int currentBatchSize = numberOfTests / maxBatches;
        if (numberOfTests % maxBatches > 0)
        {
            currentBatchSize++;
        }

        //Now we know how many tests there are in total. We now need to div them up. We do this by working out
        //what we expect the current size of the batch should be. We continually add tests to the batch until we reach
        //or exceed this size. We then recalculate the batch size and continue until we have divided up all the
        //batches. Note, splittable tests have already been handled when they where split so we just add them to the test
        //always.

        int batch = 1;
        int size = 0;

        for (TestPair pair : tests)
        {
            if (!isBatchable(pair.getKlazz()))
            {
                //Tests not batchable so lets just execute it.
                suite.addTest(pair.getTest());
            }
            else if (isSplittable(pair.getKlazz()))
            {
                suite.addTest(pair.getTest());
            }
            else if (batch <= batchNo)
            {
                if (batch == batchNo)
                {
                    suite.addTest(pair.getTest());
                }
                size += pair.getTest().countTestCases();
                if (size >= currentBatchSize)
                {
                    numberOfTests -= size;
                    final int remainingBatches = maxBatches - batch;
                    if (remainingBatches > 0)
                    {
                        currentBatchSize = numberOfTests / remainingBatches;
                        if (numberOfTests % remainingBatches > 0)
                        {
                            currentBatchSize++;
                        }
                    }
                    else
                    {
                        assert numberOfTests == 0;
                        currentBatchSize = 0;
                    }
                    batch++;
                    size = 0;
                }
            }
        }

        if (log)
        {
            FuncTestOut.log(String.format("** Tests in batch %d of %d **", batchNo, maxBatches));
            outputTest(suite);
            outputIgnored(suite);
            FuncTestOut.log(String.format("** End tests in batch %d of %d **", batchNo, maxBatches));
        }
        return suite;
    }

    private void checkBatchState(int batchNo, int maxBatches)
    {
        if (maxBatches < 0)
        {
            throw new IllegalStateException(String.format("Invalid maxBatch(%d) when batch(%d) >= 0.", maxBatches, batchNo));
        }

        if (batchNo > maxBatches)
        {
            throw new IllegalStateException(String.format("batch(%d) > maxBatch(%d).", batchNo, maxBatches));
        }
    }

    private static boolean isBatchable(Class<?> testClass)
    {
        //We only want to batch functional tests. These tests implement EnvironmentAware currently.
        return EnvironmentAware.class.isAssignableFrom(testClass);
    }

    private static boolean isSplittable(Class<? extends TestCase> testClass)
    {
        return testClass.getAnnotation(Splitable.class) != null;
    }

    private EnvironmentTestSuite splitSuite(final TestSuite splitTest, int batchNo, int maxBatches)
    {
        final EnvironmentTestSuite newSuite = new EnvironmentTestSuite(splitTest.getName());
        final List<TestCase> newTests = new ArrayList<TestCase>(splitTest.countTestCases());
        for (Enumeration<Test> enumeration = splitTest.tests(); enumeration.hasMoreElements();)
        {
            final Test innerTest = enumeration.nextElement();
            if (innerTest instanceof TestCase)
            {
                newTests.add((TestCase) innerTest);
            }
            else if (batchNo == 1)
            {
                //Add all non-TestCase tests to the first batch.
                newSuite.addTest(innerTest);
            }
        }

        Collections.sort(newTests, new Comparator<TestCase>()
        {
            public int compare(TestCase o1, TestCase o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });

        //This is the size of the batch.
        final int testsInBatch = newTests.size() / maxBatches;

        //This is the extra tests that need to be added to be batch. Basically, for all batches <= testsInBatchRem
        //there will need to be an extra test added.
        final int testsInBatchRem = newTests.size() % maxBatches;

        //Calculate the start position for the batch.
        int startPos = testsInBatch * (batchNo - 1);
        if (testsInBatchRem != 0)
        {
            //remember each batch before testsInBatchRem have an extra test.
            if (batchNo <= testsInBatchRem)
            {
                startPos += (batchNo - 1);
            }
            else
            {
                startPos += testsInBatchRem;
            }
        }

        if (startPos < newTests.size())
        {
            //calculate the end position of the batch.
            int endPos = testsInBatch + startPos;
            if (endPos > newTests.size())
            {
                endPos = newTests.size();
            }
            else if (endPos < newTests.size() && batchNo <= testsInBatchRem)
            {
                //Need to add extra test to cover testsInBatchRem.
                endPos++;
            }

            for (TestCase testCase : newTests.subList(startPos, endPos))
            {
                newSuite.addTest(testCase);
            }
        }

        return newSuite;
    }

    private static void outputTest(TestSuite suite)
    {
        final Enumeration<?> enumeration = suite.tests();
        while (enumeration.hasMoreElements())
        {
            Test nextTest = (Test) enumeration.nextElement();
            if (nextTest instanceof TestSuite)
            {
                outputTest((TestSuite) nextTest);
            }
            else
            {
                outputTestCase(nextTest);
            }
        }
    }

    private static void outputIgnored(TestSuite suite)
    {
        FuncTestOut.log("** Ignored tests **");
        outputIgnoredRecursively(suite);
    }

    private static void outputIgnoredRecursively(TestSuite suite)
    {
        if (suite instanceof EnvironmentTestSuite)
        {
            EnvironmentTestSuite ets = (EnvironmentTestSuite) suite;
            for (IgnoredTest test : ets.ignoredTests())
            {
                outputIgnoredTestCase(test.wrapped, getReason(test));
            }
        }
    }

    private static String getReason(IgnoredTest test)
    {
        return isNotEmpty(test.reason) ? test.reason : "not provided";
    }

    private static void outputTestCase(Test nextTest)
    {
        if (nextTest instanceof TestCase)
        {
            FuncTestOut.log(getFullName((TestCase) nextTest));
        }
        else
        {
            FuncTestOut.log("Unknown Test: " + nextTest);
        }
    }

    private static void outputIgnoredTestCase(Test nextTest, String reason)
    {
        if (nextTest instanceof TestCase)
        {
            FuncTestOut.log(getFullName((TestCase) nextTest) + " --- REASON: " + reason);
        }
        else
        {
            FuncTestOut.log("Unknown Test: " + nextTest + " --- REASON: " + reason);
        }
    }

    private final static class TestPair
    {
        private final Class<? extends TestCase> klazz;
        private final Test test;

        private TestPair(Class<? extends TestCase> klazz, Test test)
        {
            this.klazz = klazz;
            this.test = test;
        }

        private Class<? extends TestCase> getKlazz()
        {
            return klazz;
        }

        private Test getTest()
        {
            return test;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    /**
     * This is a TestSuite that implements EnvironmentAware. We need this so that the tests run correctly under cargo.
     */
    private static class EnvironmentTestSuite extends TestSuite implements EnvironmentAware
    {
        private JIRAEnvironmentData environmentData;

        public EnvironmentTestSuite()
        {
            super();
        }

        public EnvironmentTestSuite(final Class<? extends TestCase> theClass)
        {
            super(theClass);
        }

        public EnvironmentTestSuite(final String name)
        {
            super(name);
        }

        public void setEnvironmentData(final JIRAEnvironmentData environmentData)
        {
            this.environmentData = environmentData;
            for (final Enumeration<?> enumeration = tests(); enumeration.hasMoreElements();)
            {
                Object testObj = enumeration.nextElement();
                if (testObj instanceof EnvironmentAware)
                {
                    ((EnvironmentAware) testObj).setEnvironmentData(environmentData);
                }
            }
        }

        @Override
        public void addTestSuite(final Class<? extends TestCase> testClass)
        {
            addTest(new EnvironmentTestSuite(testClass));
        }

        @Override
        public void addTest(Test test)
        {
            if (isIgnored(test))
            {
                super.addTest(new IgnoredTest(test, extractIgnoreReason(test)));
            }
            else
            {
                super.addTest(test);
            }
        }

        @Override
        public Enumeration<Test> tests()
        {
            return IteratorEnumeration.fromIterable(Collections2.filter(allTests(), new Predicate<Test>()
            {
                @Override
                public boolean apply(Test input)
                {
                    return !IgnoredTest.class.isInstance(input);
                }
            }));
        }

        @Override
        public void runTest(Test test, TestResult result)
        {
            if (hasAnnotation(test, SystemTenantOnly.class) &&
                    environmentData != null &&
                    environmentData.getTenant() != null &&
                    !environmentData.getTenant().equals("_jiraSystemTenant"))
            {
                if (test instanceof TestCase)
                {
                    FuncTestOut.log("Skipping " + getFullName((TestCase) test) +
                            " because it is marked SystemTenantOnly and this is not a system tenant");
                }
            }
            // JUnit annoyingly uses instance variable test collection, which forces us to override even more methods!
            else if (!IgnoredTest.class.isInstance(test))
            {
                super.runTest(test, result);
            }
        }

        List<Test> allTests()
        {
            return CollectionUtil.toList(super.tests());
        }

        List<IgnoredTest> ignoredTests()
        {
            final List<IgnoredTest> answer = new ArrayList<IgnoredTest>();
            for (Test test : allTests())
            {
                if (test instanceof IgnoredTest)
                {
                    answer.add((IgnoredTest) test);
                }
                else if (test instanceof EnvironmentTestSuite)
                {
                    answer.addAll(((EnvironmentTestSuite) test).ignoredTests());
                }
            }
            return answer;
        }

        private boolean isIgnored(Test test)
        {
            return hasAnnotation(test, Ignore.class);
        }

        private boolean hasAnnotation(Test test, Class<? extends Annotation> annotation)
        {
            if (!TestCase.class.isInstance(test))
            {
                return false;
            }
            else
            {
                Class<?> testClass = test.getClass();
                Method testMethod = getMethod((TestCase) test);

                // Ascend the inheritance tree
                while (testClass != null)
                {
                    if (testClass.isAnnotationPresent(annotation))
                    {
                        return true;
                    }
                    testClass = testClass.getSuperclass();
                }
                return testMethod != null && testMethod.isAnnotationPresent(annotation);
            }
        }

        private <A extends Annotation> A getAnnotation(Test test, Class<A> annotationClass)
        {
            Class<?> testClass = test.getClass();
            while (testClass != null)
            {
                if (testClass.isAnnotationPresent(annotationClass))
                {
                    return testClass.getAnnotation(annotationClass);
                }
                testClass = testClass.getSuperclass();
            }
            return null;
        }

        private String extractIgnoreReason(Test test)
        {
            final TestCase testCase = (TestCase) test;
            return getIgnoreAnnotation(testCase).value();
        }

        private Ignore getIgnoreAnnotation(TestCase testCase)
        {
            Ignore answer = getAnnotation(testCase, Ignore.class);
            if (answer == null)
            {
                answer = getMethod(testCase).getAnnotation(Ignore.class);
            }
            if (answer == null)
            {
                throw new IllegalArgumentException("Test case <" + testCase + "> was supposed to be annotated");
            }
            return answer;
        }

        private Method getMethod(TestCase test)
        {
            return new TestCaseMethodNameDetector(test).resolve();
        }
    }

    public class ParallelEnvironmentTestSuite extends EnvironmentTestSuite
    {
        private JIRAEnvironmentData environmentData;

        @Override
        public void setEnvironmentData(JIRAEnvironmentData environmentData)
        {
            this.environmentData = environmentData;
            int tenantNo = 0;
            for (final Enumeration<?> enumeration = tests(); enumeration.hasMoreElements();)
            {
                Object testObj = enumeration.nextElement();
                if (testObj instanceof EnvironmentAware)
                {
                    tenantNo++;
                    ((EnvironmentAware) testObj).setEnvironmentData(
                            new TenantOverridingEnvironmentData("tenant" + tenantNo, environmentData));
                }
            }
        }

        @Override
        public void run(final TestResult result)
        {
            // First must run a test to ensure the JIRA system tenant is set up
            EnvironmentTestSuite setupTest = new EnvironmentTestSuite(TestDefaultJiraDataFromInstall.class);
            setupTest.setEnvironmentData(environmentData);
            runTest(setupTest, result);
            // Now run all the tests in parallel
            ExecutorService testExecutor = Executors.newFixedThreadPool(testCount());
            try
            {
                Collection<Future> futures = new ArrayList<Future>();
                for (final Test test : allTests())
                {
                    futures.add(testExecutor.submit(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            runTest(test, result);
                        }
                    }));
                }

                for (Future future : futures)
                {
                    future.get();
                }
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            catch (ExecutionException e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                testExecutor.shutdownNow();
            }
        }
    }

    private static class IgnoredTest implements Test
    {
        final Test wrapped;
        final String reason;

        public IgnoredTest(Test wrapped, String reason)
        {
            this.wrapped = wrapped;
            this.reason = reason;
        }

        @Override
        public int countTestCases()
        {
            return 0;
        }

        @Override
        public void run(TestResult result)
        {
            result.addError(wrapped, new IllegalStateException("Test deemed ignored (reason: " + reason
                    + ") attempted to be run <" + wrapped + ">"));
        }
    }

    private static class SubClassPredicate implements Predicate<WebTestDescription>
    {
        private final Collection<Class<? extends Test>> classes;

        public SubClassPredicate(Collection<Class<? extends Test>> classes)
        {
            this.classes = classes;
        }

        @Override
        public boolean apply(WebTestDescription input)
        {
            for (Class<? extends Test> k : classes)
            {
                if (k.isAssignableFrom(input.testClass()))
                {
                    return true;
                }
            }
            return false;
        }
    }

}
