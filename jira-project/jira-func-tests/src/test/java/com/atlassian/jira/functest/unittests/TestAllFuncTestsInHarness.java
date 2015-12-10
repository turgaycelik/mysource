package com.atlassian.jira.functest.unittests;

import java.util.List;
import java.util.Set;

import com.atlassian.jira.functest.config.MissingTestFinder;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.AcceptanceTestHarness;
import com.atlassian.jira.webtests.util.TestClassUtils;
import com.atlassian.jira.webtests.ztests.setup.TestSetupPreinstalledBundleServiceDesk;
import com.atlassian.jira.webtests.ztests.setup.TestSetupPreinstalledBundlesAgile;
import com.atlassian.jira.webtests.ztests.upgrade.TestUpgradeXmlData;

import com.google.common.collect.Sets;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * Finds any Func Tests that are missing from our AcceptanceTestHarness.
 *
 * @since v3.13
 */
public class TestAllFuncTestsInHarness
{
    private static final Logger LOGGER = Logger.getLogger(TestAllFuncTestsInHarness.class);

    private final MissingTestFinder missingTestFinder = new MissingTestFinder();

    @Test
    public void testFindTestsMissingFromTestHarness() throws Exception
    {
        final Set<Class<? extends TestCase>> testsDefinedInSuite = AcceptanceTestHarness.SUITE.getAllTests();
        List<Class<? extends TestCase>> allFuncTests = TestClassUtils.getAllFuncTests();
        Set<Class<? extends TestCase>> testsToIgnore = getIgnoredTests(allFuncTests);
        missingTestFinder.assertAllTestsInTestHarness(allFuncTests, "AcceptanceTestHarness", testsDefinedInSuite, testsToIgnore);
    }

    private Set<Class<? extends TestCase>> getIgnoredTests(List<Class<? extends TestCase>> allFuncTests)
    {
        Set<Class<? extends TestCase>> ignored = Sets.newHashSet();
        for (Class<? extends TestCase> testCase : allFuncTests)
        {
            Ignore ignore = testCase.getAnnotation(Ignore.class);
            boolean isTestIgnored = ignore!=null;
            final Set<Category> testCategories = Category.fromAnnotation(testCase.getAnnotation(WebTest.class));
            final boolean needsPristineJira = testCategories.contains(Category.SETUP_PRISTINE);

            if (isTestIgnored || needsPristineJira)
            {
                if (isTestIgnored)
                {
                    LOGGER.warn(String.format("IGNORED %s (%s)", testCase.getName(), ignore.value()));
                }
                else {
                    LOGGER.warn(String.format("IGNORED %s", testCase.getName()));

                }
                ignored.add(testCase);
            }
        }
        // temporary - we don't want the plugins tests to be in a main suite, they are run using categories
        ignored.addAll(TestClassUtils.getJUni3TestClasses("com.atlassian.jira.webtests.ztests.plugin.reloadable", true));
        ignored.add(TestUpgradeXmlData.class);

        // add setup tests to ignored by acceptance harness - they need pristine JIRA and need to be run separately
        ignored.add(TestSetupPreinstalledBundlesAgile.class);
        ignored.add(TestSetupPreinstalledBundleServiceDesk.class);

        return ignored;
    }
}
