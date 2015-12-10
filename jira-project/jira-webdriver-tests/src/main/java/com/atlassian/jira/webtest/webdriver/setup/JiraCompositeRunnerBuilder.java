package com.atlassian.jira.webtest.webdriver.setup;

import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.config.junit4.JiraWebTestRunner;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.internal.builders.AnnotatedBuilder;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.Runner;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
* Runner builder for JIRA, replaces default builders for annotated and JUnit4 case. This allows us to run suites
 * and use JIRA web test runner for classes known to be web tests.
*
* @since v4.4
*/
class JiraCompositeRunnerBuilder extends AllDefaultPossibilitiesBuilder
{
    private final JiraTestedProduct jiraProduct;

    public JiraCompositeRunnerBuilder(boolean canUseSuiteMethod, JiraTestedProduct jiraProduct)
    {
        super(canUseSuiteMethod);
        this.jiraProduct = notNull(jiraProduct);
    }

    @Override
    protected AnnotatedBuilder annotatedBuilder()
    {
        return new AnnotatedBuilder(this)
        {
            @Override
            public Runner runnerForClass(Class<?> testClass) throws Exception
            {
                if (testClass.isAnnotationPresent(WebTest.class))
                {
                    return new JiraWebTestRunner(testClass, jiraProduct);
                }
                else
                {
                    return super.runnerForClass(testClass);
                }

            }
        };
    }

    @Override
    protected JUnit4Builder junit4Builder()
    {
        return new JUnit4Builder()
        {
            @Override
            public Runner runnerForClass(Class<?> testClass) throws Throwable
            {
                return new JiraWebTestRunner(testClass, jiraProduct);
            }
        };
    }
}
