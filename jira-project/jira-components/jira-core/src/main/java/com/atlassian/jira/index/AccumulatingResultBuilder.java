package com.atlassian.jira.index;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.jira.index.Index.Result;
import com.atlassian.jira.util.log.RateLimitingLogger;
import com.atlassian.util.concurrent.Timeout;

import com.google.common.annotations.VisibleForTesting;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Used to build a {@link Result} implementation that accumulates results from
 * other operations and awaits on them all.
 *
 * For operations that are complete it just aggregates their results.
 */
public final class AccumulatingResultBuilder
{
    private static final RateLimitingLogger log = new RateLimitingLogger(AccumulatingResultBuilder.class);
    private final Collection<InFlightResult> inFlightResults = new LinkedBlockingQueue<InFlightResult>();
    private int successesToDate = 0;
    private int failuresToDate = 0;
    private final Collection<Runnable> completionTasks = new LinkedList<Runnable>();

    public AccumulatingResultBuilder add( @Nonnull final Index.Result result)
    {
        notNull("result", result);
        if (result instanceof CompositeResult)
        {
            CompositeResult compositeResult = (CompositeResult) result;
            for (InFlightResult ifr : compositeResult.getResults())
            {
                addInternal(ifr);
            }
            successesToDate += compositeResult.getSuccesses();
            failuresToDate += compositeResult.getFailures();
        }
        else
        {
            addInternal(null, null, result);
        }
        return this;
    }

    public AccumulatingResultBuilder add(String indexName, Long identifier, @Nonnull final Index.Result result)
    {
        notNull("result", result);
        if (result instanceof CompositeResult)
        {
            add(result);
        }
        else
        {
            addInternal(indexName, identifier, result);
        }
        return this;
    }

    private void addInternal(final InFlightResult ifr)
    {
        checkCompleted();
        if (ifr.getResult().isDone())
        {
            collectResult(ifr.getIndexName(), ifr.getIdentifier(), ifr.getResult());
        }
        else
        {
            inFlightResults.add(ifr);
        }
    }

    private void addInternal(final String indexName, final Long identifier, @Nonnull final Result result)
    {
        checkCompleted();
        if (result.isDone())
        {
            collectResult(indexName, identifier, result);
        }
        else
        {
            inFlightResults.add(new InFlightResult(indexName, identifier, result));
        }
    }

    public void addCompletionTask(@Nonnull final Runnable runnable)
    {
        completionTasks.add(notNull("runnable", runnable));
    }

    /**
     * Keep the results list small, we don't want to waste too much ram with
     * complete results.
     */
    private void checkCompleted()
    {
        for (final Iterator<InFlightResult> iterator = inFlightResults.iterator(); iterator.hasNext();)
        {
            final InFlightResult ifr = iterator.next();
            if (ifr.getResult().isDone())
            {
                collectResult(ifr.getIndexName(), ifr.getIdentifier(), ifr.getResult());
                iterator.remove();
            }
        }
    }

    private void collectResult(final String indexName, final Long identifier, final Result result)
    {
        try
        {
            result.await();
            successesToDate++;
        }
        catch (RuntimeException e)
        {
            failuresToDate++;
            logFailure(indexName, identifier, e);
        }
        catch (Error e)
        {
            failuresToDate++;
            logFailure(indexName, identifier, e);
        }
    }

    public Result toResult()
    {
        return new CompositeResult(inFlightResults, successesToDate, failuresToDate, completionTasks);
    }

    private static void logFailure(final String indexName, final Long identifier, final Throwable e)
    {
        // We don't want to flood the logs if an indexing operation is going awry, but
        // we do want the ability to debug it in production when required.
        if (indexName != null)
        {
            log.warn("Indexing failed for " + indexName + " - '" + identifier + '\'');
        }
        log.warn(e.getMessage(), e);
    }

    /**
     * This class holds the actual result objects and aggregates them. Once a
     * result has been awaited then it can be discarded.
     */
    static class CompositeResult implements Result
    {
        private final Collection<InFlightResult> results;
        private final Queue<Runnable> completionTasks;
        private int successes;
        private int failures;

        CompositeResult(final Collection<InFlightResult> inFlightResults, final int successes, final int failures, final Collection<Runnable> completionTasks)
        {
            this.successes = successes;
            this.failures = failures;
            this.results = new LinkedBlockingQueue<InFlightResult>(inFlightResults);
            this.completionTasks = new LinkedList<Runnable>(completionTasks);
        }

        public void await()
        {
            for (final Iterator<InFlightResult> it = results.iterator(); it.hasNext();)
            {
                // all threads should await
                final InFlightResult ifr = it.next();
                final Result result = ifr.getResult();
                try
                {
                    result.await();
                    successes++;
                }
                catch (RuntimeException e)
                {
                    failures++;
                    logFailure(ifr.getIndexName(), ifr.getIdentifier(), e);
                }
                // once run, they should be removed
                it.remove();
            }
            if (failures > 0)
            {
                throw new IndexingFailureException(failures);
            }
            complete();
        }

        private void complete()
        {
            while (!completionTasks.isEmpty())
            {
                // only one thread should run these tasks
                final Runnable task = completionTasks.poll();
                // /CLOVER:OFF
                if (task != null)
                {
                    // /CLOVER:ON

                    task.run();
                }
            }
        }

        public boolean await(final long time, final TimeUnit unit)
        {
            final Timeout timeout = Timeout.getNanosTimeout(time, unit);
            for (final Iterator<InFlightResult> it = results.iterator(); it.hasNext();)
            {
                // all threads should await
                final InFlightResult ifr = it.next();
                final Result result = ifr.getResult();
                try
                {
                    if (!result.await(timeout.getTime(), timeout.getUnit()))
                    {
                        return false;
                    }
                    successes++;
                }
                catch (RuntimeException e)
                {
                    failures++;
                    logFailure(ifr.getIndexName(), ifr.getIdentifier(), e);
                }
                // once run, they should be removed
                it.remove();
            }
            if (failures > 0)
            {
                throw new IndexingFailureException(failures);
            }
            complete();
            return true;
        }

        public boolean isDone()
        {
            for (final InFlightResult ifr : results)
            {
                if (!ifr.getResult().isDone())
                {
                    return false;
                }
            }
            return true;
        }

        Iterable<InFlightResult> getResults()
        {
            return results;
        }

        int getSuccesses()
        {
            return successes;
        }

        int getFailures()
        {
            return failures;
        }
    }

    static class InFlightResult
    {
        private final String indexName;
        private final Long identifier;
        private final Result result;

        InFlightResult(final String indexName, final Long identifier, final Result result)
        {
            this.indexName = indexName;
            this.identifier = identifier;
            this.result = result;
        }

        String getIndexName()
        {
            return indexName;
        }

        Long getIdentifier()
        {
            return identifier;
        }

        Result getResult()
        {
            return result;
        }
    }
}