package com.atlassian.jira.functest.framework.suite;

import com.atlassian.jira.functest.framework.WebTestDescription;
import com.atlassian.jira.functest.framework.WebTestListener;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.webtests.util.ClassLocator;
import com.google.common.collect.Sets;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * <p>
 * A JUnit4 runner for JIRA web tests suite. Given a {@link com.atlassian.jira.functest.framework.suite.WebTestSuite}
 * implementation class it instantiates it and gathers information necessary to create a delegate runner that will
 * run all the tests in the suite.
 *
 * @since 4.3
 */
public abstract class WebTestSuiteRunner extends Runner
{
    protected final Class<?> webTestSuiteClass;
    protected final WebTestSuite suite;
    protected final String testPackage;
    protected final Set<Class<?>> testClasses;

    public WebTestSuiteRunner(Class<?> webTestSuiteClass) throws InitializationError
    {
        this.webTestSuiteClass = notNull("webTestSuiteClass", webTestSuiteClass);
        this.suite = instantiateSuite();
        this.testPackage = suite.webTestPackage();
        this.testClasses = filterClasses();
    }

    /**
     * Override this method to create the runner that will perform actual test run.
     *
     * @return delegate runner for the suite of web test classes
     */
    abstract protected Runner delegateRunner();

    /**
     * A list of listeners to apply for this suite
     *
     * @return list of listeners
     */
    protected Iterable<WebTestListener> listeners()
    {
        return Collections.emptyList();
    }

    private WebTestSuite instantiateSuite()
    {
        try
        {
            return (WebTestSuite) webTestSuiteClass.getConstructor().newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to instantiate suite", e);
        }
    }

    private Set<Class<?>> filterClasses()
    {
        List<Class<?>> webTests = new ClassLocator<Object>(Object.class)
                .setAllowInner(true)
                .setPredicate(new TestPredicate())
                .setPackage(testPackage)
                .findClasses();
        return Sets.newLinkedHashSet(webTests);
    }

    private class TestPredicate implements Predicate<Class<?>>
    {
        @Override
        public boolean evaluate(Class<?> input)
        {
            if (!Modifier.isPublic(input.getModifiers()) || Modifier.isAbstract(input.getModifiers()))
            {
                return false;
            }
            return true;
        }

    }

    @Override
    public Description getDescription()
    {
        return delegateRunner().getDescription();
    }

    @Override
    public void run(RunNotifier notifier)
    {
        final Runner runner = delegateRunner();
        final WebTestDescription mainDescription = new JUnit4WebTestDescription(runner.getDescription());
        final Iterable<WebTestListener> listeners = listeners();
        addListeners(notifier, listeners);
        try
        {
            fireSuiteStarted(listeners, mainDescription);
            runner.run(notifier);
        }
        finally
        {
            fireSuiteFinished(listeners, mainDescription);
        }
    }

    private void fireSuiteStarted(Iterable<WebTestListener> listeners, WebTestDescription suiteDescription)
    {
        for (WebTestListener listener : listeners)
        {
            listener.suiteStarted(suiteDescription);
        }
    }

    private void fireSuiteFinished(Iterable<WebTestListener> listeners, WebTestDescription suiteDescription)
    {
        for (WebTestListener listener : listeners)
        {
            listener.suiteFinished(suiteDescription);
        }
    }

    private void addListeners(RunNotifier notifier, Iterable<WebTestListener> listeners)
    {
        for (WebTestListener listener : listeners)
        {
            notifier.addListener(new JUnit4WebTestListener(listener));
        }
    }

    public final WebTestSuite suite()
    {
        return suite;
    }
}
