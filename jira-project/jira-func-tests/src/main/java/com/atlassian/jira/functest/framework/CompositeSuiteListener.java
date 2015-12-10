package com.atlassian.jira.functest.framework;

import com.atlassian.jira.util.collect.CollectionBuilder;
import com.google.common.base.Function;
import org.apache.log4j.Logger;

import java.util.Collection;

public class CompositeSuiteListener implements WebTestListener
{
    private static final Logger log = Logger.getLogger(CompositeSuiteListener.class);

    private final Collection<WebTestListener> listeners;

    private CompositeSuiteListener(Collection<WebTestListener> listeners)
    {
        this.listeners = listeners;
    }

    @Override
    public void suiteStarted(final WebTestDescription suiteDescription)
    {
        forEachSafely(new Function<WebTestListener, Void>()
        {
            @Override
            public Void apply(WebTestListener input)
            {
                input.suiteStarted(suiteDescription);
                return null;
            }
        });
    }

    @Override
    public void suiteFinished(final WebTestDescription suiteDescription)
    {
        forEachSafely(new Function<WebTestListener, Void>()
        {
            @Override
            public Void apply(WebTestListener input)
            {
                input.suiteFinished(suiteDescription);
                return null;
            }
        });
    }

    @Override
    public void testError(final WebTestDescription test, final Throwable t)
    {
        forEachSafely(new Function<WebTestListener, Void>()
        {
            @Override
            public Void apply(WebTestListener input)
            {
                input.testError(test, t);
                return null;
            }
        });
    }

    @Override
    public void testFailure(final WebTestDescription test, final Throwable t)
    {
        forEachSafely(new Function<WebTestListener, Void>()
        {
            @Override
            public Void apply(WebTestListener input)
            {
                input.testFailure(test, t);
                return null;
            }
        });
    }

    @Override
    public void testFinished(final WebTestDescription test)
    {
        forEachSafely(new Function<WebTestListener, Void>()
        {
            @Override
            public Void apply(WebTestListener input)
            {
                input.testFinished(test);
                return null;
            }
        });
    }

    @Override
    public void testStarted(final WebTestDescription test)
    {
        forEachSafely(new Function<WebTestListener, Void>()
        {
            @Override
            public Void apply(WebTestListener input)
            {
                input.testStarted(test);
                return null;
            }
        });
    }

    private void forEachSafely(Function<WebTestListener, ?> call)
    {
        for (WebTestListener listener : listeners)
        {
            try
            {
                call.apply(listener);
            }
            catch (Exception e)
            {
                log.error("Error occurred while running test listener.", e);
            }
        }
    }

    public static WebTestListener of(WebTestListener listener)
    {
        return listener;
    }

    public static WebTestListener of(WebTestListener...listeners)
    {
        if (listeners == null || listeners.length == 0)
        {
            throw new IllegalArgumentException("listeners is null or empty");
        }
        else if (listeners.length == 1)
        {
            return listeners[0];
        }
        else
        {
            return new CompositeSuiteListener(CollectionBuilder.newBuilder(listeners).asList());
        }
    }
}
