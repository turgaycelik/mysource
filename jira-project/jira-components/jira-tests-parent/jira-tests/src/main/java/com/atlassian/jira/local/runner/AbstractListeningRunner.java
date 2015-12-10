package com.atlassian.jira.local.runner;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A base class to be used to wrap other JUnit4 runners and listen to the events that will be generated for tests
 *
 * @since v4.3
 */
public class AbstractListeningRunner<R extends Class<? extends Runner>> extends Runner
{
    private final Runner delegateRunner;

    public AbstractListeningRunner(final Class<?> classUnderTest, final R runnerDelegateClass)
    {
        try
        {
            Constructor<? extends Runner> constructor = runnerDelegateClass.getConstructor(new Class[] { Class.class });
            delegateRunner = constructor.newInstance(classUnderTest);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Description getDescription()
    {
        return delegateRunner.getDescription();
    }

    @Override
    public int testCount()
    {
        return delegateRunner.testCount();
    }

    @Override
    public void run(RunNotifier notifier)
    {
        delegateRunner.run(new ChainedRunNotifier(notifier, GlobalRunNotifier.getInstance()));
    }


    private class ChainedRunNotifier extends RunNotifier
    {
        private abstract class SafeInvoker
        {
            void run()
            {
                for (RunNotifier delegate : delegates)
                {
                    try
                    {
                        invoke(delegate);
                    }
                    catch (Exception ignored)
                    {
                    }
                }
            }
            
            abstract void invoke(final RunNotifier delegate);
        }

        private final RunNotifier[] delegates;

        private ChainedRunNotifier(RunNotifier... delegates)
        {
            this.delegates = delegates;
        }

        @Override
        public void addListener(final RunListener listener)
        {
            new SafeInvoker()
            {
                void invoke(RunNotifier delegate)
                {
                    delegate.addListener(listener);
                }
            }.run();
        }

        @Override
        public void removeListener(final RunListener listener)
        {
            new SafeInvoker()
            {
                void invoke(RunNotifier delegate)
                {
                    delegate.removeListener(listener);
                }
            }.run();
        }

        @Override
        public void fireTestRunStarted(final Description description)
        {
            new SafeInvoker()
            {
                void invoke(RunNotifier delegate)
                {
                    delegate.fireTestRunStarted(description);
                }
            }.run();
        }

        @Override
        public void fireTestRunFinished(final Result result)
        {
            new SafeInvoker()
            {
                void invoke(RunNotifier delegate)
                {
                    delegate.fireTestRunFinished(result);
                }
            }.run();
        }

        @Override
        public void fireTestStarted(final Description description) throws StoppedByUserException
        {
            new SafeInvoker()
            {
                void invoke(RunNotifier delegate)
                {
                    delegate.fireTestStarted(description);
                }
            }.run();
        }

        @Override
        public void fireTestFailure(final Failure failure)
        {
            new SafeInvoker()
            {
                void invoke(RunNotifier delegate)
                {
                    delegate.fireTestFailure(failure);
                }
            }.run();
        }

        @Override
        public void fireTestAssumptionFailed(final Failure failure)
        {
            new SafeInvoker()
            {
                void invoke(RunNotifier delegate)
                {
                    delegate.fireTestAssumptionFailed(failure);
                }
            }.run();
        }

        @Override
        public void fireTestIgnored(final Description description)
        {
            new SafeInvoker()
            {
                void invoke(RunNotifier delegate)
                {
                    delegate.fireTestIgnored(description);
                }
            }.run();
        }

        @Override
        public void fireTestFinished(final Description description)
        {
            new SafeInvoker()
            {
                void invoke(RunNotifier delegate)
                {
                    delegate.fireTestFinished(description);
                }
            }.run();
        }

        @Override
        public void pleaseStop()
        {
            new SafeInvoker()
            {
                void invoke(RunNotifier delegate)
                {
                    delegate.pleaseStop();
                }
            }.run();
        }

        @Override
        public void addFirstListener(final RunListener listener)
        {
            new SafeInvoker()
            {
                void invoke(RunNotifier delegate)
                {
                    delegate.addFirstListener(listener);
                }
            }.run();
        }
    }
}
