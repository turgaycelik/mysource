package com.atlassian.jira.functest.config;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.util.TestClassUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import org.apache.log4j.Logger;
import org.junit.Ignore;

import junit.framework.Assert;
import junit.framework.TestCase;

import static com.google.common.collect.Collections2.filter;

/**
 * Finds missing tests from a defined suite.
 * 
 * @since v4.2
 */
public class MissingTestFinder
{
    private static final Logger LOGGER = Logger.getLogger(MissingTestFinder.class);

    public void assertAllTestsInTestHarness(final String packageForAllTests, final String harnessSuiteName, final Set<Class<? extends TestCase>> testsDefinedInSuite, Set<Class<? extends TestCase>> testsToIgnore)
    {
        assertAllTestsInTestHarness(TestClassUtils.getJUnit3TestClasses(packageForAllTests), harnessSuiteName, testsDefinedInSuite, testsToIgnore);
    }

    public void assertAllTestsInTestHarness(final List<Class<? extends TestCase>> allTestCases, final String harnessSuiteName, final Set<Class<? extends TestCase>> testsDefinedInSuite, final Set<Class<? extends TestCase>> testsToIgnore)
    {
        // filter out the classes that we need to ignore.
        Collection<Class<? extends TestCase>> allTestsExceptIgnored = filter(allTestCases, new Predicate<Class<? extends TestCase>>()
        {
            public boolean apply(Class<? extends TestCase> testClass)
            {
                return testClass.getSimpleName().startsWith("Test") && !testsToIgnore.contains(testClass);
            }
        });

        Sets.SetView<Class<? extends TestCase>> ignoredButRunning = Sets.intersection(Sets.newHashSet(testsDefinedInSuite), testsToIgnore);
        for (Class<? extends TestCase> testClass : ignoredButRunning)
        {
            LOGGER.error(String.format("Test %s is marked with @%s but will be run.", testClass.getName(), Ignore.class.getSimpleName()));
        }

        // find all missing tests by calculating set difference
        Sets.SetView<Class<? extends TestCase>> missingTestClasses = Sets.difference(Sets.newHashSet(allTestsExceptIgnored), Sets.newHashSet(testsDefinedInSuite));
        if (!missingTestClasses.isEmpty())
        {
            StringBuilder missingTests = new StringBuilder();
            for (Class<? extends TestCase> testClass : missingTestClasses)
            {
                missingTests.append("\n  ").append(testClass.getName());
            }

            Assert.fail("Found " + missingTestClasses.size() + " Tests that are unknown to " + harnessSuiteName + ":" + missingTests.toString() + "\n. See subclasses of " + FuncTestSuite.class.getSimpleName() + " for details.\n");
        }
    }

}
