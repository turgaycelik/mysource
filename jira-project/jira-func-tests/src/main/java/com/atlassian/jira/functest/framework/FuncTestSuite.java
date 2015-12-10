package com.atlassian.jira.functest.framework;

import com.atlassian.jira.testkit.client.log.FuncTestOut;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import com.atlassian.jira.webtests.util.TestClassUtils;
import com.atlassian.jira.webtests.ztests.admin.TestGlobalPermissions;
import com.atlassian.jira.webtests.ztests.admin.TestGroupBrowser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A base class for JIRA functional test suites.  It handles TestCase classes and ensures that a test instance is never
 * added more than once.  It also keeps track of the various JIRA editions and what test should run within them.
 * <p/>
 * Under the covers it uses LinkedHashSets which means the test are maintained in "insertion order" but not
 * test is returned more than once.
 *
 * @since v4.0
 */

//This must extend Test so that the maven2 test runner will call the suite method. This is stupid.
public class FuncTestSuite implements Test
{
    private final Set<Class<? extends TestCase>> funcTests = new LinkedHashSet<Class<? extends TestCase>>();
    private final Set<Class<? extends TestCase>> bundledPlugins2Tests = new LinkedHashSet<Class<? extends TestCase>>();
    private final Set<Class<? extends TestCase>> tpmLdapTests = new LinkedHashSet<Class<? extends TestCase>>();
    private final Set<Class<? extends TestCase>> singleRunTests = new LinkedHashSet<Class<? extends TestCase>>();

    /**
     * This gets the suite of Tests based on the func test settings that you currently have.
     *
     * @return a Test suite of classes to run
     */
    public Test createTest()
    {
        return createTest(new LocalTestEnvironmentData());
    }

    /**
     * This gets the suite of Tests based on the passed environment.
     *
     * @param environment the environment to used to select the tests.
     *
     * @return a Test suite of classes to run
     */
    public Test createTest(JIRAEnvironmentData environment)
    {
        if (isBlameMode())
        {
            return getJudgeJudyTests();
        }

        final Set<Class<? extends TestCase>> tests = getTests(environment);

        TestSuiteBuilder builder = createFuncTestBuilder();
        builder.addTests(tests);

        return builder.build();
    }

    /**
     * Returns a set of test classes that are deemed as test that should run on based on the JIRA edition inside the
     * JIRAEnvironmentData object.
     *
     * @param environmentData the edition to use is in here
     *
     * @return the of test classes that are deemed as test that should run on based on the JIRA edition, in insertion
     *         order
     */
    public Set<Class<? extends TestCase>> getTests(JIRAEnvironmentData environmentData)
    {
        Set<Class<? extends TestCase>> tests;
        if (environmentData.isSingleNamedTest()) {
            // ignore configuration and just add a specifically named test
            tests = new LinkedHashSet<Class<? extends TestCase>>();
            tests.add(environmentData.getSingleTestClass());
        }
        else if (environmentData.isAllTests())
        {
            tests = new LinkedHashSet<Class<? extends TestCase>>();
            tests.addAll(getFuncTests());
            tests.addAll(getBundledPlugins2Tests());
        }
        else if (environmentData.isBundledPluginsOnly())
        {
            tests = getBundledPlugins2Tests();
        }
        else if (environmentData.isTpmLdapTests())
        {
            tests = getTpmLdapTests();
        }
        else
        {
            tests = getFuncTests();
        }
        return tests;
    }

    private boolean isBlameMode()
    {
        String mode = System.getProperty("atlassian.test.suite.mode");
        FuncTestOut.out.println("MODE!! atlassian.test.suite.mode = " + mode);
        if ("blame".equals(System.getProperty("atlassian.test.suite.mode")))
            return true;
        mode = System.getProperty("atlassian.test.suite.numbatches");
        FuncTestOut.out.println("MODE!! atlassian.test.suite.numbatches = " + mode);
        return "99".equals(System.getProperty("atlassian.test.suite.numbatches"));
    }

    private Test getJudgeJudyTests()
    {
        TestSuiteBuilder testSuiteBuilder = new TestSuiteBuilder().log(true);
        testSuiteBuilder.addSingleTestMethod(TestGlobalPermissions.class, "testCanRemoveAnyoneFromJiraUsers");
        testSuiteBuilder.addSingleTestMethod(TestGroupBrowser.class, "testGroupPagingWorks");

        return testSuiteBuilder.build();
    }

    protected TestSuiteBuilder createFuncTestBuilder()
    {
        final String b = System.getProperty("atlassian.test.suite.numbatches");
        final String n = System.getProperty("atlassian.test.suite.batch");
        final boolean parallel = Boolean.getBoolean("atlassian.test.suite.parallel");
        if (parallel && b != null)
        {
            int numBatches = Integer.parseInt(b);
            return new TestSuiteBuilder().maxBatch(numBatches).parallel(true).log(true);
        }
        if (b != null && n != null)
        {
            String batchInfo = "Batch " + n + " of " + b;
            try
            {
                int numBatches = Integer.parseInt(b);
                int batch = Integer.parseInt(n);
                if (batch > 0 && batch <= numBatches)
                {
                    FuncTestOut.out.println(batchInfo);
                    return new TestSuiteBuilder(batch, numBatches).log(true);
                }
                else
                {
                    FuncTestOut.out.println("Batch mode FAIL. Batch information looks wrong-arse: " + batchInfo);
                }
            }
            catch (NumberFormatException e)
            {
                FuncTestOut.err.println("Batch mode FAIL. Batch information cannot be properly interpreted: " + batchInfo);
                e.printStackTrace(FuncTestOut.err);
                // will fall back to unbatched mode
            }
        }
        return new TestSuiteBuilder().log(true);
    }

    /**
     * @return the set of test classes that are deemed as test that should run on JIRA ENTERPRISE edition, in insertion
     *         order.
     */
    public Set<Class<? extends TestCase>> getFuncTests()
    {
        return new LinkedHashSet<Class<? extends TestCase>>(funcTests);
    }

    /**
     * @return the set of test classes that are deemed as test that should run for bundled plugins in insertion order.
     */
    public Set<Class<? extends TestCase>> getBundledPlugins2Tests()
    {
        return new LinkedHashSet<Class<? extends TestCase>>(bundledPlugins2Tests);
    }

    public Set<Class<? extends TestCase>> getTpmLdapTests()
    {
        return new LinkedHashSet<Class<? extends TestCase>>(tpmLdapTests);
    }

    public Set<Class<? extends TestCase>> getSingleRunTests()
    {
        return new LinkedHashSet<Class<? extends TestCase>>(singleRunTests);
    }

    public Set<Class<? extends TestCase>> getAllTests()
    {
        return CollectionBuilder.<Class<? extends TestCase>>newBuilder()
                .addAll(getFuncTests())
                .addAll(getBundledPlugins2Tests())
                .addAll(getTpmLdapTests())
                .addAll(getSingleRunTests())
                .asSet();
    }

    /**
     * This will add the test class into the suite as a bundled plugins 2.0 test only.
     *
     * @param testCaseClass the test case class to add
     *
     * @return <b>this</b> (in order to create a fluent style)
     */
    public FuncTestSuite addBundledPlugins2Only(Class<? extends TestCase> testCaseClass)
    {
        assertItsATest(testCaseClass);
        bundledPlugins2Tests.add(testCaseClass);
        return this;
    }

    public FuncTestSuite addTpmLdapOnly(Class<? extends TestCase> testCaseClass)
    {
        assertItsATest(testCaseClass);
        tpmLdapTests.add(testCaseClass);
        return this;
    }

    public FuncTestSuite addSingleRunTest(Class<? extends TestCase> testCaseClass)
    {
        assertItsATest(testCaseClass);
        singleRunTests.add(testCaseClass);
        return this;
    }

    /**
     * This will add the test class into the suite.
     *
     * @param testCaseClass the test case class to add
     *
     * @return <b>this</b> (in order to create a fluent style)
     */
    public FuncTestSuite addTest(Class<? extends TestCase> testCaseClass)
    {
        assertItsATest(testCaseClass);
        funcTests.add(testCaseClass);
        return this;
    }

    /**
     * This will add a collection of test classes into the suite as a STANDARD, PROFESSIONAL and ENTERPRISE tests.
     *
     * @param testCaseClasses a collection of test case classes
     *
     * @return <b>this</b> (in order to create a fluent style)
     */
    public FuncTestSuite addTests(final Collection<Class<? extends TestCase>> testCaseClasses)
    {
        for (final Class<? extends TestCase> testCaseClass : testCaseClasses)
        {
            addTest(testCaseClass);
        }
        return this;
    }

    public FuncTestSuite addTestsInPackage(final String packageName, boolean recursive)
    {
        final List<Class<? extends TestCase>> cases = getTestClasses(packageName, recursive);
        addTests(cases);

        return this;
    }

    public FuncTestSuite addTestsInPackageBundledPluginsOnly(final String packageName, boolean recursive)
    {
        final List<Class<? extends TestCase>> cases = getTestClasses(packageName, recursive);
        for (Class<? extends TestCase> aCase : cases)
        {
            addBundledPlugins2Only(aCase);
        }

        return this;
    }

    /**
     * This will add the tests from the passed in FuncTestSuite to this suite, respecting the various editions and
     * ensuring that tests are not added twice.
     *
     * @param funcTestSuite the FuncTestSuite to copy test classes from
     *
     * @return <b>this</b> (in order to create a fluent style)
     */
    public FuncTestSuite addTestSuite(FuncTestSuite funcTestSuite)
    {
        Set<Class<? extends TestCase>> tests;

        tests = funcTestSuite.getFuncTests();
        for (final Class<? extends TestCase> testCaseClass : tests)
        {
            addTest(testCaseClass);
        }

        tests = funcTestSuite.getBundledPlugins2Tests();
        for (final Class<? extends TestCase> testCaseClass : tests)
        {
            addBundledPlugins2Only(testCaseClass);
        }

        tests = funcTestSuite.getTpmLdapTests();
        for (final Class<? extends TestCase> testCaseClass : tests)
        {
            addTpmLdapOnly(testCaseClass);
        }

        tests = funcTestSuite.getSingleRunTests();
        for (final Class<? extends TestCase> testCaseClass : tests)
        {
            addSingleRunTest(testCaseClass);
        }
        return this;
    }

    private void assertItsATest(final Class testCaseClass)
    {
        if (!Test.class.isAssignableFrom(testCaseClass))
        {
            throw new IllegalArgumentException("The class must be an instanceof of junit.framework.Test to be added - " + testCaseClass);
        }
    }

    public int countTestCases()
    {
        return createTest().countTestCases();
    }

    public void run(final TestResult result)
    {
        createTest().run(result);
    }

    /**
     * Returns all test classes in a package, excluding any that are marked with @Ignore.
     *
     * @param packageName a String containing a package name
     * @param recursive a boolean indicating whether to search recursively
     * @return a List containing all test classes  in a package, sorted by name
     */
    private List<Class<? extends TestCase>> getTestClasses(String packageName, boolean recursive)
    {
        List<Class<? extends TestCase>> result = TestClassUtils.getJUni3TestClasses(packageName, recursive);
        // Add the tests in a predictable order, just their name order.
        Collections.sort(result, new Comparator<Class<? extends TestCase>>()
        {
            public int compare(final Class<? extends TestCase> o1, final Class<? extends TestCase> o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return result;
    }
}
