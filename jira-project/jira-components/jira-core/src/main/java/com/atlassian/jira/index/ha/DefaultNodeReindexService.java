package com.atlassian.jira.index.ha;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.Nullable;

import com.atlassian.instrumentation.AtomicCounter;
import com.atlassian.instrumentation.Counter;
import com.atlassian.instrumentation.ExternalGauge;
import com.atlassian.instrumentation.ExternalValue;
import com.atlassian.jira.bc.project.index.ProjectReindexService;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.Node;
import com.atlassian.jira.instrumentation.Instrumentation;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.index.SharedEntityIndexer;
import com.atlassian.jira.task.AlreadyExecutingException;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.util.concurrent.ThreadFactories;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.instrumentation.InstrumentationName.CLUSTER_REPLICATED_INDEX_OPERATIONS_LATEST;
import static com.atlassian.jira.instrumentation.InstrumentationName.CLUSTER_REPLICATED_INDEX_OPERATIONS_TOTAL;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Runs periodically and reindexes issues that have been indexed in other nodes
 *
 * @since v6.1
 */
public class DefaultNodeReindexService implements NodeReindexService
{
    private static final Logger log = Logger.getLogger(DefaultNodeReindexService.class);

    private final ClusterManager clusterManager;
    private final OfBizNodeIndexCounterStore ofBizNodeIndexCounterStore;
    private final OfBizReplicatedIndexOperationStore ofBizNodeIndexOperationStore;
    private final IssueIndexManager indexManager;
    private final SharedEntityIndexer sharedEntityIndexer;
    private final ProjectManager projectManager;
    private final ProjectReindexService projectReindexService;
    private final IssueManager issueManager;
    private final CommentManager commentManager;
    private final OfBizDelegator ofBizDelegator;
    private final SharedEntityResolver sharedEntityResolver;
    private final IndexCopyService indexCopyService;

    public static final String ISSUE_ENTITY = "Issue";

    private final Runnable indexer = new Runnable()
    {
        public void run()
        {
            reIndex();
        }
    };

    @Nullable
    private final ScheduledExecutorService scheduler;
    private final LatestGaugeValue latestGaugeValue = new LatestGaugeValue();
    private final Counter totalOperationCountInstrument;
    @Nullable
    private ScheduledFuture<?> indexerService;
    private static final int INITIAL_DELAY = 10;
    private static final int PERIOD = 5;

    public DefaultNodeReindexService(final ClusterManager clusterManager, final OfBizNodeIndexCounterStore ofBizNodeIndexCounterStore,
            final OfBizReplicatedIndexOperationStore ofBizNodeIndexOperationStore, final IssueIndexManager indexManager,
            SharedEntityIndexer sharedEntityIndexer, final ProjectManager projectManager, final ProjectReindexService projectReindexService, final IssueManager issueManager, final CommentManager commentManager,
            final OfBizDelegator ofBizDelegator, final SharedEntityResolver sharedEntityResolver, final IndexCopyService indexCopyService)
    {
        this.clusterManager = clusterManager;
        this.ofBizNodeIndexCounterStore = ofBizNodeIndexCounterStore;
        this.ofBizNodeIndexOperationStore = ofBizNodeIndexOperationStore;
        this.indexManager = indexManager;
        this.sharedEntityIndexer = sharedEntityIndexer;
        this.projectManager = projectManager;
        this.projectReindexService = projectReindexService;
        this.issueManager = issueManager;
        this.commentManager = commentManager;
        this.ofBizDelegator = ofBizDelegator;
        this.sharedEntityResolver = sharedEntityResolver;
        this.indexCopyService = indexCopyService;

        final ExternalGauge operationCountInstrument;
        if (clusterManager.isClustered())
        {
            scheduler = Executors.newScheduledThreadPool(1, ThreadFactories.namedThreadFactory("NodeReindexServiceThread"));
            totalOperationCountInstrument = new AtomicCounter(CLUSTER_REPLICATED_INDEX_OPERATIONS_TOTAL.getInstrumentName());
            operationCountInstrument = new ExternalGauge(CLUSTER_REPLICATED_INDEX_OPERATIONS_LATEST.getInstrumentName(), latestGaugeValue);
            Instrumentation.putInstrument(totalOperationCountInstrument);
            Instrumentation.putInstrument(operationCountInstrument);
        }
        else
        {
            scheduler = null;
            totalOperationCountInstrument = null;
        }
    }

    @Override
    public void cancel() {
        pause();
        if (scheduler != null)
        {
            scheduler.shutdownNow();
        }
    }

    @Override
    public synchronized void start()
    {
        if (scheduler != null)
        {
            if (indexerService == null)
            {
                indexerService = scheduler.scheduleWithFixedDelay(indexer, INITIAL_DELAY, PERIOD, SECONDS);
            }
            else if (log.isDebugEnabled()) // Avoid stacktrace creation unless needed
            {
                log.debug("Start called on NodeReindexService when already running", new IllegalStateException());
            }
        }
    }

    @Override
    public synchronized void pause()
    {
        if (indexerService != null)
        {
            indexerService.cancel(true);
            indexerService = null;
        }
    }

    @Override
    public void restart()
    {
        pause();
        start();
    }

    @Override
    public void resetIndexCount()
    {
        // We reset all node counters including one for the current node, which will be used when replaying changes
        // after a full reindex or index copy.
        final String currentNodeId = getCurrentNodeId();
        for (Node node : clusterManager.getAllNodes())
        {
            Long lastId = ofBizNodeIndexOperationStore.getLatestOperation(node.getNodeId());
            if (lastId != null)
            {
                ofBizNodeIndexCounterStore.storeHighestIdForNode(currentNodeId, node.getNodeId(), lastId);
            }
        }
    }

    @Override
    public boolean canIndexBeRebuilt()
    {
        final String currentNodeId = getCurrentNodeId();
        for (Node node : clusterManager.getAllNodes())
        {
            if (node.isClustered() && !node.getNodeId().equals(currentNodeId))
            {
                // I get the last operation between the other node and this one.
                long lastIndexed = getCurrentIndexCount(currentNodeId, node.getNodeId());

                // If the operation does not exist in the replication operations table means
                // that no merge of indexes occurred between those 2 nodes.
                // If there is, I need to know how old is that record.
                if (!ofBizNodeIndexOperationStore.contains(lastIndexed))
                {
                    // If there are no rows for this node, then it has never done any work and we don't need to care
                    // about it. We never purge all rows so once a row has been added, then one should always remain.
                    Long latestOperation = ofBizNodeIndexOperationStore.getLatestOperation(node.getNodeId());
                    if (latestOperation != null)
                    {
                        return false;
                    }
                }
            }
        }

        // If all the checks were ok means we can apply the deltas. We check also if the filesystem reflects
        // the information in the db. This call has a 10% tolerance that is good enough for the deltas to rebuild
        return indexManager.isIndexConsistent();
    }

    @Override
    public void replayLocalOperations()
    {
        // in theory we are still in the cluster, but we may implement dynamic addition/removal at some point
        if (scheduler != null)
        {
            // All the interesting work should happen in the NodeReindexService thread
            scheduler.submit(new Runnable() {
                public void run()
                {
                    try
                    {
                        final String nodeId = getCurrentNodeId();
                        final Set<ReplicatedIndexOperation> indexOps = ofBizNodeIndexOperationStore
                                .getIndexOperationsAfter(nodeId, getCurrentIndexCount(nodeId, nodeId));

                        if (!indexOps.isEmpty())
                        {
                            try
                            {
                                updateAffectedIndexes(indexOps);
                            }
                            finally
                            {
                                updateIndexCount(indexOps);
                            }
                        }
                    }
                    catch (final Exception e) //catch everything here to make sure it doesn't bring scheduled service down.
                    {
                        log.error("Error re-indexing node changes", e);
                    }
                }
            });
        }
    }

    @VisibleForTesting
    ScheduledFuture<?> getIndexerService()
    {
        return indexerService;
    }

    private void reIndex()
    {
        // in theory we are still in the cluster, but we may implement dynamic addition/removal at some point
        final String currentNodeId = getCurrentNodeId();
        if (currentNodeId == null)
        {
            return;
        }

        try
        {
            final Set<ReplicatedIndexOperation> allIndexOps = Sets.newHashSet();
            final Set<Node> allNodes = clusterManager.getAllNodes();
            for (Node node : allNodes)
            {
                if (!currentNodeId.equals(node.getNodeId()))
                {
                    final Set<ReplicatedIndexOperation> indexOps = ofBizNodeIndexOperationStore.getIndexOperationsAfter(
                            node.getNodeId(),
                            getCurrentIndexCount(currentNodeId, node.getNodeId()) );
                    allIndexOps.addAll(indexOps);
                }
            }

            if (!allIndexOps.isEmpty())
            {
                try
                {
                    updateAffectedIndexes(allIndexOps);
                }
                finally
                {
                    updateIndexCount(allIndexOps);
                }
            }
            totalOperationCountInstrument.addAndGet(allIndexOps.size());
            latestGaugeValue.setValue(allIndexOps.size());
        }
        catch (final Throwable e) //catch everything here to make sure it doesn't bring scheduled service down.
        {
            log.error("Error re-indexing node changes", e);
        }
    }

    private void updateIndexCount(Set<ReplicatedIndexOperation> indexOps)
    {
        final String currentNodeId = getCurrentNodeId();
        Map<String, Long> highestNodeCounts = getHighestNodeCounts(indexOps);
        for (Map.Entry<String, Long> countEntry : highestNodeCounts.entrySet())
        {
            ofBizNodeIndexCounterStore.storeHighestIdForNode(currentNodeId, countEntry.getKey(), countEntry.getValue());
        }
    }

    private String getCurrentNodeId()
    {
        return clusterManager.getNodeId();
    }

    private long getCurrentIndexCount(String receivingNodeId, final String sendingNodeId)
    {
        return ofBizNodeIndexCounterStore.getIndexOperationCounterForNodeId(receivingNodeId, sendingNodeId);
    }

    private void updateAffectedIndexes(Set<ReplicatedIndexOperation> indexOps) throws IndexException
    {
        ReplicatedIndexOperation latestReindex = null;
        for (ReplicatedIndexOperation indexOp : indexOps)
        {
            if (indexOp.getOperation().isReindexEnd())
            {
                if (latestReindex == null || indexOp.getIndexTime().getTime() > latestReindex.getIndexTime().getTime())
                {
                    latestReindex = indexOp;
                }
            }
        }
        if (latestReindex != null)
        {
            // If there has been a reindex since we last caught up, we have no choice but to request/rebuild the index.
            resetIndexCount();
            indexCopyService.restoreIndex(latestReindex.getBackupFilename());
            return;
        }

        final Map<ReplicatedIndexOperation.AffectedIndex, Set<ReplicatedIndexOperation>> partitionedOperations = partition(indexOps);
        updateIssueIndex(partitionedOperations.get(ReplicatedIndexOperation.AffectedIndex.ISSUE));
        updateCommentsIndex(partitionedOperations.get(ReplicatedIndexOperation.AffectedIndex.COMMENT));
        updateSharedEntityIndex(partitionedOperations.get(ReplicatedIndexOperation.AffectedIndex.SHAREDENTITY));
    }

    private void updateSharedEntityIndex(final Set<ReplicatedIndexOperation> indexOps)
    {
        final Set<SharedEntity> entitiesToIndex = Sets.newHashSet();
        final Set<SharedEntity> entitiesToDelete = Sets.newHashSet();
        for (ReplicatedIndexOperation operation : indexOps)
        {
            final ReplicatedIndexOperation.Operation action =  operation.getOperation();
            switch (action)
            {
                case UPDATE:
                case CREATE:
                    entitiesToIndex.addAll(sharedEntityResolver.getSharedEntities(operation.getEntityType(), operation.getAffectedIds()));
                    break;
                case DELETE:
                    entitiesToDelete.addAll(sharedEntityResolver.getDummySharedEntities(operation.getEntityType(), operation.getAffectedIds()));
                    break;
            }
        }
        if (entitiesToIndex.size() > 0)
        {
            sharedEntityIndexer.index(entitiesToIndex, false).await();
        }
        if (entitiesToDelete.size() > 0)
        {
            sharedEntityIndexer.deIndex(entitiesToDelete, false).await();
        }
    }

    private void updateCommentsIndex(final Set<ReplicatedIndexOperation> indexOps) throws IndexException
    {
        final Set<Comment> commentsToIndex = Sets.newHashSet();
        for (ReplicatedIndexOperation operation : indexOps)
        {
            for (Long id : operation.getAffectedIds())
            {
                // JDEV-28627 - comment might get deleted before we process this edit for it
                final Comment comment = commentManager.getCommentById(id);
                if (comment != null)
                {
                    commentsToIndex.add(comment);
                }
            }
        }
        indexManager.reIndexComments(commentsToIndex, Contexts.nullContext(), false);
    }

    private void updateIssueIndex(final Set<ReplicatedIndexOperation> indexOps) throws IndexException
    {
        final Set<Project> projectsToUpdate = Sets.newHashSet();
        final Set<Issue> issuesToUpdate = Sets.newHashSet();
        // We use a tree set and our own comparator here because these are not real issues, just ids.
        final Set<Issue> issuesToDelete = Sets.newTreeSet(new Comparator<Issue>()
        {
            @Override
            public int compare(final Issue o1, final Issue o2)
            {
                return o1.getId().compareTo(o2.getId());
            }
        });

        for (ReplicatedIndexOperation operation : indexOps)
        {
            final ReplicatedIndexOperation.Operation action =  operation.getOperation();
            switch (action)
            {
                case UPDATE:
                case CREATE:
                    issuesToUpdate.addAll(issueManager.getIssueObjects(operation.getAffectedIds()));
                    break;
                case DELETE:
                    for (long id: operation.getAffectedIds())
                    {
                        // We need to use the gv here to create an in memory skeleton of the deleted issue.
                        GenericValue gv = ofBizDelegator.makeValue(ISSUE_ENTITY, ImmutableMap.<String, Object>of("id", id));
                        issuesToDelete.add(IssueImpl.getIssueObject(gv));
                    }
                    break;
                case PROJECT_REINDEX:
                    for (long id: operation.getAffectedIds())
                    {
                        // We need to use the gv here to create an in memory skeleton of the deleted issue.
                        Project project = projectManager.getProjectObj(id);
                        if (project != null)
                        {
                            projectsToUpdate.add(project);
                        }
                    }
                    break;
            }
        }
        if (issuesToUpdate.size() > 0)
        {
            // don't index comments do index change history
            indexManager.reIndexIssueObjects(issuesToUpdate, false, true, false);
        }
        if (issuesToDelete.size() > 0)
        {
            indexManager.deIndexIssueObjects(issuesToDelete, false);
        }
        for (Project project : projectsToUpdate)
        {
            reindexProject(project);
        }
    }

    private void reindexProject(Project project)
    {
        try
        {
            // Check this project is not already being re-indexed.
            if (projectReindexService.isReindexPossible(project))
            {
                projectReindexService.reindex(project, false);
            }
        }
        catch (AlreadyExecutingException aee)
        {
            // isReindexPossible should have caught this gracefully and dropped the request, but no matter...
            log.debug("Lost race detecting that project reindex for '" + project.getKey() + "' is already in progress", aee);
        }
    }

    private Map<ReplicatedIndexOperation.AffectedIndex, Set<ReplicatedIndexOperation>> partition(final Set<ReplicatedIndexOperation> indexOps)
    {
        final Map<ReplicatedIndexOperation.AffectedIndex, Set<ReplicatedIndexOperation>> partitionedOperations = Maps.newHashMap();
        for (ReplicatedIndexOperation.AffectedIndex affectedIndex : ReplicatedIndexOperation.AffectedIndex.values())
        {
            partitionedOperations.put(affectedIndex, Sets.<ReplicatedIndexOperation>newHashSet());
        }
        for (ReplicatedIndexOperation operation : indexOps)
        {
            partitionedOperations.get(operation.getAffectedIndex()).add(operation);
        }
        return partitionedOperations;
    }

    private Map<String, Long> getHighestNodeCounts(Iterable<ReplicatedIndexOperation> indexOperations)
    {
        Map<String, Long> highestOperationIds = new HashMap<String, Long>();

        for (ReplicatedIndexOperation indexOperation : indexOperations)
        {
            String nodeId = indexOperation.getNodeId();
            Long currentHigh = highestOperationIds.get(nodeId);
            if (currentHigh == null || currentHigh < indexOperation.getId())
            {
                highestOperationIds.put(nodeId, indexOperation.getId());
            }
        }
        return highestOperationIds;
    }

    private static class LatestGaugeValue implements ExternalValue
    {
        private long value = 0;

        public void setValue(final long value)
        {
            this.value = value;
        }

        @Override
        public long getValue()
        {
            return value;
        }
    }
}
