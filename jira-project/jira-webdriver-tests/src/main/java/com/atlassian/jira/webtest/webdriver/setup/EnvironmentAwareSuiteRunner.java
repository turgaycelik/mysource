package com.atlassian.jira.webtest.webdriver.setup;

import com.atlassian.jira.functest.framework.WebTestListener;
import com.atlassian.jira.functest.framework.suite.WebTestRunners;
import com.atlassian.jira.functest.framework.suite.WebTestSuiteRunner;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.config.EnvironmentBasedProductInstance;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.webtests.util.EnvironmentAware;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;

/**
 * Runner based on cargo environment data instance.
 *
 * @since 4.4
 */
public class EnvironmentAwareSuiteRunner extends WebTestSuiteRunner implements EnvironmentAware
{
    private JIRAEnvironmentData environmentData;
    private JiraTestedProduct jiraProduct;
    private Runner delegate;
    private SuiteTestListenerProvider listenerProvider;

    public EnvironmentAwareSuiteRunner(Class<?> webTestSuiteClass) throws InitializationError
    {
        super(webTestSuiteClass);
    }

    @Override
    protected Runner delegateRunner()
    {
        Assertions.notNull("setEnvironmentData has not been called yet!", delegate);
        return delegate;
    }

    @Override
    public void setEnvironmentData(JIRAEnvironmentData environmentData)
    {
        this.environmentData = environmentData;
        this.jiraProduct = new JiraTestedProduct(new EnvironmentBasedProductInstance(environmentData));
        // TODO this exception should actually be allowed to go through cause we're breaking JUnit4 contracts otherwise
        // but we would need a separate EnvironmentAwareThatCanThrowFreakinInitializationError
        // and TestSetupRunnerCallbackThatCanThrowFreakinInitializationError classes... ooouch
        try
        {
            if (!environmentData.isSingleNamedTest())
            {
                this.delegate = WebTestRunners.newRunner(suite,
                        new JiraCompositeRunnerBuilder(true, jiraProduct),
                        testClasses.toArray(new Class<?>[testClasses.size()]));
            }
            else
            {
                this.delegate = WebTestRunners.newRunner(suite,
                        new JiraCompositeRunnerBuilder(true, jiraProduct),
                        environmentData.getSingleTestClass());
            }
        }
        catch (InitializationError initializationError)
        {
            throw new RuntimeException(initializationError);
        }
        listenerProvider = SuiteTestListenerProvider.cargo(jiraProduct);
    }

    @Override
    protected Iterable<WebTestListener> listeners()
    {
        return listenerProvider.listeners();
    }
}