package com.atlassian.jira.functest.framework;

import com.atlassian.jira.util.dbc.Assertions;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;


/**
 * This class basically listens to tests and fires a callback if a test does not complete with the configured time. The
 * callback can be repeatably invoked after the initial timeout at a fixed interval.
 */
public class JiraTestWatchDog implements WebTestListener
{
    private static final Logger log = Logger.getLogger(JiraTestWatchDog.class);

    private final ScheduledExecutorService service;
    private final Predicate<WebTestDescription> monitorPredicate;
    private final Map<WebTestDescription, ScheduledFuture<?>> runningTests = Maps.newConcurrentMap();
    private final Function<WebTestDescription, ?> timoutCallable;
    private final long timeout;
    private final long repeatMax;
    private final long repeatDelay;
    private final TimeUnit unit;

    JiraTestWatchDog(final Predicate<WebTestDescription> monitorPredicate, long timeout, long repeatDelay, TimeUnit unit,
            long repeatMax, final Function<WebTestDescription, ?> timeoutCallable, final ScheduledExecutorService service)
    {
        this.service = Assertions.notNull("service", service);
        this.monitorPredicate = Assertions.notNull("monitorPredicate", monitorPredicate);
        this.timoutCallable = Assertions.notNull("timeoutCallable", timeoutCallable);
        this.unit = Assertions.notNull("unit", unit);
        this.timeout = timeout;
        this.repeatDelay = repeatDelay;
        this.repeatMax = repeatMax;
    }

    /**
     * Create a watchdog that will watch the tests and fire the passed callback for any tests that do not complete
     * within the specified amount of time.
     *
     * @param monitorPredicate will only monitor tests for which the predicate returns true.
     * @param timeout the time we should wait for a test to finish before it is considered to have frozen.
     * @param repeatDelay the delay between taking thread dumps once the timeout has passed.
     * @param unit the unit of the timeout and repeatDelay.
     * @param repeatMax the number of times to execute the callable.
     * @param timeoutCallable this callback is invoked
     */
    public JiraTestWatchDog(final Predicate<WebTestDescription> monitorPredicate, long timeout, long repeatDelay, TimeUnit unit,
            final int repeatMax, final Function<WebTestDescription, ?> timeoutCallable)
    {
        this(monitorPredicate, timeout, repeatDelay, unit, repeatMax, timeoutCallable, createDefaultExecutor());
    }

    @Override
    public void suiteStarted(final WebTestDescription suiteDescription)
    {
        //don't care.
    }

    @Override
    public void suiteFinished(final WebTestDescription suiteDescription)
    {
        close();
    }

    @Override
    public void testError(WebTestDescription description, Throwable t)
    {
        //endTest is always called.
    }

    @Override
    public void testFailure(WebTestDescription test, Throwable t)
    {
        //endTest is always called.
    }

    @Override
    public void testFinished(WebTestDescription test)
    {
        ScheduledFuture<?> future = runningTests.remove(test);
        if (future != null)
        {
            future.cancel(true);
        }
    }

    @Override
    public void testStarted(final WebTestDescription test)
    {
        if (!shouldMonitor(test) || service.isShutdown())
        {
            return;
        }

        //Here to make sure that we put the future in the map before
        //we run the task though this should never be a problem in the
        //real world.
        final CountDownLatch startBarrier = new CountDownLatch(1);
        final ScheduledFuture<?> future = service.scheduleWithFixedDelay(new Runnable()
        {
            private int repeat = 0;
            private boolean executing = false;
            public void run()
            {
                try
                {
                    startBarrier.await();
                }
                catch (InterruptedException e)
                {
                    log.error(format("Watchdog task for Test '%s' was killed. Watchdog functionality disabled for this test.",
                            test.name()), e);
                    return;
                }

                synchronized (this)
                {
                    //If we have executed the callback repeatMax times then do nothing.
                    if (repeat < repeatMax)
                    {
                        repeat = repeat + 1;

                        if (executing)
                        {
                            //If the callback is already running then don't try to run it in parallel.
                            return;
                        }
                        else
                        {
                            executing = true;
                        }
                    }
                    else
                    {
                        return;
                    }
                }

                timoutCallable.apply(test);

                synchronized (this)
                {
                    executing = false;

                    //If the callback has been called enough times then stop running this task.
                    if (repeat >= repeatMax)
                    {
                        testFinished(test);
                    }
                }
            }
        }, timeout, repeatDelay, unit);

        runningTests.put(test, future);
        startBarrier.countDown();
    }

    public void close()
    {
        for (ScheduledFuture<?> future : runningTests.values())
        {
            future.cancel(true);
        }
        service.shutdown();
    }

    private boolean shouldMonitor(WebTestDescription test)
    {
        return monitorPredicate.apply(test);
    }

    private static ScheduledExecutorService createDefaultExecutor()
    {
        ThreadFactory factory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("WatchDogThread-%d").build();
        return Executors.newSingleThreadScheduledExecutor(factory);
    }
}
