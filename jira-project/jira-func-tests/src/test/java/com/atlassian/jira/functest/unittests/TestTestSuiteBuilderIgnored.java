package com.atlassian.jira.functest.unittests;

import com.atlassian.jira.functest.config.FuncSuiteAssertions;
import com.atlassian.jira.functest.framework.TestSuiteBuilder;
import com.atlassian.jira.webtests.util.EnvironmentAware;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test case for {@link com.atlassian.jira.functest.framework.TestSuiteBuilder} handling of @Ignore annotation. 
 *
 * @since v4.3
 */
public class TestTestSuiteBuilderIgnored
{

    @Test
    public void shouldNotAddIgnoredClassToTheSuiteInBatchMode()
    {
        TestSuiteBuilder tested = new TestSuiteBuilder(1,10).log(true);
        junit.framework.Test result = tested.addTests(IgnoredTest.class, TestWithIgnoredMethods.class).build();
        FuncSuiteAssertions.assertNumberOfTestCasesEquals(2, result);
        FuncSuiteAssertions.assertHasTests(result, TestWithIgnoredMethods.class);
        FuncSuiteAssertions.assertDoesNotHaveTests(result, IgnoredTest.class);
        FuncSuiteAssertions.assertHasTestNames(result, "test1", "test2");
        FuncSuiteAssertions.assertDoesNotHaveTestNames(result, "test3", "test4");
    }


    @Test
    public void shouldNotAddIgnoredClassToTheSuiteInNonBatchMode()
    {
        TestSuiteBuilder tested = new TestSuiteBuilder().log(true);
        junit.framework.Test result = tested.addTests(IgnoredTest.class, TestWithIgnoredMethods.class).build();
        FuncSuiteAssertions.assertNumberOfTestCasesEquals(2, result);
        FuncSuiteAssertions.assertHasTests(result, TestWithIgnoredMethods.class);
        FuncSuiteAssertions.assertDoesNotHaveTests(result, IgnoredTest.class);
        FuncSuiteAssertions.assertHasTestNames(result, "test1", "test2");
        FuncSuiteAssertions.assertDoesNotHaveTestNames(result, "test3", "test4");
    }

    @Test
    public void shouldDetectMethodNameFromModifiedTestName()
    {
        TestSuiteBuilder tested = new TestSuiteBuilder().log(true);
        junit.framework.Test result = tested.addTests(TestWithIgnoredMethodsAndOverridenGetName.class).build();
        FuncSuiteAssertions.assertNumberOfTestCasesEquals(2, result);
        FuncSuiteAssertions.assertHasTests(result, TestWithIgnoredMethodsAndOverridenGetName.class);
        FuncSuiteAssertions.assertHasTestNames(result,
                "com.atlassian.jira.functest.unittests.TestTestSuiteBuilderIgnored$TestWithIgnoredMethodsAndOverridenGetName.test1",
                "com.atlassian.jira.functest.unittests.TestTestSuiteBuilderIgnored$TestWithIgnoredMethodsAndOverridenGetName.test2");
        FuncSuiteAssertions.assertDoesNotHaveTestNames(result,
                "test3",
                "test4",
                "com.atlassian.jira.functest.unittests.TestTestSuiteBuilderIgnored$TestWithIgnoredMethodsAndOverridenGetName.test3",
                "com.atlassian.jira.functest.unittests.TestTestSuiteBuilderIgnored$TestWithIgnoredMethodsAndOverridenGetName.test4");
    }

    @Test
    public void shouldDetectMethodNameFromArbitraryName()
    {
        TestSuiteBuilder tested = new TestSuiteBuilder().log(true);
        junit.framework.Test result = tested.addTests(TestWithIgnoredMethodsAndArbitraryTestName.class).build();
        FuncSuiteAssertions.assertNumberOfTestCasesEquals(2, result);
        FuncSuiteAssertions.assertHasTests(result, TestWithIgnoredMethodsAndArbitraryTestName.class);
        FuncSuiteAssertions.assertHasTestNames(result, "Something really silly");
        FuncSuiteAssertions.assertDoesNotHaveTestNames(result,
                "test3",
                "test4",
                "com.atlassian.jira.functest.unittests.TestTestSuiteBuilderIgnored$TestWithIgnoredMethodsAndOverridenGetName.test3",
                "com.atlassian.jira.functest.unittests.TestTestSuiteBuilderIgnored$TestWithIgnoredMethodsAndOverridenGetName.test4");
    }


    @Test
    public void shouldRunNonIgnoredOnly()
    {
        TestResult testResult = new TestResult();
        new TestSuiteBuilder().addTests(IgnoredTest.class, TestWithIgnoredMethods.class).build().run(testResult);
        assertEquals(2, testResult.runCount());
    }

    @Test
    public void shouldCountNonIgnoredTestCasesOnly()
    {
        junit.framework.Test suite = new TestSuiteBuilder().addTests(IgnoredTest.class, TestWithIgnoredMethods.class).build();
        assertEquals(2, suite.countTestCases());
    }

    @Ignore("just because")
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

    public static class TestWithIgnoredMethods extends TestCase implements EnvironmentAware
    {
        public void test1()
        {
        }

        public void test2()
        {
        }

        @Ignore
        public void test3()
        {
        }

        @Ignore("cause I don't like 4s")
        public void test4()
        {
        }

        @Override
        public void setEnvironmentData(JIRAEnvironmentData environmentData)
        {
        }
    }

    public static class TestWithIgnoredMethodsAndOverridenGetName extends TestWithIgnoredMethods
    {
        @Override
        public String getName()
        {
            return getClass().getName() + "." + super.getName();
        }
    }

    public static class TestWithIgnoredMethodsAndArbitraryTestName extends TestWithIgnoredMethods
    {
        @Override
        public String getName()
        {
            return "Something really silly";
        }
    }
}
