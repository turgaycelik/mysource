package com.atlassian.jira.bc.project.index;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.Callable;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.index.AccumulatingResultBuilder;
import com.atlassian.jira.index.IssueIndexHelper;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.index.BackgroundIndexListener;
import com.atlassian.jira.issue.index.IndexReconciler;
import com.atlassian.jira.issue.index.IssueBatcherFactory;
import com.atlassian.jira.issue.index.IssueIndexer;
import com.atlassian.jira.issue.index.IssuesBatcher;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.statistics.util.FieldableDocumentHitCollector;
import com.atlassian.jira.issue.util.IssuesIterable;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.task.ProvidesTaskProgress;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.Sized;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.index.Contexts;
import com.atlassian.jira.web.action.admin.index.IndexCommandResult;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;

import com.google.common.collect.ImmutableMap;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;

/**
 * @since v6.1
 */
class ReIndexProjectIndexerCommand implements Callable<IndexCommandResult>, ProvidesTaskProgress
{
    private final Project project;
    private final OfBizDelegator delegator;
    private final IssueIndexer issueIndexer;
    private final TaskManager taskManager;
    private final SearchProvider searchProvider;
    private final IssueManager issueManager;


    private final EventPublisher eventPublisher;
    /**
     * Value used to project re-indexing. Introduced to make project re-indexing more responsive to cancellation request.
     * This value is not configurable through JIRA properties.
     */
    private static final int BATCH_SIZE_FOR_PROJECT = 100;

    public ReIndexProjectIndexerCommand(final Project project, final OfBizDelegator delegator, final IssueIndexer issueIndexer,
            final TaskManager taskManager, final SearchProvider searchProvider,
            final IssueManager issueManager, final EventPublisher eventPublisher, final IssueBatcherFactory issueBatcherFactory,
            final Logger log, final I18nHelper i18nHelper, final I18nHelper.BeanFactory i18nBeanFactory)
    {
        this.issueIndexer = issueIndexer;
        this.taskManager = taskManager;
        this.delegator = delegator;
        this.project = project;
        this.searchProvider = searchProvider;
        this.issueManager = issueManager;
        this.eventPublisher = eventPublisher;
        this.issueBatcherFactory = issueBatcherFactory;
        this.log = log;
        this.i18nHelper = i18nHelper;
        this.i18nBeanFactory = i18nBeanFactory;
    }

    private final IssueBatcherFactory issueBatcherFactory;
    private final Logger log;
    private final I18nHelper i18nHelper;
    private final I18nHelper.BeanFactory i18nBeanFactory;
    private volatile TaskProgressSink taskProgressSink;

    public IndexCommandResult call()
    {
        // We use a null user here to get the Johnson message in the default language for this JIRA instance.
        final String johnsonMessage = i18nBeanFactory.getInstance((ApplicationUser) null).getText("admin.reindex.in.progress.johnson.summary");
        final Event appEvent = new Event(EventType.get("reindex"), johnsonMessage, EventLevel.get(EventLevel.WARNING));
        try
        {
            final Context context = Contexts.percentageReporter(new Sized()
            {
                @Override
                public int size()
                {
                    return (int) delegator.getCountByAnd(Entity.Name.ISSUE, ImmutableMap.of("project", project.getId()));
                }

                @Override
                public boolean isEmpty()
                {
                    return size() == 0;
                }
            }, taskProgressSink, i18nHelper, log, appEvent);
            log.info("Re-indexing started");
            Assertions.notNull("context", context);
            context.setName("Issue");
            log.info(String.format("Reindexing issues in project %s", project.getName()));

            final long startTime = System.currentTimeMillis();

            if (!reindexAllProjectIssues(context))
            {
                return new IndexCommandResult(-1);
            }

            final long totalTime = (System.currentTimeMillis() - startTime);
            if (log.isDebugEnabled())
            {
                log.debug("Reindex took : " + totalTime + "ms");
            }
            return new IndexCommandResult(totalTime);
        }
        finally
        {
            log.info("Re-indexing finished");
        }
    }

    private boolean reindexAllProjectIssues(final Context context)
    {
        TaskDescriptor<Serializable> currentTaskDescriptor = taskManager.getLiveTask(new ProjectIndexTaskContext(project));
        final IssueIndexHelper issueIndexHelper = new IssueIndexHelper(issueManager, issueIndexer, getIssueFactory());

        AccumulatingResultBuilder resultBuilder = new AccumulatingResultBuilder();

        // Create a listener to capture concurrent changes
        final BackgroundIndexListener backgroundIndexListener = new BackgroundIndexListener();
        eventPublisher.register(backgroundIndexListener);
        try
        {
            // Get all the currently indexed issues.  These are in a sorted array
            long[] indexedIssues = getProjectIssuesFromLucene();
            IndexReconciler reconciler = new IndexReconciler(indexedIssues);
            try
            {

                final EntityCondition where = new EntityExpr("project", EntityOperator.EQUALS, project.getId());
                final IssuesBatcher batcher = issueBatcherFactory.getBatcher(where, reconciler, BATCH_SIZE_FOR_PROJECT);
                for (IssuesIterable batchOfIssues : batcher)
                {
                    if (currentTaskDescriptor.isCancelled())
                    {
                        throw new InterruptedException();
                    }

                    resultBuilder.add(issueIndexer.reindexIssues(batchOfIssues, context, false, true, false));
                }
                // Wait on the intermediate result
                resultBuilder.toResult().await();
            }
            finally
            {
                eventPublisher.unregister(backgroundIndexListener);
                issueIndexHelper.fixupConcurrentlyIndexedIssues(context, resultBuilder, backgroundIndexListener, true, true);

            }
            if (currentTaskDescriptor.isCancelled())
            {
                throw new InterruptedException();
            }
            issueIndexHelper.fixupIndexCorruptions(resultBuilder, reconciler);
        }
        catch (SearchException e)
        {
            throw new RuntimeException(e);
        }
        catch (InterruptedException e)
        {
            return false;
        }
        return true;
    }

    private long[] getProjectIssuesFromLucene() throws SearchException
    {
        final ProjectIssueCollector issueCollector = new ProjectIssueCollector();

        final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().project(project.getId());
        searchProvider.searchOverrideSecurity(builder.buildQuery(), (ApplicationUser) null, issueCollector);

        return issueCollector.getIssueIds();
    }

    public I18nHelper getI18nHelper()
    {
        return i18nHelper;
    }

    public void setTaskProgressSink(final TaskProgressSink taskProgressSink)
    {
        this.taskProgressSink = taskProgressSink;
    }

    private static class ProjectIssueCollector extends FieldableDocumentHitCollector
    {
        long[] issueIds = new long[1000];
        private int i = 0;
        private String issueIdFieldName = SystemSearchConstants.forIssueId().getIndexField();

        @Override
        protected FieldSelector getFieldSelector()
        {
            return new FieldSelector()
            {
                @Override
                public FieldSelectorResult accept(final String fieldName)
                {
                    if (fieldName.equals(issueIdFieldName))
                    {
                        return FieldSelectorResult.LOAD_AND_BREAK;
                    }
                    return FieldSelectorResult.NO_LOAD;
                }
            };
        }

        @Override
        public void collect(final Document doc)
        {
            String issueId = doc.get(issueIdFieldName);
            issueIds = ensureCapacity(issueIds, i);
            issueIds[i] = Long.parseLong(issueId);
            i++;
        }

        private long[] ensureCapacity(final long[] issueIds, final int i)
        {
            if (issueIds.length <= i)
            {
                // Expand the array.  This should occur rarely if ever so we only add a small increment
                int newSize = Math.max(i, issueIds.length + issueIds.length / 10);
                return Arrays.copyOf(issueIds, newSize);
            }
            return issueIds;
        }

        public long[] getIssueIds()
        {
            return Arrays.copyOf(issueIds, i);
        }
    }
    IssueFactory getIssueFactory()
    {
        // the reason that this is not done in the constructor is that IssueFactory depends on IssueLinkManager which
        // depends on IssueIndexManager
        // and therefore is a cyclic dependency
        return ComponentAccessor.getComponentOfType(IssueFactory.class);
    }

}