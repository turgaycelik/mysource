package com.atlassian.jira.local.runner;

import com.atlassian.jira.local.listener.StatsGatheringRunListener;
import junit.framework.TestCase;
import junit.framework.TestListener;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A global container of {@link TestListener} objects to allow us to spy on our own test code
 *
 * @since v4.3
 */
public class GlobalRunNotifier extends RunNotifier
{

    private static GlobalRunNotifier INSTANCE = new GlobalRunNotifier();
    private static AtomicBoolean SUITE_STARTED = new AtomicBoolean(false);
    private static AtomicBoolean SUITE_ENDED = new AtomicBoolean(false);
    private Result result = new Result();

    static
    {
        //
        // Register any other test listeners here so they can participate in the
        // world of test events!
        //
        INSTANCE.addListener(INSTANCE.getResult().createListener());
        INSTANCE.addListener(new TestRunStartedListener());
        INSTANCE.addListener(new StatsGatheringRunListener());
    }

    static
    {
        handleJVMShutdown();
    }

    public static GlobalRunNotifier getInstance()
    {
        return INSTANCE;
    }

    private static void handleJVMShutdown()
    {
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                INSTANCE.fireTestRunFinished(INSTANCE.getResult());
            }
        });
    }

    /**
     * At any time you can ask for the live result object of the test suite run.
     *
     * @return a {@link org.junit.runner.Result} object.  Its alive!
     */
    public Result getResult()
    {
        return result;
    }

    /**
     * Depending on the test environment we may be notified of test run start or not.  We have a little guard against
     * this.
     */
    private static class TestRunStartedListener extends RunListener
    {
        @Override
        public void testRunStarted(Description description) throws Exception
        {
            INSTANCE.fireTestRunStarted(description);
        }
    }

    @Override
    public void fireTestRunStarted(Description description)
    {
        if (SUITE_STARTED.compareAndSet(false, true))
        {
            super.fireTestRunStarted(description);
        }
    }

    @Override
    public void fireTestRunFinished(Result result)
    {
        if (SUITE_ENDED.compareAndSet(false, true))
        {
            super.fireTestRunFinished(result);
        }
    }

    @Override
    public void fireTestStarted(Description description) throws StoppedByUserException
    {
        if (!SUITE_STARTED.get())
        {
            fireTestRunStarted(Description.createSuiteDescription("JIRA Unit Tests"));
        }
        super.fireTestStarted(description);
    }

    public void fireTestStarted(TestCase test) throws StoppedByUserException
    {
        fireTestStarted(toDesc(test));
    }

    public void fireTestFailure(TestCase test, Throwable thrownException)
    {
        fireTestFailure(new Failure(toDesc(test), thrownException));
    }

    public void fireTestFinished(TestCase test)
    {
        fireTestFinished(toDesc(test));
    }

    private Description toDesc(TestCase test)
    {
        return Description.createTestDescription(test.getClass(), test.getName());
    }

}
