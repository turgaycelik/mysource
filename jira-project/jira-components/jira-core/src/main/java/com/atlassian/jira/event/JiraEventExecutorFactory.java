package com.atlassian.jira.event;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.spi.EventExecutorFactory;
import com.atlassian.jira.EventComponent;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This is a thread pool for async events. The pool is configured with:
 * <ul>
 *     <li>Maximum of 5 threads.</li>
 *     <li>Timeout after 60 seconds for idle threads.</li>
 *     <li>Only daemon threads.</li>
 *     <li>Caller runs the the code when threads are busy (i.e. nothing is queued)</li>
 *     <li>Caller runs the the code when the pool is shutdown (i.e. events still get delivered during shutdown)</li>
 * </ul>
 *
 */
@EventComponent
public class JiraEventExecutorFactory implements EventExecutorFactory
{
    private final ThreadPoolExecutor executor;

    public JiraEventExecutorFactory()
    {
        final SynchronousQueue<Runnable> neverQ = new SynchronousQueue<Runnable>();
        final RejectedExecutionHandler callerRunsPolicy = new CallerRunsAlways();
        final ThreadFactoryBuilder builder = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("JIRA-EventThread-%d");
        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 5, 60, TimeUnit.SECONDS, neverQ, builder.build(), callerRunsPolicy);
        threadPoolExecutor.allowCoreThreadTimeOut(true);

        this.executor = threadPoolExecutor;
    }

    @Override
    public Executor getExecutor()
    {
        return executor;
    }

    @EventListener
    public void shutdown(ComponentManagerShutdownEvent shutdownEvent)
    {
        shutdown();
    }

    public void shutdown()
    {
        executor.shutdown();
    }

    @VisibleForTesting
    boolean shutdownAndWait(int seconds)
    {
        executor.shutdown();
        try
        {
            executor.awaitTermination(seconds, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            //fall through.
        }
        return executor.isShutdown();
    }

    private static class CallerRunsAlways implements RejectedExecutionHandler
    {
        @Override
        public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor)
        {
            r.run();
        }
    }
}
