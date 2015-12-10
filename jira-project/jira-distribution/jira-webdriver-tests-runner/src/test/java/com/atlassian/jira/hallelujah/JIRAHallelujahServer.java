package com.atlassian.jira.hallelujah;

import com.atlassian.buildeng.hallelujah.HallelujahServer;
import com.atlassian.buildeng.hallelujah.api.model.TestCaseName;
import com.atlassian.buildeng.hallelujah.core.JUnitUtils;
import com.atlassian.buildeng.hallelujah.jms.JMSConnectionFactory.DeliveryMode;
import com.atlassian.buildeng.hallelujah.jms.JMSHallelujahServer;
import com.atlassian.buildeng.hallelujah.listener.SlowTestsListener;
import com.atlassian.buildeng.hallelujah.listener.TestRetryingServerListener;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.CategoryFilter;
import com.atlassian.jira.functest.framework.suite.SystemPropertyBasedSuite;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.suite.WebTestRunners;
import com.atlassian.jira.functest.framework.suite.WebTestSuiteRunner;
import com.atlassian.jira.functest.framework.util.junit.DescriptionWalker;
import com.atlassian.jira.util.Consumer;
import javax.annotation.Nonnull;
import com.atlassian.webdriver.LifecycleAwareWebDriverGrid;
import com.google.common.base.Predicates;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;

import javax.jms.JMSException;
import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayList;

/**
 * The JIRA Hallelujah server for batching and running tests concurrently.
 */
public class JIRAHallelujahServer
{
    public static void main (String[] args) throws Exception
    {
        System.out.println("JIRA Hallelujah Server starting...");
        System.out.println(System.getProperties());

        final WebDriverSuiteRunner webDriverSuiteRunner = new WebDriverSuiteRunner(SystemPropertyBasedSuite.class);
        final List<Test> tests = webDriverSuiteRunner.getTests();
        final TestSuite testSuite = new TestSuite();
        for (final Test test : tests)
        {
            testSuite.addTest(test);
        }

        final String junitFilename = "TEST-Hallelujah.xml";
        final String suiteName = "WebDriverCargoTestHarness";

        HallelujahServer hallelujahServer;
        try
        {
            hallelujahServer = new JMSHallelujahServer.Builder()
                    .setJmsConfig(JIRAHallelujahConfig.getConfiguration())
                    .setSuite(testSuite)
                    .setTestResultFileName(junitFilename)
                    .setSuiteName(suiteName)
                    .setDeliveryMode(DeliveryMode.PERSISTENT)
                    .build()
                    .registerListeners(
                            new TestRetryingServerListener(1, new File("flakyTests.txt")),
                            new SlowTestsListener(20),
                            new FailSlowTestsListener(5, TimeUnit.MINUTES)
                    );
        }
        catch (JMSException e)
        {
            throw new RuntimeException(e);
        }

        boolean success = hallelujahServer.call();

        System.out.println("JIRA Hallelujah Server finished.");
        LifecycleAwareWebDriverGrid.getCurrentDriver().quit();

        if (!success)
        {
            System.exit(1);
        }
    }

    /**
     * Runs web driver test suites.
     */
    private static class WebDriverSuiteRunner extends WebTestSuiteRunner
    {
        private final CategoryFilter categoryFilter;

        public WebDriverSuiteRunner(Class<?> webTestSuiteClass) throws InitializationError
        {
            super(webTestSuiteClass);
            this.categoryFilter = new CategoryFilter(suite.includes(), suite.excludes());
        }

        @Override
        protected Runner delegateRunner()
        {
            try
            {
                return WebTestRunners.newRunner(suite,
                        new AllDefaultPossibilitiesBuilder(true),
                        testClasses.toArray(new Class<?>[testClasses.size()]));
            }
            catch (InitializationError initializationError)
            {
                throw new RuntimeException(initializationError);
            }
        }

        public List<Test> getTests()
        {
            logCategories("Filtering included categories:", suite.includes());
            logCategories("Filtering excluded categories:", suite.excludes());
            final List<Test> tests = createTestListFromDescriptions();
            return tests;
        }

        private List<Test> createTestListFromDescriptions()
        {
            final List<Test> tests = newArrayList();
            DescriptionWalker.walk(new Consumer<Description>() {
                @Override
                public void consume(@Nonnull Description description)
                {
                    if (description.getClassName() != null && description.getMethodName() != null
                            && description.getAnnotation(org.junit.Test.class) != null && categoryFilter.shouldRun(description))
                    {
                        final TestCaseName testCaseName = new TestCaseName(description.getClassName(), description.getMethodName());
                        Test test = JUnitUtils.testFromTestCaseName(testCaseName);
                        tests.add(test);
                    }
                }
            }, delegateRunner().getDescription());

            System.out.println("total test count:" + tests.size());
            return tests;
        }

        private void logCategories(String msg, Set<Category> categories)
        {
            System.out.println(msg);
            for (Category category : categories)
            {
                System.out.println(category.name());
            }
        }
    }
}
