package com.atlassian.jira.util.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.index.ha.NodeReindexService;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.sharing.index.SharedEntityIndexManager;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.RuntimeInterruptedException;

import org.apache.log4j.Logger;

/**
 * Convenience class for managing known IndexManagers and calling them all.
 *
 * @since v5.0
 */
public class CompositeIndexLifecycleManager implements IndexLifecycleManager
{
    private static final Logger log = Logger.getLogger(CompositeIndexLifecycleManager.class);

    private final IndexLifecycleManager[] delegates;
    private final IndexingCounterManager indexingCounterManager;

    public CompositeIndexLifecycleManager(final IssueIndexManager issueIndexManager, final SharedEntityIndexManager sharedEntityIndexManager,
            final IndexingCounterManager indexingCounterManager)
    {
        delegates = new IndexLifecycleManager[] { issueIndexManager, sharedEntityIndexManager };
        this.indexingCounterManager = indexingCounterManager;
    }

    public long optimize()
    {
        log.info("Optimize Indexes starting...");

        long result = 0;
        for (final IndexLifecycleManager delegate : delegates)
        {
            final long optimize = delegate.optimize();
            log.info("Optimize took: " + optimize + "ms. Indexer: " + delegate.toString());
            result += optimize;
        }
        log.info("Optimize Indexes complete. Total time: " + result + "ms.");
        return result;
    }

    public long reIndexAll(final Context context)
    {
        log.info("Reindex All starting...");

        // Stop the node from processing re-index events from elsewhere
        final NodeReindexService nodeReindexService = ComponentAccessor.getComponent(NodeReindexService.class);
        nodeReindexService.pause();
        nodeReindexService.resetIndexCount();

        long result = 0;
        try
        {
            for (final IndexLifecycleManager delegate : delegates)
            {
                try
                {
                    final long reIndexAll = delegate.reIndexAll(context);
                    log.info("Reindex took: " + reIndexAll + "ms. Indexer: " + delegate.toString());
                    result += reIndexAll;
                }
                catch (RuntimeException re)
                {
                    log.error("Reindex All FAILED.  Indexer: " + delegate.toString(), re);
                    throw re;
                }
            }
        }
        finally
        {
            nodeReindexService.start();
        }

        long newCounterValue = indexingCounterManager.incrementValue();
        context.setName("");
        log.info("Reindex All complete. Total time: " + result + "ms. Reindex run: " + newCounterValue);

        return result;
    }

    @Override
    public long reIndexAllIssuesInBackground(final Context context)
    {
        return reIndexAllIssuesInBackground(context, false, false);
    }

    public long reIndexAllIssuesInBackground(final Context context, final boolean reIndexComments, final boolean reIndexChangeHistory)
    {
        log.info("Reindex All In Background starting...");
        long result = 0;
        for (final IndexLifecycleManager delegate : delegates)
        {
            try
            {
                final long reIndexAll = delegate.reIndexAllIssuesInBackground(context, reIndexComments, reIndexChangeHistory);
                log.info("Reindex took: " + reIndexAll + "ms. Indexer: " + delegate.toString());
                result += reIndexAll;
            }
            catch (RuntimeInterruptedException rie)
            {
                log.warn("Reindex All In Background CANCELLED. Indexer: " + delegate.toString());
                throw rie;
            }
            catch (RuntimeException re)
            {
                log.error("Reindex All In Background FAILED. Indexer: " + delegate.toString(), re);
                throw re;
            }
        }
        long newCounterValue = indexingCounterManager.incrementValue();
        context.setName("");
        log.info("Reindex All In Background complete. Total time: " + result + "ms. Reindex run: " + newCounterValue);

        return result;
    }

    public void shutdown()
    {
        for (final IndexLifecycleManager delegate : delegates)
        {
            delegate.shutdown();
        }
    }

    public long activate(final Context context)
    {
        return activate(context, true);
    }

    public long activate(final Context context, final boolean reindex)
    {
        long result = 0;
        for (final IndexLifecycleManager delegate : delegates)
        {
            result += delegate.activate(context, reindex);
        }
        return result;
    }

    public void deactivate()
    {
        for (final IndexLifecycleManager delegate : delegates)
        {
            delegate.deactivate();
        }
    }

    public boolean isIndexingEnabled()
    {
        return delegates[0].isIndexAvailable();
    }

    public boolean isIndexAvailable()
    {
        return delegates[0].isIndexAvailable();
    }

    public boolean isIndexConsistent()
    {
        for (final IndexLifecycleManager delegate : delegates)
        {
            if (!delegate.isIndexConsistent())
            {
                return false;
            }
        }
        return true;
    }

    public Collection<String> getAllIndexPaths()
    {
        final Collection<String> result = new ArrayList<String>();
        for (final IndexLifecycleManager delegate : delegates)
        {
            result.addAll(delegate.getAllIndexPaths());
        }
        return Collections.unmodifiableCollection(result);
    }

    public int size()
    {
        int result = 0;
        for (final IndexLifecycleManager delegate : delegates)
        {
            result += delegate.size();
        }
        return result;
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }
}
