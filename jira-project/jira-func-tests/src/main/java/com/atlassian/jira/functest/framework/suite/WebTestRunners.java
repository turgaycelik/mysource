package com.atlassian.jira.functest.framework.suite;

import com.google.common.collect.Lists;
import org.hamcrest.StringDescription;
import org.junit.runner.Runner;
import org.junit.runners.ParentRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * Factory of web test runners. Web test runners wrap any parent runner with a transforming runner that applies
 * filtering and sorting by web test category (for given web test suite instance) of the UNDERLYING tests.
 *
 * @since v4.4
 */
public final class WebTestRunners
{
    private WebTestRunners()
    {
        throw new AssertionError("Don't instantiate me");
    }


    public static <T> ParentRunner<T> newRunner(WebTestSuite suite, ParentRunner<T> runner) throws InitializationError
    {
        return new TransformingParentRunner<T>(nameOf(suite), runner, Lists.<SuiteTransform>newArrayList(
                Transforms.fromFilter(new CategoryFilter(suite.includes(), suite.excludes())),
                SortByCategory.INSTANCE
        ));
    }

    public static ParentRunner<Runner> newRunner(WebTestSuite suite, RunnerBuilder runnerBuilder, Class<?>... classes)
            throws InitializationError
    {
        return newRunner(suite, new SuiteExt(runnerBuilder, classes));
    }

    private static final class SuiteExt extends Suite
    {
        SuiteExt(RunnerBuilder builder, Class<?>[] classes) throws InitializationError
        {
            super(builder, classes);
        }
    }


    private static String nameOf(WebTestSuite suite)
    {
        return new StringDescription().appendText(suite.getClass().getName()).appendText("\n")
                .appendText("package=").appendText(suite.webTestPackage()).appendText("\n")
                .appendText("includes=").appendValue(suite.includes()).appendText("\n")
                .appendText("excludes=").appendValue(suite.excludes()).toString();
    }
}
