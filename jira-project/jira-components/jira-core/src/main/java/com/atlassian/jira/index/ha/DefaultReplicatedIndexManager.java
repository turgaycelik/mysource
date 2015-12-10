package com.atlassian.jira.index.ha;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.cluster.ClusterNodeProperties;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IndexTaskContext;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.index.ReindexAllCompletedEvent;
import com.atlassian.jira.issue.index.ReindexAllStartedEvent;
import com.atlassian.jira.issue.util.IssuesIterable;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.util.Consumer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.apache.log4j.Logger;

/**
 * Manages the replicated index.
 *
 * @since v6.1
 */
@EventComponent
public class DefaultReplicatedIndexManager implements ReplicatedIndexManager
{
    private static final Logger log = Logger.getLogger(DefaultReplicatedIndexManager.class);

    private final OfBizReplicatedIndexOperationStore ofBizReplicatedIndexOperationStore;
    private final TaskManager taskManager;

    public DefaultReplicatedIndexManager(final OfBizReplicatedIndexOperationStore ofBizReplicatedIndexOperationStore, final TaskManager taskManager)
    {
        this.ofBizReplicatedIndexOperationStore = ofBizReplicatedIndexOperationStore;
        this.taskManager = taskManager;
    }

    /**
     *  Reindexes the set of provided issues in the repliccated index.
     *
     * @param issuesIterable  an {@Link IssuesIterable} that iterates obver the issues to be reindexed
     */
    @Override
    public void reindexIssues(@Nonnull final IssuesIterable issuesIterable)
    {
        try
        {
            final Set<Long> issueIds = Sets.newHashSet();
            issuesIterable.foreach(new Consumer<Issue>()
            {
                @Override
                public void consume(@Nonnull Issue issue)
                {
                    issueIds.add(issue.getId());
                }
            });
            updateReplicatedIndex(issueIds, ReplicatedIndexOperation.AffectedIndex.ISSUE,
                    ReplicatedIndexOperation.SharedEntityType.NONE, ReplicatedIndexOperation.Operation.UPDATE);
        }
        catch (Exception e)
        {
            log.error(buildErrorMessage("reindexed issues"), e);
        }
    }

    /**
     *  Reindexes the set of provided issues in the repliccated index.
     *
     * @param comments  A collection of {@Link Comment} to reindex
     */
    @Override
    public void reindexComments(final Collection<Comment> comments)
    {
        try
        {
            final Set<Long> commentIds = Sets.newHashSet();
            for(Comment comment : comments)
            {
                commentIds.add(comment.getId());
            }
            updateReplicatedIndex(commentIds, ReplicatedIndexOperation.AffectedIndex.COMMENT,
                    ReplicatedIndexOperation.SharedEntityType.NONE, ReplicatedIndexOperation.Operation.UPDATE);
        }
        catch (Exception e)
        {
            log.error(buildErrorMessage("reindexed comments"));
        }
    }

    /**
     * Removes the issues from the replicated index.
     *
     * @param issuesToDelete  the {@Link Issue}s to mark as deleted.
     */
    @Override
    public void deIndexIssues(final Set<Issue> issuesToDelete)
    {
        try
        {
            final Set<Long> issueIds = Sets.newHashSet();
            if (issuesToDelete != null)
            {
                for (Issue issue : issuesToDelete)
                {
                    issueIds.add(issue.getId());
                }
            }
            updateReplicatedIndex(issueIds, ReplicatedIndexOperation.AffectedIndex.ISSUE,
                    ReplicatedIndexOperation.SharedEntityType.NONE, ReplicatedIndexOperation.Operation.DELETE);
        }
        catch (Exception e)
        {
            log.error(buildErrorMessage("deindexed issues"), e);
        }
    }

    @Override
    public void reindexProject(final Project project)
    {
        updateReplicatedIndex(ImmutableSet.of(project.getId()), ReplicatedIndexOperation.AffectedIndex.ISSUE,
                ReplicatedIndexOperation.SharedEntityType.NONE, ReplicatedIndexOperation.Operation.PROJECT_REINDEX);
    }

    /**
     * Reindexes the shared entity in the replicated index.
     *
     * @param entity  the {@Link SharedEntity} to be reindexed
     */
    @Override
    public void indexSharedEntity(SharedEntity entity)
    {
        try
        {
            updateReplicatedIndex(ImmutableSet.of(entity.getId()), ReplicatedIndexOperation.AffectedIndex.SHAREDENTITY,
                    ReplicatedIndexOperation.SharedEntityType.fromTypeDescriptor(entity.getEntityType()),
                    ReplicatedIndexOperation.Operation.UPDATE);
        }
        catch (Exception e)
        {
            log.error(buildErrorMessage("reindexed shared entity"), e);
        }
    }

    /**
     * Deindexes the shared entity in the replicated index.
     *
     * @param entity  the {@Link SharedEntity} to be removed
     */
    @Override
    public void deIndexSharedEntity(SharedEntity entity)
    {
        try
        {
            updateReplicatedIndex(ImmutableSet.of(entity.getId()), ReplicatedIndexOperation.AffectedIndex.SHAREDENTITY,
                    ReplicatedIndexOperation.SharedEntityType.fromTypeDescriptor(entity.getEntityType()),
                    ReplicatedIndexOperation.Operation.DELETE);
        }
        catch (Exception e)
        {
            log.error(buildErrorMessage("deindexed shared entity"), e);
        }
    }

    @EventListener
    public void onReindexAllStarted(ReindexAllStartedEvent reindexAllStartedEvent)
    {
        if (reindexAllStartedEvent.shouldUpdateReplicatedIndex()) {
            ReplicatedIndexOperation.Operation  reindex;
            if (reindexAllStartedEvent.isUsingBackgroundIndexing())
            {
                reindex = ReplicatedIndexOperation.Operation.BACKGROUND_REINDEX_START;
            }
            else
            {
                reindex = ReplicatedIndexOperation.Operation.FULL_REINDEX_START;
            }
            ofBizReplicatedIndexOperationStore.createIndexOperation(new Timestamp(System.currentTimeMillis()),
                    ReplicatedIndexOperation.AffectedIndex.ALL,
                    ReplicatedIndexOperation.SharedEntityType.NONE,
                    reindex, Collections.<Long>emptySet(), "");
        }
    }

    @EventListener
    public void onReindexAllCompleted(ReindexAllCompletedEvent reindexAllCompletedEvent)
    {
        if (reindexAllCompletedEvent.shouldUpdateReplicatedIndex()) {
            ReplicatedIndexOperation.Operation  reindex;
            if (reindexAllCompletedEvent.isUsingBackgroundIndexing())
            {
                reindex = ReplicatedIndexOperation.Operation.BACKGROUND_REINDEX_END;
            }
            else
            {
                reindex = ReplicatedIndexOperation.Operation.FULL_REINDEX_END;
            }
            // Circular dependencies force us to access via ComponentAccessor. Potentially we could replace direct calls
            // to the ReplicatedIndexManager with events & event handlers.
            final ClusterNodeProperties clusterNodeProperties = ComponentAccessor.getComponent(ClusterNodeProperties.class);
            final IndexCopyService indexCopyService = ComponentAccessor.getComponent(IndexCopyService.class);
            final String backupFilename = indexCopyService.backupIndex(clusterNodeProperties.getNodeId());
            ofBizReplicatedIndexOperationStore.createIndexOperation(new Timestamp(System.currentTimeMillis()),
                    ReplicatedIndexOperation.AffectedIndex.ALL,
                    ReplicatedIndexOperation.SharedEntityType.NONE,
                    reindex, Collections.<Long>emptySet(), backupFilename);
        }
        else
        {
            TaskDescriptor taskDescriptor = taskManager.getLiveTask(new IndexTaskContext());
            if (taskDescriptor != null && taskDescriptor.isFinished())
            {
                taskManager.removeTask(taskDescriptor.getTaskId());
            }
        }
    }


    private void updateReplicatedIndex(final Set<Long> ids, final ReplicatedIndexOperation.AffectedIndex affectedIndex,
            final ReplicatedIndexOperation.SharedEntityType entityType, final ReplicatedIndexOperation.Operation operation)
    {
        ofBizReplicatedIndexOperationStore.createIndexOperation(new Timestamp(System.currentTimeMillis()),
                affectedIndex, entityType, operation, ids, "");
    }

    private String buildErrorMessage(String indexOperationInError)
    {
        return String.format("Caught an exception trying to replicate %s.  The replicated index may or may not be correct", indexOperationInError);
    }
}
