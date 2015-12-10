package com.atlassian.jira.pageobjects.config.junit4;

import com.atlassian.jira.functest.framework.suite.JUnit4WebTestListener;
import com.atlassian.jira.functest.framework.suite.RunnerChildList;
import com.atlassian.jira.functest.framework.suite.SuiteTransform;
import com.atlassian.jira.functest.framework.suite.TransformableRunner;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.inject.InjectionContext;
import com.atlassian.pageobjects.util.InjectingTestedProducts;
import com.atlassian.webdriver.testing.runner.AbstractProductContextRunner;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Base runner for JIRA web tests.
 *
 * @since 4.4
 */
public class JiraWebTestRunner extends AbstractProductContextRunner implements TransformableRunner<JiraWebTestRunner>
{
    private final List<RunListener> listeners = Lists.newArrayList();
    private final List<SuiteTransform> transforms = Lists.newArrayList();
    private final JiraTestedProduct product;

    public JiraWebTestRunner(Class<?> klass, JiraTestedProduct product, Iterable<RunListener> listeners) throws InitializationError
    {
        this(klass, product, listeners, Collections.<SuiteTransform>emptyList());
    }

    private JiraWebTestRunner(Class<?> klass, JiraTestedProduct product, Iterable<RunListener> listeners,
            Iterable<SuiteTransform> transforms) throws InitializationError
    {
        super(klass);
        this.product = product;
        Iterables.addAll(this.listeners, listeners);
        injectStuffToListeners(product);
        Iterables.addAll(this.transforms, transforms);
    }

    private void injectStuffToListeners(JiraTestedProduct product)
    {
        final InjectionContext injectionContext = InjectingTestedProducts.asInjectionContext(product);
        for (RunListener runListener : listeners)
        {
            injectionContext.injectMembers(runListener);
            if (runListener instanceof JUnit4WebTestListener)
            {
                injectionContext.injectMembers(((JUnit4WebTestListener) runListener).webTestListener());
            }
        }
    }

    @Override
    protected TestedProduct<?> getProduct()
    {
        return product;
    }

    public JiraWebTestRunner(Class<?> klass, JiraTestedProduct product) throws InitializationError
    {
        this(klass, product, Collections.<RunListener>emptyList());
    }

    public JiraWebTestRunner withTransforms(List<SuiteTransform> transforms) throws InitializationError
    {
        return new JiraWebTestRunner(getTestClass().getJavaClass(), product, listeners, transforms);
    }

    @Override
    protected List<FrameworkMethod> getChildren()
    {
        final List<FrameworkMethod> children = super.getChildren();
        final List<Description> descriptions = Lists.transform(children, new Function<FrameworkMethod, Description>()
        {
            @Override
            public Description apply(@Nullable FrameworkMethod from)
            {
                return describeChild(from);
            }
        });
        return RunnerChildList.matchingChildren(children, descriptions, transforms);
    }

    @Override
    protected Statement classBlock(final RunNotifier notifier)
    {
        final Statement original = super.classBlock(notifier);
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                addListenersTo(notifier);
                try
                {
                    original.evaluate();
                }
                finally
                {
                    removeListenersFrom(notifier);
                }
            }
        };
    }

    private void addListenersTo(RunNotifier runNotifier)
    {
        for (RunListener listener : listeners)
        {
            runNotifier.addListener(listener);
        }
    }

    private void removeListenersFrom(RunNotifier runNotifier)
    {
        for (RunListener listener : listeners)
        {
            runNotifier.removeListener(listener);
        }
    }
}
