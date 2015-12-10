package com.atlassian.jira.webtest.webdriver.setup;

import com.atlassian.jira.functest.framework.WebTestListener;
import com.atlassian.jira.functest.framework.suite.WebTestRunners;
import com.atlassian.jira.functest.framework.suite.WebTestSuiteRunner;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.config.EnvironmentBasedProductInstance;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;

import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;

/**
 * Runner based on local environment data instance.
 *
 * @since 4.4
 */
public class LocalSuiteRunner extends WebTestSuiteRunner
{
    private final JIRAEnvironmentData environmentData;
    private final JiraTestedProduct jiraProduct;
    private final Runner delegate;
    private final SuiteTestListenerProvider listenerProvider;

    public LocalSuiteRunner(Class<?> webTestSuiteClass) throws InitializationError
    {
        super(webTestSuiteClass);
        // TODO better - read location from sys property
        this.environmentData = new LocalTestEnvironmentData();
        this.jiraProduct = new JiraTestedProduct(new EnvironmentBasedProductInstance(environmentData));
        this.delegate = WebTestRunners.newRunner(suite,
                new JiraCompositeRunnerBuilder(true, jiraProduct),
                testClasses.toArray(new Class<?>[testClasses.size()]));
        this.listenerProvider = SuiteTestListenerProvider.local(jiraProduct);
    }

    @Override
    protected Runner delegateRunner()
    {
        return delegate;
    }

    @Override
    protected Iterable<WebTestListener> listeners()
    {
        return listenerProvider.listeners();
    }
}