/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.index;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nonnull;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.instrumentation.operations.OpTimer;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IndexTaskContext;
import com.atlassian.jira.config.ReindexMessage;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.IndexingConfiguration;
import com.atlassian.jira.event.ListenerManager;
import com.atlassian.jira.event.listeners.search.IssueIndexListener;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.index.AccumulatingResultBuilder;
import com.atlassian.jira.index.Index;
import com.atlassian.jira.index.IssueIndexHelper;
import com.atlassian.jira.index.ha.ReplicatedIndexManager;
import com.atlassian.jira.instrumentation.Instrumentation;
import com.atlassian.jira.instrumentation.InstrumentationName;
import com.atlassian.jira.issue.ActionConstants;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.util.DatabaseIssuesIterable;
import com.atlassian.jira.issue.util.IssueGVsIssueIterable;
import com.atlassian.jira.issue.util.IssueObjectIssuesIterable;
import com.atlassian.jira.issue.util.IssuesIterable;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.core.LifecycleAwareSchedulerService;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import net.jcip.annotations.GuardedBy;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static org.ofbiz.core.entity.EntityOperator.EQUALS;

public class DefaultIndexManager implements IssueIndexManager
{
    private static final Logger log = Logger.getLogger(DefaultIndexManager.class);

    public static final Analyzer ANALYZER_FOR_SEARCHING = JiraAnalyzer.ANALYZER_FOR_SEARCHING;
    public static final Analyzer ANALYZER_FOR_INDEXING = JiraAnalyzer.ANALYZER_FOR_INDEXING;

    // ---------------------------------------------------------------------------------------------------------- members

    /**
     * The index locks are used to keep "online" issue indexing (i.e. what happens when you edit a single issue) from
     * running concurrently with the "stop the world" reindex that deletes the index and re-creates everything.
     */
    @ClusterSafe("Indexes are local to each node")
    private final IndexLocks indexLock = new IndexLocks();

    private final IssueIndexer issueIndexer;
    private final IndexPathManager indexPathManager;
    private final IndexingConfiguration indexConfig;
    private final ReindexMessageManager reindexMessageManager;
    private final EventPublisher eventPublisher;
    private final ListenerManager listenerManager;
    private final ProjectManager projectManager;
    private final IssueManager issueManager;
    private final TaskManager taskManager;
    private final OfBizDelegator ofBizDelegator;
    private final ReplicatedIndexManager replicatedIndexManager;

    private final ThreadLocal<Boolean> indexingHeld = new ThreadLocal<Boolean>();
    private final ThreadLocal<Map<String, Issue>> heldIssues = new ThreadLocal<Map<String, Issue>>()
    {
        @Override
        protected Map<String, Issue> initialValue()
        {
            return new HashMap<String,Issue>();
        }
    };

    @Override
    public Analyzer getAnalyzerForSearching()
    {
        return ANALYZER_FOR_SEARCHING;
    }

    @Override
    public Analyzer getAnalyzerForIndexing()
    {
        return ANALYZER_FOR_INDEXING;
    }

    // responsible for getting the actual searcher when required
    private final Supplier<IndexSearcher> issueSearcherSupplier = new Supplier<IndexSearcher>()
    {
        public IndexSearcher get()
        {
            try
            {
                return issueIndexer.openIssueSearcher();
            }
            catch (final RuntimeException e)
            {
                throw new SearchUnavailableException(e, indexConfig.isIndexAvailable());
            }
        }
    };

    // responsible for getting the actual searcher when required
    private final Supplier<IndexSearcher> commentSearcherSupplier = new Supplier<IndexSearcher>()
    {
        public IndexSearcher get()
        {
            try
            {
                return issueIndexer.openCommentSearcher();
            }
            catch (final RuntimeException e)
            {
                throw new SearchUnavailableException(e, indexConfig.isIndexAvailable());
            }
        }
    };

    // responsible for getting the actual searcher when required
    private final Supplier<IndexSearcher> changeHistorySearcherSupplier = new Supplier<IndexSearcher>()
    {
        public IndexSearcher get()
        {
            try
            {
                return issueIndexer.openChangeHistorySearcher();
            }
            catch (final RuntimeException e)
            {
                throw new SearchUnavailableException(e, indexConfig.isIndexAvailable());
            }
        }
    };

    // ------------------------------------------------------------------------------------------------------------ ctors

    public DefaultIndexManager(final IndexingConfiguration indexProperties, final IssueIndexer issueIndexer, final IndexPathManager indexPath,
            ReindexMessageManager reindexMessageManager, EventPublisher eventPublisher, ListenerManager listenerManager, ProjectManager projectManager,
            final IssueManager issueManager, TaskManager taskManager, OfBizDelegator ofBizDelegator, ReplicatedIndexManager replicatedIndexManager)
    {
        this.issueManager = issueManager;
        this.taskManager = taskManager;
        this.ofBizDelegator = ofBizDelegator;
        this.eventPublisher = notNull("eventPublisher", eventPublisher);
        indexConfig = notNull("indexProperties", indexProperties);
        this.issueIndexer = notNull("issueIndexer", issueIndexer);
        indexPathManager = notNull("indexPath", indexPath);
        this.reindexMessageManager = notNull("reindexMessageManager", reindexMessageManager);
        this.listenerManager = listenerManager;
        this.projectManager = projectManager;
        this.replicatedIndexManager = replicatedIndexManager;
    }

    // ---------------------------------------------------------------------------------------------------------- methods

    public void deactivate()
    {
        listenerManager.deleteListener(IssueIndexListener.class);
        // Turn indexing off
        indexConfig.disableIndex();

        issueIndexer.shutdown();

        // Flush the ThreadLocal searcher to ensure that the index files are closed.
        // This method is called from DataImport, which flushes the mail queue. If the mail queue contains
        // any items that run a Lucene search (e.g. subscription mail queue items), they will initialise the
        // thread local searcher which will stop the index files from being deleted in Windows. Windows does not
        // allow to delete open files.
        // This code is here as it is a good idea to ensure that the searcher in the thread local is closed
        // before the index files are deleted.
        flushThreadLocalSearchers();

        eventPublisher.publish(new IndexDeactivatedEvent());
    }

    // Activates the indexing path currently configured by the IndexPathManager.
    public long activate(final Context context)
    {
        return activate(context, true);
    }

    // Activates the indexing path currently configured by the IndexPathManager.
    public long activate(final Context context, final boolean reindex)
    {
        Assertions.notNull("context", context);
        // Test if indexing is turned off
        if (isIndexAvailable())
        {
            throw new IllegalStateException("Cannot enable indexing as it is already enabled.");
        }
        if (log.isDebugEnabled())
        {
            log.debug("Activating indexes in '" + indexPathManager.getIndexRootPath() + "'.");
        }

        // Create Index listener
        listenerManager.createListener(IssueIndexListener.NAME, IssueIndexListener.class);

        // Turn indexing on
        indexConfig.enableIndex();

        if (reindex)
        {
            return reIndexAll(context);
        }
        return 0;
    }

    public boolean isIndexingEnabled()
    {
        return indexConfig.isIndexAvailable();
    }

    @Override
    public boolean isIndexAvailable()
    {
        return indexConfig.isIndexAvailable();
    }

    /**
     * @return The number of milliseconds taken to reindex everything, or -1 if not indexing
     * @throws IndexException
     */
    public long reIndexAll() throws IndexException
    {
        return reIndexAll(Contexts.nullContext());
    }

    public long reIndexAll(final Context context)
    {
        // don't use background indexing by default
        return reIndexAll(context, false);
    }

    public long reIndexAll(final Context context, boolean useBackgroundIndexing)
    {
        return reIndexAll(context, useBackgroundIndexing, true);
    }

    @Override
    public long reIndexAll(final Context context, boolean useBackgroundIndexing, boolean updateReplicatedIndex)
    {
        return reIndexAll(context, useBackgroundIndexing, false, false, updateReplicatedIndex);
    }

    @Override
    public long reIndexAll(final Context context, final boolean useBackgroundIndexing, final boolean reIndexComments, final boolean reIndexChangeHistory, final boolean updateReplicatedIndex)
    {
        Assertions.notNull("context", context);
        context.setName("Issue");
        log.info("Reindexing all issues");
        eventPublisher.publish(new ReindexAllStartedEvent(useBackgroundIndexing, updateReplicatedIndex));
        final long startTime = System.currentTimeMillis();

        ReindexMessage message = reindexMessageManager.getMessageObject();
        if (useBackgroundIndexing)
        {
            // doesn't stop the scheduler, doesn't delete indexes
            if (!getIndexLock())
            {
                log.error("Could not perform background re-index");
                return -1;
            }
            try
            {
                doBackgroundReindex(context, reIndexComments, reIndexChangeHistory);
            }
            catch (InterruptedException e)
            {
                eventPublisher.publish(new ReindexAllCancelledEvent());
                return -1;
            }
            finally
            {
                releaseIndexLock();
                // Releasing the lock should never throw an exception - so we are safe to flush the
                // searcher without another try/finally here.
                flushThreadLocalSearchers();
            }
        }
        else if(!withReindexLock(new Runnable()
            {
                @Override
                public void run()
                {
                    doStopTheWorldReindex(context);
                }
            }))
        {
            return -1;
        }
        // clear any reindex messages
        if (message != null)
        {
            reindexMessageManager.clearMessageForTimestamp(message.getTime());
        }

        final long totalTime = (System.currentTimeMillis() - startTime);
        if (log.isDebugEnabled())
        {
            log.debug("ReindexAll took : " + totalTime + "ms");
        }

        eventPublisher.publish(new ReindexAllCompletedEvent(totalTime, useBackgroundIndexing, updateReplicatedIndex));
        return totalTime;
    }

    /**
     * This method takes a runnable that is run under the 'stop the world' reindex lock. It is used here in preference
     * to a Guava function in order to avoid introducing more Guava into the API, which limits our ability to update
     * the library. The runnable is executed in the current thread.
     *
     * @param runnable The runnable to be executed
     * @return true if the lock was acquired and the runnable was run, false if the lock could not be acquired and the code was not run
     */
    public boolean withReindexLock(final Runnable runnable)
    {
        if (!indexLock.writeLock.tryLock())
        {
            return false;
        }

        try
        {
            runnable.run();
        }
        finally
        {
            indexLock.writeLock.unlock();

            // Releasing the lock should never throw an exception - so we are safe to flush the
            // seacher without another try/finally here.
            flushThreadLocalSearchers();
        }
        return true;
    }

    public long reIndexAllIssuesInBackground(final Context context)
    {
        return reIndexAllIssuesInBackground(context, false, false);
    }

    @Override
    public long reIndexAllIssuesInBackground(final Context context, final boolean reIndexComments, final boolean reIndexChangeHistory)
    {
        return reIndexAll(context, true, reIndexComments, reIndexChangeHistory, true);
    }

    public long reIndexIssues(final Collection<GenericValue> issues) throws IndexException
    {
        return reIndexIssues(new IssueGVsIssueIterable(issues, getIssueFactory()), Contexts.nullContext());
    }

    protected long reIndexIssues(final Collection<GenericValue> issues, boolean reIndexComments, boolean reIndexChangeHistory) throws IndexException
    {
        return reIndexIssues(new IssueGVsIssueIterable(issues, getIssueFactory()), Contexts.nullContext(), reIndexComments, reIndexChangeHistory);
    }

    public long reIndexIssueObjects(final Collection<? extends Issue> issueObjects) throws IndexException
    {
        return reIndexIssueObjects(issueObjects, true, true);
    }

    @Override
    public long reIndexIssueObjects(Collection<? extends Issue> issueObjects, boolean reIndexComments, boolean reIndexChangeHistory)
            throws IndexException
    {
        // We would like to transform the Issue object to GenericValues before re-indexing to ensure that there
        // are no discrepancies between them. Once we move the entire system to Issue objects this will be unnecessary.
        // Until then, please do *not* change this behaviour.
        @SuppressWarnings ( { "unchecked" })
        final Collection<GenericValue> genericValues = CollectionUtils.collect(issueObjects, IssueFactory.TO_GENERIC_VALUE);
        return reIndexIssues(genericValues, reIndexComments, reIndexChangeHistory);
    }

    @Override
    public long reIndexIssueObjects(final Collection<? extends Issue> issueObjects, final boolean reIndexComments, final boolean reIndexChangeHistory, final boolean updateReplicatedIndexStore)
            throws IndexException
    {
        return reIndexIssues(new IssueObjectIssuesIterable(issueObjects), Contexts.nullContext(), reIndexComments, reIndexChangeHistory, updateReplicatedIndexStore);
    }

    public void reIndex(final Issue issue) throws IndexException
    {
        reIndex(issue, true, true);
    }

    @Override
    public void reIndex(Issue issue, boolean reIndexComments, boolean reIndexChangeHistory) throws IndexException
    {
        final List<Issue> issues = Collections.singletonList(issue);
        reIndexIssueObjects(issues, reIndexComments, reIndexChangeHistory);
    }

    public void reIndex(final GenericValue issueGV) throws IndexException
    {
        if ("Issue".equals(issueGV.getEntityName()))
        {
            final List<GenericValue> genericValues = Lists.newArrayList(issueGV);
            reIndexIssues(genericValues);
        }
        else
        {
            log.error("Entity is not an issue " + issueGV.getEntityName());
        }
    }

    public void hold()
    {
        indexingHeld.set(Boolean.TRUE);
    }

    public boolean isHeld()
    {
        return indexingHeld.get() != null && indexingHeld.get();
    }

    public long release() throws IndexException
    {
        indexingHeld.set(Boolean.FALSE);
        try
        {
            final Map<String, Issue> queue = heldIssues.get();
            if (queue.size() > 0)
            {
                IssuesIterable issuesIterable = new IssueObjectIssuesIterable(queue.values());
                return reIndexIssues(issuesIterable, Contexts.nullContext());
            }
            return 0;
        }
        finally
        {
            heldIssues.remove();
            indexingHeld.remove();
        }
    }

    public long reIndexIssues(final IssuesIterable issuesIterable, final Context context) throws IndexException
    {
        return reIndexIssues(issuesIterable, context, true, true);
    }

    @Override
    public long reIndexIssues(IssuesIterable issuesIterable, Context context, boolean reIndexComments, boolean reIndexChangeHistory)
        throws IndexException
    {
        return reIndexIssues(issuesIterable, context, reIndexComments, reIndexChangeHistory, true);
    }

    private long reIndexIssues(final IssuesIterable issuesIterable, final Context context, final boolean reIndexComments, final boolean reIndexChangeHistory, final boolean updateReplicatedIndexStore)
    {
        // if indexing is currently held on this thread just save up the issues for later
        if (isHeld())
        {
            final Map<String, Issue> queue = heldIssues.get();
            issuesIterable.foreach(new Consumer<Issue>()
            {
                @Override
                public void consume(@Nonnull Issue element)
                {
                    queue.put(element.getKey(), element);
                }
            });
            return 0;
        }

        Assertions.notNull("issues", issuesIterable);
        Assertions.notNull("context", context);

        eventPublisher.publish(new ReindexIssuesStartedEvent());
        final OpTimer opTimer = Instrumentation.pullTimer(InstrumentationName.ISSUE_INDEX_WRITES);
        if (!getIndexLock())
        {
            log.error("Could not reindex: " + issuesIterable.toString());
            return -1;
        }

        try
        {
            await(issueIndexer.reindexIssues(issuesIterable, context, reIndexComments, reIndexChangeHistory, false));
        }
        finally
        {
            releaseIndexLock();
            // Releasing the lock should never throw an exception - so we are safe to flush the
            // seacher without another try/finally here.
            flushThreadLocalSearchers();
            if (updateReplicatedIndexStore)
            {
                replicatedIndexManager.reindexIssues(issuesIterable);
            }
            opTimer.end();
        }

        final long totalTime = opTimer.snapshot().getMillisecondsTaken();
        if (log.isDebugEnabled())
        {
            log.debug("Reindexed " + issuesIterable.size() + " issues in " + totalTime + "ms.");
        }
        eventPublisher.publish(new ReindexIssuesCompletedEvent(totalTime));
        return totalTime;
    }

    @Override
    public long reIndexComments(Collection<Comment> comments) throws IndexException
    {
        return reIndexComments(comments, Contexts.nullContext());
    }

    @Override
    public long reIndexComments(Collection<Comment> comments, Context context) throws IndexException
    {
        return reIndexComments(comments, context, true);
    }

    @Override
    public long reIndexComments(final Collection<Comment> comments, final Context context, final boolean updateReplicatedIndexStore)
            throws IndexException
    {
        Assertions.notNull("comments", comments);
        Assertions.notNull("context", context);

        eventPublisher.publish(new ReindexIssuesStartedEvent());
        final OpTimer opTimer = Instrumentation.pullTimer(InstrumentationName.ISSUE_INDEX_WRITES);
        if (!getIndexLock())
        {
            log.error("Could not reindex: " + comments.toString());
            return -1;
        }

        try
        {
            await(issueIndexer.reindexComments(comments, context));
        }
        finally
        {
            releaseIndexLock();
            // Releasing the lock should never throw an exception - so we are safe to flush the
            // seacher without another try/finally here.
            flushThreadLocalSearchers();
            if (updateReplicatedIndexStore)
            {
                replicatedIndexManager.reindexComments(comments);
            }
            opTimer.end();
        }

        final long totalTime = opTimer.snapshot().getMillisecondsTaken();
        if (log.isDebugEnabled())
        {
            log.debug("Reindexed " + comments.size() + " comments in " + totalTime + "ms.");
        }
        eventPublisher.publish(new ReindexIssuesCompletedEvent(totalTime));
        return totalTime;
    }

    private int getCommentCount()
    {
        EntityCondition condition = new EntityFieldMap(FieldMap.build("type", ActionConstants.TYPE_COMMENT), EntityOperator.AND);
        List<GenericValue> commentCount = ofBizDelegator.findByCondition("ActionCount", condition, ImmutableList.of("count"), Collections.<String>emptyList());
        if (commentCount != null && commentCount.size() == 1)
        {
            GenericValue commentCountGV = commentCount.get(0);
            return commentCountGV.getLong("count").intValue();
        }
        else
        {
            throw new DataAccessException("Unable to access the count for the Action table");
        }
    }

    public boolean isIndexConsistent()
    {
        try
        {
            return IndexConsistencyUtils.isIndexConsistent("Issue", size(), issueSearcherSupplier)
                    && IndexConsistencyUtils.isIndexConsistent("Comment", getCommentCount(), commentSearcherSupplier)
                    && IndexConsistencyUtils.isIndexConsistent("ChangeHistory", -1, changeHistorySearcherSupplier);
        }
        catch (Exception ex)
        {
            // Any exception suggests that we should probably do a full re-index just to be safe
            log.warn("Exception during index consistency check: " + ex);
            return false;
        }
    }

    public int size()
    {
        return new DatabaseIssuesIterable(ofBizDelegator, getIssueFactory()).size();
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    public long optimize()
    {
        if (!isIndexAvailable())
        {
            return 0;
        }
        if (!getIndexLock())
        {
            return -1;
        }
        try
        {
            return optimize0();
        }
        finally
        {
            releaseIndexLock();
        }
    }

    /**
     * Optimizes the index and resets the dirtyness count. Should only be called if the index read lock is obtained.
     *
     * @return optimization time in milliseconds
     */
    @GuardedBy("index read lock")
    private long optimize0()
    {
        // JRA-23490 we want to reset the update count just before the optimization starts so that newly indexed issues
        // (if any) are counted towards next scheduled optimization
        final long startTime = System.currentTimeMillis();
        // do not timeout on optimize
        issueIndexer.optimize().await();
        return System.currentTimeMillis() - startTime;
    }

    public void deIndex(final Issue issue) throws IndexException
    {
        deIndexIssueObjects(Sets.newHashSet(issue), true);
    }

    @Override
    public void deIndexIssueObjects(final Set<Issue> issuesToDelete, final boolean updateReplicatedIndexStore)
            throws IndexException
    {
        if (issuesToDelete == null || issuesToDelete.isEmpty())
        {
            return;
        }
        if (!getIndexLock())
        {
            log.error("Could not deindex: " + issuesToDelete.iterator().next().getKey());
            return;
        }

        try
        {
            await(issueIndexer.deindexIssues(new IssueObjectIssuesIterable(issuesToDelete), Contexts.nullContext()));
        }
        finally
        {
            releaseIndexLock();
            // Releasing the lock should never throw an exception - so we are safe to flush the
            // searcher without another try/finally here.
            flushThreadLocalSearchers();
            if (updateReplicatedIndexStore)
            {
                replicatedIndexManager.deIndexIssues(issuesToDelete);
            }
        }
    }

    public void deIndex(final GenericValue entity) throws IndexException
    {
        if (!"Issue".equals(entity.getEntityName()))
        {
            log.error("Entity is not an issue " + entity.getEntityName());
            return;
        }
        deIndex(getIssueFactory().getIssue(entity));
    }

    private void await(final Index.Result result)
    {
        obtain(new Awaitable()
        {
            public boolean await(final long time, final TimeUnit unit) throws InterruptedException
            {
                return result.await(time, unit);
            }
        });
    }

    @VisibleForTesting
    void releaseIndexLock()
    {
        indexLock.readLock.unlock();
    }

    /**
     * @return true if got the lock, false otherwise
     */
    @VisibleForTesting
    boolean getIndexLock()
    {
        if (StringUtils.isBlank(indexPathManager.getIndexRootPath()))
        {
            log.error("File path not set - not indexing");
            return false;
        }

        // Attempt to acquire read lock on index operations.
        return indexLock.readLock.tryLock();
    }

    private boolean obtain(final Awaitable waitFor)
    {
        try
        {
            if (waitFor.await(indexConfig.getIndexLockWaitTime(), TimeUnit.MILLISECONDS))
            {
                return true;
            }
        }
        catch (final InterruptedException ie)
        {
            log.error("Wait attempt interrupted.", new IndexException("Wait attempt interrupted.", ie));
            return false;
        }
        // We failed to acquire a lock after waiting the configured time (default=30s), so give up.
        final String errorMessage = "Wait attempt timed out - waited " + indexConfig.getIndexLockWaitTime() + " milliseconds";
        log.error(errorMessage, new IndexException(errorMessage));
        return false;
    }

    public String getPluginsRootPath()
    {
        return indexPathManager.getPluginIndexRootPath();
    }

    public List<String> getExistingPluginsPaths()
    {
        final File pluginRootPath = new File(getPluginsRootPath());

        if (pluginRootPath.exists() && pluginRootPath.isDirectory() && pluginRootPath.canRead())
        {
            final String[] listing = pluginRootPath.list();
            // Find all sub-directories of the plugins root path, as each plugin should have its own
            // sub-directory under the root plugin index plath
            if (listing != null)
            {
                final List<String> subdirs = new ArrayList<String>();
                for (final String element : listing)
                {
                    final File f = new File(pluginRootPath, element);
                    if (f.exists() && f.canRead() && f.isDirectory())
                    {
                        subdirs.add(f.getAbsolutePath());
                    }
                }
                return Collections.unmodifiableList(subdirs);
            }
        }

        return Collections.emptyList();
    }

    public Collection<String> getAllIndexPaths()
    {
        final List<String> paths = new ArrayList<String>();
        paths.addAll(issueIndexer.getIndexPaths());
        paths.addAll(getExistingPluginsPaths());
        return Collections.unmodifiableList(paths);
    }

    public IndexSearcher getIssueSearcher()
    {
        if (!getIndexLock())
        {
            throw new SearchUnavailableException(null, indexConfig.isIndexAvailable());
        }
        try
        {
            return SearcherCache.getThreadLocalCache().retrieveIssueSearcher(issueSearcherSupplier);
        }
        finally
        {
            releaseIndexLock();
        }
    }

    public IndexSearcher getCommentSearcher()
    {
        if (!getIndexLock())
        {
            throw new SearchUnavailableException(null, indexConfig.isIndexAvailable());
        }
        try
        {
            return SearcherCache.getThreadLocalCache().retrieveCommentSearcher(commentSearcherSupplier);
        }
        finally
        {
            releaseIndexLock();
        }
    }

    public IndexSearcher getChangeHistorySearcher()
    {
        if (!getIndexLock())
        {
            throw new SearchUnavailableException(null, indexConfig.isIndexAvailable());
        }
        try
        {
            return SearcherCache.getThreadLocalCache().retrieveChangeHistorySearcher(changeHistorySearcherSupplier);
        }
        finally
        {
            releaseIndexLock();
        }
    }

    public void shutdown()
    {
        eventPublisher.publish(new IndexingShutdownEvent());
        flushThreadLocalSearchers();
        issueIndexer.shutdown();
    }

    IssueFactory getIssueFactory()
    {
        // the reason that this is not done in the constructor is that IssueFactory depends on IssueLinkManager which
        // depends on IssueIndexManager
        // and therefore is a cyclic dependency
        return ComponentAccessor.getComponentOfType(IssueFactory.class);
    }

    IssueBatcherFactory getIssueBatcherFactory()
    {
        // the reason that this is not done in the constructor is that IssueBatcherFactory depends on IssueFactory that
        // depends on IssueLinkManager which depends on IssueIndexManager and therefore is a cyclic dependency
        return ComponentAccessor.getComponent(IssueBatcherFactory.class);
    }

    @Override
    public String toString()
    {
        return "DefaultIndexManager: paths: " + getAllIndexPaths();
    }

    private void doBackgroundReindex(Context context, boolean reIndexComments, boolean reIndexChangeHistory) throws InterruptedException
    {
        StopWatch watch = new StopWatch();
        watch.start();

        // Get all the currently indexed issues.  These are in a sorted array
        final IssueIndexHelper issueIndexHelper = new IssueIndexHelper(issueManager, issueIndexer, getIssueFactory());
        long[] indexedIssues = issueIndexHelper.getAllIssueIds();

        IndexReconciler reconciler = new IndexReconciler(indexedIssues);
        AccumulatingResultBuilder resultBuilder = new AccumulatingResultBuilder();

        log.info("Reindexing " + indexedIssues.length + " issues in the background.");

        TaskDescriptor<Serializable> currentTaskDescriptor = taskManager.getLiveTask(new IndexTaskContext());

        // Create a listener to capture concurrent changes
        final BackgroundIndexListener backgroundIndexListener = new BackgroundIndexListener();
        eventPublisher.register(backgroundIndexListener);
        try
        {
            // Index the issues one batch at a time.  This stops various database drivers sucking all issues into
            // memory at once.
            IssuesBatcher batcher = getIssueBatcherFactory().getBatcher(reconciler);
            for (IssuesIterable batchOfIssues : batcher)
            {
                TaskDescriptor<Serializable> taskDescriptor = taskManager.getTask(currentTaskDescriptor.getTaskId());
                if (taskDescriptor != null && taskDescriptor.isCancelled())
                {
                    break;
                }
                resultBuilder.add(issueIndexer.reindexIssues(batchOfIssues, context, reIndexComments, reIndexChangeHistory, false));
            }

            // Wait on the intermediate result
            resultBuilder.toResult().await();
        }
        finally
        {
            eventPublisher.unregister(backgroundIndexListener);
            log.info(indexedIssues.length + " issues reindexed in the background, in " + watch.getTime() + " millis.");
            watch.split();

            issueIndexHelper.fixupConcurrentlyIndexedIssues(context, resultBuilder, backgroundIndexListener, reIndexComments, reIndexChangeHistory);

            log.info("" + backgroundIndexListener.getTotalModifications() + " concurrently modified issues reindexed in " + (watch.getTime() - watch.getSplitTime()) + " millis.");
            watch.split();
        }

        TaskDescriptor<Serializable> taskDescriptor = taskManager.getTask(currentTaskDescriptor.getTaskId());
        if (taskDescriptor != null && taskDescriptor.isCancelled())
        {
            // If we are cancelled we don't want to proceed further and look for rare corruptions.
            log.info("Background reindex cancelled.");
            throw new InterruptedException();
        }

        issueIndexHelper.fixupIndexCorruptions(resultBuilder, reconciler);

        log.info("Reindexing " + getIssueSearcher().getIndexReader().numDocs() + " issues in the background completed in " + watch.getTime() + " millis");
    }

    private void doStopTheWorldReindex(Context context)
    {
        // Stop the scheduler if it is running
        final LifecycleAwareSchedulerService schedulerService = ComponentAccessor.getComponent(LifecycleAwareSchedulerService.class);
        boolean restartScheduler = false;
        try
        {
            try
            {
                if (schedulerService.getState() == LifecycleAwareSchedulerService.State.STARTED)
                {
                    schedulerService.standby();
                    restartScheduler = true;
                }
            }
            catch (final SchedulerServiceException e)
            {
                log.warn("Unable to place the scheduler service in standby mode during reindex", e);
            }

            // Recreate the index as we are about to reindex all issues
            issueIndexer.deleteIndexes();
            doIndexIssuesInBatchMode(context);

            // optimise logic, passes 'true' for 'recreateIndex', which forces the optimize
            optimize0();
        }
        finally
        {
            // reactivate the scheduler if we need to
            if (restartScheduler)
            {
                try
                {
                    schedulerService.start();
                }
                catch (final SchedulerServiceException e)
                {
                    log.error("Unable to restart the scheduler after reindex", e);
                }
            }
        }
    }

    private void doIndexIssuesInBatchMode(Context context)
    {
        // Index the issues one batch at a time.  This stops various database drivers sucking all issues into
        // memory at once.
        AccumulatingResultBuilder resultBuilder = new AccumulatingResultBuilder();
        IssuesBatcher batcher = getIssueBatcherFactory().getBatcher();
        for (IssuesIterable batchOfIssues : batcher)
        {
            // do not timeout on reindexAll
            resultBuilder.add(issueIndexer.indexIssuesBatchMode(batchOfIssues, context));
        }
        resultBuilder.toResult().await();
    }

    public static void flushThreadLocalSearchers()
    {
        try
        {
            SearcherCache.getThreadLocalCache().closeSearchers();
        }
        catch (final IOException e)
        {
            log.error("Error while resetting searcher: " + e, e);
        }
    }

    private interface Awaitable
    {
        /**
         * See if we can wait successfully for this thing.
         * @param time how long to wait
         * @param unit the unit in which time is specified
         * @return true if the thing was obtained.
         * @throws InterruptedException if someone hits the interrupt button
         */
        boolean await(long time, TimeUnit unit) throws InterruptedException;
    }

    class ProjectBatcher implements IssuesBatcher
    {
        private final OfBizDelegator delegator;
        private final ImmutableList<Project> projects;
        private final IssueFactory issueFactory;

        ProjectBatcher()
        {
            this(ofBizDelegator, getIssueFactory());
        }

        @VisibleForTesting
        ProjectBatcher(OfBizDelegator delegator, IssueFactory issueFactory)
        {
            this.delegator = delegator;
            this.issueFactory = issueFactory;
            this.projects = ImmutableList.copyOf(projectManager.getProjectObjects());
        }

        @Override
        public Iterator<IssuesIterable> iterator()
        {
            return new ProjectsIterator();
        }

        class ProjectsIterator extends AbstractIterator<IssuesIterable>
        {
            private final UnmodifiableIterator<Project> projectsIt;

            ProjectsIterator()
            {
                projectsIt = projects.iterator();
            }

            @Override
            protected IssuesIterable computeNext()
            {
                if (!projectsIt.hasNext())
                {
                    return endOfData();
                }

                // read the project's issues
                Project project = projectsIt.next();
                EntityCondition condition = new EntityExpr(IssueFieldConstants.PROJECT, EQUALS, project.getId());

                return new DatabaseIssuesIterable(delegator, issueFactory, condition);
            }
        }
    }

    /**
     * Holds the index read/write locks.
     */
    private class IndexLocks
    {
        /**
         * Internal lock. Not to be used by clients.
         */
        private final ReadWriteLock indexLock = new ReentrantReadWriteLock();

        /**
         * The index read lock. This lock needs to be acquired when updating the index (i.e. adding to it or updating
         * existing documents in the index).
         */
        final IndexLock readLock = new IndexLock(indexLock.readLock());

        /**
         * The index write lock. This lock needs to be acquired only when a "stop the world" reindex is taking place and
         * the entire index is being deleted and re-created.
         */
        final IndexLock writeLock = new IndexLock(indexLock.writeLock());
    }

    /**
     * An index lock that can be acquired using a configurable time out.
     */
    private final class IndexLock
    {
        private final Lock lock;

        private IndexLock(Lock lock)
        {
            this.lock = notNull("lock", lock);
        }

        /**
         * Tries to acquire this lock using a timeout of {@link IndexingConfiguration#getIndexLockWaitTime()}
         * milliseconds.
         *
         * @return a boolean indicating whether the lock was acquired within the timeout
         */
        public boolean tryLock()
        {
            return obtain(new Awaitable()
            {
                public boolean await(final long time, final TimeUnit unit) throws InterruptedException
                {
                    return lock.tryLock(time, unit);
                }
            });
        }

        /**
         * Unlocks this lock.
         */
        public void unlock()
        {
            lock.unlock();
        }
    }

}
