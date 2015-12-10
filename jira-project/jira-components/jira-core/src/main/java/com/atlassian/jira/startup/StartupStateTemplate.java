package com.atlassian.jira.startup;

import com.google.common.collect.ImmutableList;
import org.apache.log4j.Level;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Template class for JiraStartupState implementations. This class takes care of running and storing the results of all
 * startup checks (just override {@link #getStartupChecks()}.
 */
@ThreadSafe
public abstract class StartupStateTemplate implements JiraStartupState
{
    /**
     * Startup logger.
     */
    private static JiraStartupLogger log = new JiraStartupLogger();

    /**
     * An atomic reference to a future startup checks result. If the atomic reference's value is null, that indicates
     * that the startup checks have not yet run.
     */
    private final AtomicReference<Future<StartupChecksResult>> startupChecksResult = new AtomicReference<Future<StartupChecksResult>>();

    /**
     * Returns a boolean indicating if this startup state's startup checks have all passed. If the checks have not yet
     * been run, they will be run the first time that this method is called.
     *
     * @return a boolean indicating if this startup state's startup checks have all passed.
     */
    @Override
    public boolean isStartupChecksPassed()
    {
        Future<StartupChecksResult> checkResult = startupChecksResult.get();
        if (checkResult == null)
        {
            // this future task is used to run the check once and only once
            FutureTask<StartupChecksResult> newCheckResult = createFuture(new CreateStartupChecksResult());
            boolean wasNull = startupChecksResult.compareAndSet(null, newCheckResult);
            if (wasNull)
            {
                newCheckResult.run();
                checkResult = newCheckResult;
            }
            else
            {
                checkResult = startupChecksResult.get();
            }
        }

        try
        {
            return checkResult.get().validStartup;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Exception thrown while waiting for future computation", e);
        }
    }

    /**
     * Returns the first startup check that failed, or null. Note that if the startup checks have not yet been run, this
     * method returns null independently of whether the checks would actually fail at this point in time.
     *
     * @return the first startup check that failed, or null
     */
    @Override
    public StartupCheck getFailedStartupCheck()
    {
        Future<StartupChecksResult> futureCheckResult = startupChecksResult.get();
        if (futureCheckResult == null)
        {
            // startup checks haven't run yet
            return null;
        }

        try
        {
            return futureCheckResult.get().failedStartupCheck;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Exception thrown while waiting for future computation", e);
        }
    }

    /**
     * Sets the first startup check that failed.
     *
     * @param failedStartupCheck a StartupCheck
     */
    @Override
    public void setFailedStartupCheck(StartupCheck failedStartupCheck)
    {
        FutureTask<StartupChecksResult> futureTask = createFuture(new FailedCheckResult(failedStartupCheck));
        startupChecksResult.set(futureTask);
        futureTask.run();
    }

    /**
     * Template method that returns an immutable list of startup checks to perform.
     *
     * @return an ImmutableList<StartupCheck>
     */
    protected abstract ImmutableList<StartupCheck> getStartupChecks();

    /**
     * Callable that returns the first failed startup check.
     */
    private class CreateStartupChecksResult implements Callable<StartupChecksResult>
    {
        @Override
        public StartupChecksResult call() throws Exception
        {
            for (StartupCheck startupCheck : getStartupChecks())
            {
                if (!startupCheck.isOk())
                {
                    log.printMessage(startupCheck.getFaultDescription(), Level.FATAL);
                    setFailedStartupCheck(startupCheck);

                    return new StartupChecksResult(false, startupCheck);
                }
            }

            return new StartupChecksResult(true, null);
        }
    }

    /**
     * Callable that returns a specified failed startup check.
     */
    private class FailedCheckResult implements Callable<StartupChecksResult>
    {
        private final StartupCheck failedStartupCheck;

        public FailedCheckResult(StartupCheck failedStartupCheck) { this.failedStartupCheck = failedStartupCheck; }

        @Override
        public StartupChecksResult call() throws Exception
        {
            return new StartupChecksResult(false, failedStartupCheck);
        }
    }

    /**
     * This class is used for memo-ising the result of running the startup checks.
     */
    private class StartupChecksResult
    {
        final boolean validStartup;
        final StartupCheck failedStartupCheck;

        private StartupChecksResult(boolean validStartup, StartupCheck failedStartupCheck)
        {
            this.validStartup = validStartup;
            this.failedStartupCheck = failedStartupCheck;
        }
    }

    /**
     * Creates a new FutureTask from the given Callable.
     *
     * @param callable a Callable
     * @return a new FutureTask
     */
    private static <T> FutureTask<T> createFuture(Callable<T> callable)
    {
        return new FutureTask<T>(callable);
    }
}
