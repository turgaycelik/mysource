package com.atlassian.jira.webtest.webdriver.setup;

import com.atlassian.jira.functest.framework.JiraTestWatchDog;
import com.atlassian.jira.functest.framework.TomcatShutdownListener;
import com.atlassian.jira.functest.framework.WatchdogLoggingCallback;
import com.atlassian.jira.functest.framework.WebTestDescription;
import com.atlassian.jira.functest.framework.WebTestListener;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.config.junit4.LogTestInformationListener;
import com.atlassian.jira.webtest.capture.FFMpegSuiteListener;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Provides listeners and injections to the listeners for JIRA WebDriver test suites.
 *
 * @since v4.4
 */
public final class SuiteTestListenerProvider
{

    public static SuiteTestListenerProvider local(JiraTestedProduct product)
    {
        return new SuiteTestListenerProvider(product, Lists.<WebTestListener>newArrayList(
                new LogTestInformationListener("=====WEB DRIVER"),
                watchDogListener()
        ));
    }

    public static SuiteTestListenerProvider cargo(JiraTestedProduct product)
    {
        return local(product).add(
             new FFMpegSuiteListener(WebDriverCategoryPredicate.INSTANCE),
             new TomcatShutdownListener()
        );
    }

    private static JiraTestWatchDog watchDogListener()
    {
        return new JiraTestWatchDog(WebDriverCategoryPredicate.INSTANCE, 6, 1, TimeUnit.MINUTES, 4,
                WatchdogLoggingCallback.INSTANCE);
    }

    private static class WebDriverCategoryPredicate implements Predicate<WebTestDescription>
    {
        static WebDriverCategoryPredicate INSTANCE = new WebDriverCategoryPredicate();

        @Override
        public boolean apply(@Nullable WebTestDescription input)
        {
            return input.isTest() && input.categories().contains(Category.WEBDRIVER_TEST);
        }
    }


    private final List<WebTestListener> listeners;
    private final JiraTestedProduct product;

    SuiteTestListenerProvider(JiraTestedProduct product, Iterable<WebTestListener> listeners)
    {
        this.listeners = Lists.newArrayList(notNull(listeners));
        this.product = notNull(product);
        performInjections(listeners, product);
    }

    private void performInjections(Iterable<WebTestListener> listeners, JiraTestedProduct product)
    {
        for (WebTestListener listener : listeners)
        {
            product.injector().injectMembers(listener);
        }
    }

    public Iterable<WebTestListener> listeners()
    {
        return ImmutableList.copyOf(listeners);
    }

    private SuiteTestListenerProvider add(WebTestListener... moreListeners)
    {
        for (WebTestListener listener : moreListeners)
        {
            product.injector().injectMembers(listener);
        }
        this.listeners.addAll(Arrays.asList(moreListeners));
        return this;
    }
}
