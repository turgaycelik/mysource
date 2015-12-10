package com.atlassian.jira.local;

import com.atlassian.jira.local.runner.GlobalRunNotifier;
import junit.framework.TestCase;

/**
 * A Junit3 TestCase  that participates in the listener program.  Without this class
 * as a base class (for JUnit3) there is no way to get events about when tests run
 * because neither IDEA nor Surefire have a way to inject a {@link org.junit.runner.notification.RunListener}
 * into the test world.
 *
 * @since v4.2
 */
public abstract class Junit3ListeningTestCase extends TestCase
{
    public Junit3ListeningTestCase()
    {
        super();
    }

    public Junit3ListeningTestCase(String name)
    {
        super(name);
    }

    @Override
    public void runBare() throws Throwable
    {
        runBareReImplementation();
    }

    /**
     * We re-implement TestCase because JUnit refuses to allow us into its world!
     * <p/>
     * How do we reimplemnent you ask?  Via that trusty AOP method called copy and paste
     *
     * @throws Throwable because stuff went south!
     */
    protected void runBareReImplementation() throws Throwable
    {
        final GlobalRunNotifier notifier = GlobalRunNotifier.getInstance();
        notifier.fireTestStarted(this);

        Throwable exception = null;
        setUp();
        try
        {
            runTest();
        }
        catch (Throwable running)
        {
            notifier.fireTestFailure(this, running);
            exception = running;
        }
        finally
        {
            notifier.fireTestFinished(this);
            try
            {
                tearDown();
            }
            catch (Throwable tearingDown)
            {
                if (exception == null)
                {
                    exception = tearingDown;
                }
            }
        }
        if (exception != null)
        {
            throw exception;
        }

    }
}