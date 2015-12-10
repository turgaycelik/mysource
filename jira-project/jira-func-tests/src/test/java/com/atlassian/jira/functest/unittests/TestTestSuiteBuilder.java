package com.atlassian.jira.functest.unittests;

import com.atlassian.jira.functest.framework.Splitable;
import com.atlassian.jira.functest.framework.SuiteListenerWrapper;
import com.atlassian.jira.functest.framework.TestSuiteBuilder;
import com.atlassian.jira.webtests.AcceptanceTestHarness;
import com.atlassian.jira.webtests.util.EnvironmentAware;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Ignore;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Test for {@link com.atlassian.jira.functest.framework.TestSuiteBuilder}.
 */
@SuppressWarnings("unchecked")
public class TestTestSuiteBuilder extends TestCase
{
    @Override
    protected void tearDown() throws Exception
    {
        TestEnvAware.callCount = 0;
    }

    public void testConstr() throws Exception
    {
        try
        {
            new TestSuiteBuilder(0, 5);
            fail("Should now allow zero to be set for batch number.");
        }
        catch (IllegalArgumentException e)
        {
            //expected and good.
        }

        try
        {
            new TestSuiteBuilder(2, 0);
            fail("Should now allow zero to be set for max batch number.");
        }
        catch (IllegalArgumentException e)
        {
            //expected and good.
        }
    }

    public void testEmpty()
    {
        TestSuiteBuilder builder = new TestSuiteBuilder();
        Test test = builder.build();
        assertEquals(0, test.countTestCases());

        builder = new TestSuiteBuilder(1, 5);
        test = builder.build();
        assertEquals(0, test.countTestCases());
    }

    public void testNoBatch() throws Exception
    {
        TestSuiteBuilder builder = new TestSuiteBuilder();
        builder.addTests(Collections.<Class<? extends TestCase>>singleton(TestFive.class));
        assertTestSuite(builder.build(), TestFive.class);
        assertTestSuite(builder.build(), TestFive.class);

        builder.addTest(TestFive.class);
        builder.addTest(IgnoredTest.class);
        builder.addTest(TestOne.class);
        assertTestSuite(builder.build(), TestFive.class, TestOne.class);
    }

    public void testSingleTestBigBatch()
    {
        TestSuiteBuilder builder = new TestSuiteBuilder(1, 7);
        builder.addTests(Collections.<Class<? extends TestCase>>singleton(TestFive.class));
        assertTestSuite(builder.build(), TestFive.class);
        assertTestSuite(builder.batch(2).build());
    }

    public void testSingleTestSmallBatch()
    {
        TestSuiteBuilder builder = new TestSuiteBuilder(1, 2);
        builder.addTests(Collections.<Class<? extends TestCase>>singleton(TestFive.class));
        assertTestSuite(builder.build(), TestFive.class);
        assertTestSuite(builder.batch(2).build());
    }

    public void testMultipleTestNoComposite()
    {
        TestSuiteBuilder builder = new TestSuiteBuilder(1, 3);
        builder.addTests(TestTwo.class, TestThree.class, TestOne.class);
        assertTestSuite(builder.build(), TestTwo.class);
        assertTestSuite(builder.batch(2).build(), TestThree.class);
        assertTestSuite(builder.batch(3).build(), TestOne.class);

        builder = new TestSuiteBuilder(1, 3);
        builder.addTests(TestFive.class, TestTwo.class, IgnoredTest.class, TestOne.class);
        assertTestSuite(builder.batch(1).build(), TestFive.class);
        assertTestSuite(builder.batch(2).build(), TestTwo.class);
        assertTestSuite(builder.batch(3).build(), TestOne.class);
    }

    public void testMultipleTestsWithCompositeExact() throws Exception
    {
        TestSuiteBuilder builder = new TestSuiteBuilder(1, 2);
        builder.addTests(TestTwo.class, TestThree.class, TestFive.class);
        assertTestSuite(builder.build(), TestTwo.class, TestThree.class);
        assertTestSuite(builder.batch(2).build(), TestFive.class);
    }

    public void testMultipleTestsWithCompositeWithEmptyBatch() throws Exception
    {
        TestSuiteBuilder builder = new TestSuiteBuilder(1, 3);
        builder.addTests(TestFive.class, TestOne.class);
        assertTestSuite(builder.build(), TestFive.class);
        assertTestSuite(builder.batch(2).build(), TestOne.class);
        assertTestSuite(builder.batch(3).build());

        builder.addTest(TestTwo.class);
        assertTestSuite(builder.batch(1).build(), TestFive.class);
        assertTestSuite(builder.batch(2).build(), TestOne.class, TestTwo.class);
        assertTestSuite(builder.batch(3).build());
    }

    public void testMultipleTestWithComposite() throws Exception
    {
        TestSuiteBuilder builder = new TestSuiteBuilder(1, 5);
        builder.addTests(TestFive.class, TestOne.class, TestFour.class, TestTwo.class, TestThree.class);
        assertTestSuite(builder.build(), TestFive.class);
        assertTestSuite(builder.batch(2).build(), TestOne.class, TestFour.class);
        assertTestSuite(builder.batch(3).build(), TestTwo.class);
        assertTestSuite(builder.batch(4).build(), TestThree.class);
        assertTestSuite(builder.batch(5).build());
    }

    public void testSingleSplitNoBatches() throws Exception
    {
        TestSuiteBuilder builder = new TestSuiteBuilder();
        builder.addTest(TestFiveSplit.class);

        assertTestSuite(builder.build(), TestFiveSplit.class);
    }

    public void testSingleSplitBatches() throws Exception
    {
        TestSuiteBuilder builder = new TestSuiteBuilder();
        builder.addTest(TestFiveSplit.class);

        assertTestSuite(wrapInSuite(splitTest(TestFiveSplit.class, "test1", "test2")), builder.maxBatch(3).batch(1).build());
        assertTestSuite(wrapInSuite(splitTest(TestFiveSplit.class, "test3", "test4")), builder.batch(2).build());
        assertTestSuite(wrapInSuite(splitTest(TestFiveSplit.class, "test5")), builder.batch(3).build());
    }

    public void testSingleSplitWithEmptyBatches() throws Exception
    {
        TestSuiteBuilder builder = new TestSuiteBuilder();
        builder.addTest(TestFiveSplit.class);

        assertTestSuite(wrapInSuite(splitTest(TestFiveSplit.class, "test1")), builder.maxBatch(10).batch(1).build());
        assertTestSuite(wrapInSuite(splitTest(TestFiveSplit.class, "test2")), builder.batch(2).build());
        assertTestSuite(wrapInSuite(splitTest(TestFiveSplit.class, "test3")), builder.batch(3).build());
        assertTestSuite(wrapInSuite(splitTest(TestFiveSplit.class, "test4")), builder.batch(4).build());
        assertTestSuite(wrapInSuite(splitTest(TestFiveSplit.class, "test5")), builder.batch(5).build());
        assertTestSuite(new TestSuite(), builder.batch(6).build());
        assertTestSuite(new TestSuite(), builder.batch(7).build());
        assertTestSuite(new TestSuite(), builder.batch(8).build());
        assertTestSuite(new TestSuite(), builder.batch(9).build());
        assertTestSuite(new TestSuite(), builder.batch(10).build());
    }

    //Non-environment aware classes should always be placed in the first batch. The assumption is that non-environment
    //aware classes are actually Unit Tests.
    public void testNonEnvAwareWithSplittable()
    {
        TestSuiteBuilder builder = new TestSuiteBuilder().maxBatch(2);
        builder.addTest(TestFiveSplit.class);
        builder.addTest(TestNonEnvAware.class);

        TestSuite expected = new TestSuite();
        expected.addTest(splitTest(TestFiveSplit.class, "test1", "test2", "test3"));

        assertTestSuite(expected, builder.batch(1).build());

        expected = new TestSuite();
        expected.addTest(splitTest(TestFiveSplit.class, "test4", "test5"));
        expected.addTestSuite(TestNonEnvAware.class);

        assertTestSuite(expected, builder.batch(2).build());
    }

    //Non-environment aware classes should always be placed in the first batch. The assumption is that non-environment
    //aware classes are actually Unit Tests.
    public void testNonEnvAwareWithoutSplit()
    {
        TestSuiteBuilder builder = new TestSuiteBuilder().maxBatch(2);
        builder.addTest(TestTwo.class);
        builder.addTest(IgnoredTest.class);
        builder.addTest(TestNonEnvAware.class);

        TestSuite expected = new TestSuite();
        expected.addTestSuite(TestTwo.class);

        assertTestSuite(expected, builder.batch(1).build());

        expected = new TestSuite();
        expected.addTestSuite(TestNonEnvAware.class);
        assertTestSuite(expected, builder.batch(2).build());
    }

    public void testSplitAndNonSplit() throws Exception
    {
        TestSuiteBuilder builder = new TestSuiteBuilder(1, 5);
        builder.addTests(TestFive.class, TestOne.class, TestFiveSplit.class, TestFour.class, TestTwo.class,
                IgnoredTest.class, TestThree.class, TestNonEnvAware.class);

        TestSuite expected = new TestSuite();
        expected.addTestSuite(TestFive.class);
        expected.addTest(splitTest(TestFiveSplit.class, "test1"));

        assertTestSuite(expected, builder.build());

        expected = new TestSuite();
        expected.addTestSuite(TestOne.class);
        expected.addTest(splitTest(TestFiveSplit.class, "test2"));
        expected.addTestSuite(TestFour.class);

        assertTestSuite(expected, builder.batch(2).build());

        expected = new TestSuite();
        expected.addTest(splitTest(TestFiveSplit.class, "test3"));
        expected.addTestSuite(TestTwo.class);

        assertTestSuite(expected, builder.batch(3).build());

        expected = new TestSuite();
        expected.addTest(splitTest(TestFiveSplit.class, "test4"));
        expected.addTestSuite(TestThree.class);

        assertTestSuite(expected, builder.batch(4).build());

        expected = new TestSuite();
        expected.addTest(splitTest(TestFiveSplit.class, "test5"));
        expected.addTestSuite(TestNonEnvAware.class);

        assertTestSuite(expected, builder.batch(5).build());
    }

    public void testEnvironmentAware() throws Exception
    {
        final JIRAEnvironmentData envData = (JIRAEnvironmentData)Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { JIRAEnvironmentData.class }, new InvocationHandler()
        {
            public Object invoke(final Object proxy, final Method method, final Object[] args)
            {
                throw new UnsupportedOperationException();
            }
        });

        TestSuiteBuilder builder = new TestSuiteBuilder();
        final Test test = builder.addTest(TestEnvAware.class).addTest(TestFive.class).build();

        assertTrue(test instanceof EnvironmentAware);
        ((EnvironmentAware)test).setEnvironmentData(envData);
        assertEquals(1, TestEnvAware.callCount);
        assertSame(envData, TestEnvAware.envData);
    }

    public void testBadState() throws Exception
    {
        TestSuiteBuilder builder = new TestSuiteBuilder(1, 5);
        builder.maxBatch(-1).batch(5);
        builder.addTest(TestOne.class);

        try
        {
            builder.build();
            fail("Should be in illegal state.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        builder.maxBatch(4);
        try
        {
            builder.build();
            fail("Should be in an illegal state.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }
    }

    public void testBadBatch() throws Exception
    {
        TestSuiteBuilder builder = new TestSuiteBuilder(1, 5);
        try
        {
            builder.batch(0);
            fail("Should now allow zero to be set for batch number.");
        }
        catch (IllegalArgumentException e)
        {
            //expected and good.
        }
    }

    public void testBadMaxBatch() throws Exception
    {
        TestSuiteBuilder builder = new TestSuiteBuilder(1, 5);
        try
        {
            builder.maxBatch(0);
            fail("Should now allow zero to be set for max batch number.");
        }
        catch (IllegalArgumentException e)
        {
            //expected and good.
        }
    }

    public void testAcceptanceTestHarness() throws Exception
    {
        Set<Class<? extends TestCase>> classes = AcceptanceTestHarness.SUITE.getFuncTests();
        TestSuiteBuilder splitter = new TestSuiteBuilder().maxBatch(15).addTests(classes);

        int total = 0;
        for (int i = 1; i <= 15; i++)
        {
            TestSuite t = unwrapSuite(splitter.batch(i).build());
            total += t.countTestCases();
        }

        TestSuite test = unwrapSuite(splitter.batch(-1).build());
        assertEquals(test.countTestCases(), total);
    }

    private TestSuite unwrapSuite(Test test)
    {
        while (test instanceof SuiteListenerWrapper)
        {
            test = ((SuiteListenerWrapper) test).delegate();
        }

        if (test instanceof TestSuite)
        {
            return (TestSuite) test;
        }
        else
        {
            throw new IllegalArgumentException("Test unwrapped to '" + test.getClass().getName() + "' rather than a TestCase.");
        }
    }

    private TestSuite wrapInSuite(Test test)
    {
        final TestSuite testSuite = new TestSuite();
        testSuite.addTest(test);
        return testSuite;
    }

    private TestSuite splitTest(Class<? extends TestCase> test, String...names)
    {
        final TestSuite suite = new TestSuite(test);

        final Map<String, Test> nameMap = new HashMap<String, Test>();
        for (Enumeration<Test> enumeration = suite.tests(); enumeration.hasMoreElements(); )
        {
            final Test innerTest = enumeration.nextElement();
            if (innerTest instanceof TestCase)
            {
                TestCase innerCase = (TestCase) innerTest;
                if (nameMap.put(innerCase.getName(), innerCase) != null)
                {
                    fail("Test contained two tests of the same name.");
                }
            }
        }

        final TestSuite returnSuite = new TestSuite(suite.getName());
        for (String name : names)
        {
            final Test innerTest = nameMap.get(name);
            assertNotNull(String.format("Could not find test for '%s' on class '%s'.", name, test.getName()), innerTest);
            returnSuite.addTest(innerTest);
        }

        return returnSuite;
    }


    private static TestSuite createTestSuite(final Class<? extends TestCase>... args)
    {
        TestSuite expected = new TestSuite();
        for (Class<? extends TestCase> arg : args)
        {
            expected.addTestSuite(arg);
        }
        return expected;
    }

    private void assertTestSuite(Test actual, Class<? extends TestCase>... args)
    {
        assertSuiteEquals(createTestSuite(args), unwrapSuite(actual));
    }

    private void assertTestSuite(TestSuite expected, Test actual)
    {
        assertSuiteEquals(expected,  unwrapSuite(actual));
    }

    private void assertSuiteEquals(TestSuite expected, TestSuite actual)
    {
        assertEquals("Tests did not have same number of test cases.", expected.countTestCases(), actual.countTestCases());

        for (Enumeration<Test> expectedEnum = expected.tests(); expectedEnum.hasMoreElements();)
        {
            final Test expectedTest = expectedEnum.nextElement();
            if (expectedTest instanceof TestSuite)
            {
                final TestSuite expectedSuite = (TestSuite) expectedTest;
                final TestSuite actualSuite = findSuite(expectedSuite, actual);
                if (actualSuite == null)
                {
                    fail("Actual suite did not contain TestSuite: " + expectedSuite.getName());
                }
                assertSuiteEquals(expectedSuite, actualSuite);
            }
            else if (expectedTest instanceof TestCase)
            {
                final TestCase expectedCase = (TestCase) expectedTest;

                if (!findTestCaseInSuite(expectedCase, actual))
                {
                    fail("Actual suite did not contain TestCase: " + expectedCase.getName());
                }
            }
            else
            {
                //Lets hope that the test implements the equals method.
                if (!findTest(expectedTest, actual))
                {
                    fail("Actual suite did not contain Test: " + expectedTest);
                }
            }
        }
    }

    private TestSuite findSuite(TestSuite findSuite, TestSuite suite)
    {
        for (Enumeration<Test> testEnum = suite.tests(); testEnum.hasMoreElements();)
        {
            final Test test = testEnum.nextElement();
            if (test instanceof TestSuite)
            {
                TestSuite testSuite = (TestSuite) test;
                if (testSuite.getName().equals(findSuite.getName()))
                {
                    return testSuite;
                }
            }
        }
        return null;
    }

    private boolean findTestCaseInSuite(TestCase findTest, TestSuite suite)
    {
        for (Enumeration<Test> expectedEnum = suite.tests(); expectedEnum.hasMoreElements();)
        {
            Test actualTest = expectedEnum.nextElement();
            if (actualTest.getClass() == findTest.getClass() && ((TestCase) actualTest).getName().equals(findTest.getName()))
            {
                return true;
            }
        }
        return false;
    }

    private boolean findTest(Test test, TestSuite suite)
    {
        for (Enumeration<Test> expectedEnum = suite.tests(); expectedEnum.hasMoreElements();)
        {
            Test expectedTest = expectedEnum.nextElement();
            if (expectedTest.equals(test))
            {
                return true;
            }
        }
        return false;
    }

    public static class TestOne extends TestCase implements EnvironmentAware
    {
        public void testOneOne()
        {
        }

        public void setEnvironmentData(final JIRAEnvironmentData environmentData)
        {
        }
    }

    public static class TestTwo extends TestCase implements EnvironmentAware
    {
        public void testTwoOne()
        {
        }

        public void testTwoTwo()
        {
        }

        public void setEnvironmentData(final JIRAEnvironmentData environmentData)
        {
        }
    }

    public static class TestThree extends TestCase implements EnvironmentAware
    {
        public void testThreeOne()
        {
        }

        public void testThreeTwo()
        {
        }

        public void testThreeThree()
        {
        }

        public void setEnvironmentData(final JIRAEnvironmentData environmentData)
        {
        }
    }

    public static class TestFour extends TestCase implements EnvironmentAware
    {
        public void testFourOne()
        {
        }

        public void testFourTwo()
        {
        }

        public void testFourThree()
        {
        }

        public void testFourFour()
        {
        }

        public void setEnvironmentData(final JIRAEnvironmentData environmentData)
        {
        }
    }

    public static class TestFive extends TestCase implements EnvironmentAware
    {
        public void testFiveOne()
        {
        }

        public void testFiveTwo()
        {
        }

        public void testFiveThree()
        {
        }

        public void testFiveFour()
        {
        }

        public void testFiveFive()
        {
        }

        public void setEnvironmentData(final JIRAEnvironmentData environmentData)
        {
        }
    }

    @Splitable
    public static class TestFiveSplit extends TestCase implements EnvironmentAware
    {
        public void test5()
        {
        }

        public void test1()
        {
        }

        public void test2()
        {
        }

        public void test3()
        {
        }

        public void test4()
        {
        }

        public void setEnvironmentData(final JIRAEnvironmentData environmentData)
        {
        }
    }

    public static class TestNonEnvAware extends TestCase
    {
        public void test1()
        {
        }

        public void test2()
        {
        }

        public void test3()
        {
        }

        public void test4()
        {
        }

        public void test5()
        {
        }

        public void test6()
        {
        }

        public void test7()
        {
        }
    }

    public static class TestEnvAware extends TestCase implements EnvironmentAware
    {
        private static int callCount = 0;
        private static JIRAEnvironmentData envData = null;

        public void testStuff() throws Exception
        {
        }

        public void setEnvironmentData(final JIRAEnvironmentData environmentData)
        {
            callCount++;
            envData = environmentData;
        }
    }


    @Ignore
    public static class IgnoredTest extends TestCase implements EnvironmentAware
    {
        public void test1()
        {
        }

        public void test2()
        {
        }

        public void test3()
        {
        }

        @Override
        public void setEnvironmentData(JIRAEnvironmentData environmentData)
        {
        }
    }
}
