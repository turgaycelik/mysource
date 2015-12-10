package com.atlassian.jira.index.ha;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.core.util.DateUtils.DateRange;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.EntityListConsumer;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.index.IssueIndexHelper;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueBatcherFactory;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.index.IssueIndexer;
import com.atlassian.jira.issue.index.IssuesBatcher;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.util.FieldHitCollector;
import com.atlassian.jira.issue.util.IssueObjectIssuesIterable;
import com.atlassian.jira.issue.util.IssuesIterable;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.sharing.index.SharedEntityIndexManager;
import com.atlassian.jira.sharing.index.SharedEntityIndexer;
import com.atlassian.jira.task.CompositeProgressSink;
import com.atlassian.jira.task.LoggingProgressSink;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ZipUtils;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.util.index.IndexingCounterManager;
import com.atlassian.jira.web.action.admin.index.IndexCommandResult;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.order.SortOrder;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.issue.IssueFieldConstants.UPDATED;
import static java.util.Arrays.asList;

public class DefaultIndexRecoveryManager implements IndexRecoveryManager
{
    private static final Logger LOG = Logger.getLogger(DefaultIndexRecoveryManager.class);

    private final SearchProvider searchProvider;
    private final OfBizDelegator delegator;
    private final IssueBatcherFactory issueBatcherFactory;
    private final IssueManager issueManager;
    private final IssueIndexer issueIndexer;
    private final IndexLifecycleManager indexLifecycleManager;
    private final IndexPathManager indexPathManager;
    private final IssueFactory issueFactory;
    private final SharedEntityIndexManager sharedEntityIndexManager;
    private final IndexingCounterManager indexingCounterManager;
    private final IssueIndexManager indexManager;

    public DefaultIndexRecoveryManager(final SearchProvider searchProvider, final OfBizDelegator delegator, final IssueBatcherFactory issueBatcherFactory, final IssueManager issueManager, final IssueIndexer issueIndexer, final IndexLifecycleManager indexLifecycleManager, final IndexPathManager indexPathManager, final IssueFactory issueFactory,
            final SharedEntityIndexManager sharedEntityIndexManager, final IndexingCounterManager indexingCounterManager, final IssueIndexManager indexManager)
    {
        this.searchProvider = searchProvider;
        this.delegator = delegator;
        this.issueBatcherFactory = issueBatcherFactory;
        this.issueManager = issueManager;
        this.issueIndexer = issueIndexer;
        this.indexLifecycleManager = indexLifecycleManager;
        this.indexPathManager = indexPathManager;
        this.issueFactory = issueFactory;
        this.sharedEntityIndexManager = sharedEntityIndexManager;
        this.indexingCounterManager = indexingCounterManager;
        this.indexManager = indexManager;
    }

    @Override
    public IndexCommandResult recoverIndexFromBackup(final File recoveryFile, final TaskProgressSink taskProgressSink)
            throws IndexException
    {
        final File workDir = new File(indexPathManager.getIndexRootPath(), "JIRAIndexRestore");
        try
        {
            // Prepare the restore by exploding the backup to a temp directory outside of lock
            ZipUtils.unzip(recoveryFile, workDir);

            // Acquire the 'stop the world' reindex lock and stop indexing. This will block the acquisition of
            // index searchers until we're done restoring the index.
            TaskProgressSink compositeSink = new CompositeProgressSink(taskProgressSink, new LoggingProgressSink(LOG, "Recovering search indexes - {0}% complete...", 1));
            final long startTime = System.currentTimeMillis();

            final ReplaceIndexRunner runner = new ReplaceIndexRunner(workDir, compositeSink, indexLifecycleManager, indexPathManager, searchProvider, delegator);
            if(!indexManager.withReindexLock(runner))
            {
                throw new IndexException("Failed to acquire reindex lock");
            }

            if (runner.getDateRange() != null)
            {
                try
                {
                    reindexIssuesIn(runner.getDateRange(), compositeSink);
                }
                catch (IndexException e)
                {
                    throw new RuntimeException(e);
                }
                catch (SearchException e)
                {
                    throw new RuntimeException(e);
                }
            }
            compositeSink.makeProgress(80, "Recovering", "Recovered issue index");

            sharedEntityIndexManager.reIndexAll(Contexts.nullContext());

            indexingCounterManager.incrementValue();
            compositeSink.makeProgress(100, "Recovering", "Recovered all indexes");

            return new IndexCommandResult(System.currentTimeMillis() - startTime);
        }
        catch (IOException e)
        {
            throw new IndexException(e);
        }
        finally
        {
            FileUtils.deleteQuietly(workDir);
        }
    }

    private void reindexIssuesIn(final DateRange range, final TaskProgressSink taskProgressSink)
            throws IndexException, SearchException
    {
        // We need to reindex any issues that were updated between the 2 dates. How we do this depends on which is the latter date
        if (range.startDate.before(range.endDate))
        {
            try
            {
                reindexUsingDatabaseLatest(range);
            }
            catch (GenericEntityException e)
            {
                throw new RuntimeException(e);
            }
        }
        else if (range.startDate.after(range.endDate))
        {
            reindexUsingLucene(range, null);
        }
        taskProgressSink.makeProgress(60, "Recovering", "Recovered added and updated issues");
        // There may remain in the index some issues that have been deleted, we need to remove these, carefully.
        deIndexDeletedIssues();
        taskProgressSink.makeProgress(80, "Recovering", "Cleaned removed issues");
    }

    private void reindexUsingDatabaseLatest(final DateRange range)
            throws IndexException, GenericEntityException
    {
        final EntityCondition ge = new EntityExpr(UPDATED, EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(range.startDate.getTime()));
        final EntityCondition le = new EntityExpr(UPDATED, EntityOperator.LESS_THAN_EQUAL_TO, new Timestamp(range.endDate.getTime()));
        final EntityCondition condition = new EntityConditionList(asList(ge, le), EntityOperator.AND);

        Context context = Contexts.nullContext();

        IssuesBatcher batches = issueBatcherFactory.getBatcher(condition);
        for (IssuesIterable batch : batches)
        {
            issueIndexer.reindexIssues(batch, context, true, true, false);
        }
    }

    private void reindexUsingLucene(final DateRange range, final ApplicationUser user)
            throws SearchException, IndexException
    {
        FieldHitCollector collector = new FieldHitCollector(DocumentConstants.ISSUE_ID);
        final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
        queryBuilder.where().addDateRangeCondition(DocumentConstants.ISSUE_UPDATED, range.endDate, range.startDate);

        searchProvider.searchOverrideSecurity(queryBuilder.buildQuery(), user, collector);

        List<Long> issueIds = Lists.transform(collector.getValues(), new Function<String, Long>()
        {
            @Override
            public Long apply(@Nullable final String input)
            {
                return Long.valueOf(input);
            }
        });

        IssuesIterable batch = new NullAwareIssueIdsIssueIterable(issueIds, issueManager);
        issueIndexer.reindexIssues(batch, Contexts.nullContext(), true, true, false);
    }

    private void deIndexDeletedIssues() throws SearchException
    {
        // Collect all the IssueIds from Lucene
        final IssueIndexHelper issueIndexHelper = new IssueIndexHelper(issueManager, issueIndexer, issueFactory);
        final Set<Long> indexIssueIds = longsArrayToSet(issueIndexHelper.getAllIssueIds());

        final List<Long> dbIssueIds = Select.columns("id")
                .from(Entity.Name.ISSUE)
                .runWith(delegator)
                .consumeWith(createIssueIdsCollector());

        indexIssueIds.removeAll(dbIssueIds);

        // De-index the remaining
        for (final Long id : indexIssueIds)
        {
            final GenericValue gv = delegator.makeValue(Entity.Name.ISSUE, new FieldMap("id", id));
            final MutableIssue deletedIssue = issueFactory.getIssue(gv);
            final IssuesIterable issues = new IssueObjectIssuesIterable(Collections.singletonList(deletedIssue));
            issueIndexer.deindexIssues(issues, Contexts.nullContext());
        }
    }

    private Set<Long> longsArrayToSet(final long[] longs)
    {
        final Set<Long> set = Sets.newHashSetWithExpectedSize(longs.length);

        for (final long id : longs)
        {
            set.add(id);
        }

        return set;
    }

    private EntityListConsumer<GenericValue, List<Long>> createIssueIdsCollector()
    {
        return new EntityListConsumer<GenericValue, List<Long>>()
        {
            private final List<Long> issueIds = Lists.newArrayList();

            @Override
            public void consume(final GenericValue entity)
            {
                issueIds.add(entity.getLong("id"));
            }

            @Override
            public List<Long> result()
            {
                return issueIds;
            }
        };
    }
    
    @Override
    public int size()
    {
        return 100;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    private static class ReplaceIndexRunner implements Runnable
    {
        private final File workDir;
        private final TaskProgressSink taskProgressSink;
        private final IndexLifecycleManager indexLifecycleManager;
        private final IndexPathManager indexPathManager;
        private final SearchProvider searchProvider;
        private final OfBizDelegator delegator;
        private DateRange range;

        ReplaceIndexRunner(File workDir, TaskProgressSink taskProgressSink, IndexLifecycleManager indexLifecycleManager,
                IndexPathManager indexPathManager, SearchProvider searchProvider, OfBizDelegator delegator)
        {
            this.workDir = workDir;
            this.taskProgressSink = taskProgressSink;
            this.indexLifecycleManager = indexLifecycleManager;
            this.indexPathManager = indexPathManager;
            this.searchProvider = searchProvider;
            this.delegator = delegator;
            this.range = null;
        }

        @Override
        public void run()
        {
            taskProgressSink.makeProgress(1, "Restoring", "Replacing indexes");

            indexLifecycleManager.deactivate();
            removeIndexes();
            try
            {
                replaceIndexes(workDir);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                indexLifecycleManager.activate(Contexts.nullContext(), false);
            }

            taskProgressSink.makeProgress(20, "Restoring", "Restored index backup");

            range = getDurationToRecover();
        }

        public DateRange getDateRange()
        {
            return range;
        }

        private void removeIndexes()
        {
            final IssueIndexer indexIssuer = ComponentAccessor.getComponent(IssueIndexer.class);
            final SharedEntityIndexer sharedEntityIndexer = ComponentAccessor.getComponent(SharedEntityIndexer.class);
            indexIssuer.deleteIndexes();
            sharedEntityIndexer.clear(SearchRequest.ENTITY_TYPE);
            sharedEntityIndexer.clear(PortalPage.ENTITY_TYPE);
        }

        private void replaceIndexes(final File workDir) throws IOException
        {
            // Ensure the indexing directory is empty
            File indexDirectory = new File(indexPathManager.getIndexRootPath());

            // Delete the JIRA specific indexes. If plugins have added others, we don't want to blow them away
            FileUtils.deleteDirectory(new File(indexPathManager.getIssueIndexPath()));
            FileUtils.deleteDirectory(new File(indexPathManager.getCommentIndexPath()));
            FileUtils.deleteDirectory(new File(indexPathManager.getChangeHistoryIndexPath()));
            FileUtils.deleteDirectory(new File(indexPathManager.getSharedEntityIndexPath()));

            if (!indexDirectory.exists())
            {
                indexDirectory.mkdir();
            }
            FileUtils.moveDirectoryToDirectory(new File(workDir, IndexPathManager.Directory.ISSUES_SUBDIR), indexDirectory, true);
            FileUtils.moveDirectoryToDirectory(new File(workDir, IndexPathManager.Directory.COMMENTS_SUBDIR), indexDirectory, true);
            FileUtils.moveDirectoryToDirectory(new File(workDir, IndexPathManager.Directory.CHANGE_HISTORY_SUBDIR), indexDirectory, true);
            FileUtils.moveDirectoryToDirectory(new File(workDir, IndexPathManager.Directory.ENTITIES_SUBDIR), indexDirectory, true);
        }

        private DateRange getDurationToRecover()
        {
            Date latestIndexDate = getLatestIndexDate(null);
            Date latestDbDate = getLatestDbDate();

            if (latestDbDate == null || latestIndexDate == null)
            {
                return null;
            }

            LOG.info(String.format("Latest index date: {%1$tF %1$tT}, Latest DB date: {%2$tF %2$tT}", latestIndexDate, latestDbDate));

            // Stretch the dates a minute further apart to make up for any timing races
            // and precision differences between Lucene and the database.
            if (latestDbDate.after(latestIndexDate))
            {
                latestDbDate = DateUtils.addMinutes(latestDbDate, 1);
                latestIndexDate = DateUtils.addMinutes(latestIndexDate, -1);
            }
            else
            {
                latestIndexDate = DateUtils.addMinutes(latestIndexDate, 1);
                latestDbDate = DateUtils.addMinutes(latestDbDate, -1);
            }
            return new DateRange(latestIndexDate, latestDbDate);
        }

        private Date getLatestIndexDate(final ApplicationUser user)
        {
            final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
            queryBuilder.orderBy().updatedDate(SortOrder.DESC);
            try
            {
                PagerFilter filter = new PagerFilter(0, 1);
                List<Issue> issues = searchProvider.searchOverrideSecurity(queryBuilder.buildQuery(), user, filter, null).getIssues();

                if (issues.size() > 0)
                {
                    return issues.get(0).getUpdated();
                }
            }
            catch (SearchException e)
            {
                LOG.error("Error searching for issues", e);
            }
            return null;
        }

        @Nullable
        private Date getLatestDbDate()
        {
            final GenericValue lastUpdatedIssueGV = Select.columns("id", UPDATED)
                    .from(Entity.Name.ISSUE)
                    .orderBy(UPDATED + " DESC")
                    .limit(1)
                    .runWith(delegator)
                    .singleValue();

            return lastUpdatedIssueGV == null ? null : lastUpdatedIssueGV.getTimestamp(UPDATED);
        }
    }
}
