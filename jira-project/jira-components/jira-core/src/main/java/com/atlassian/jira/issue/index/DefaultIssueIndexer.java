package com.atlassian.jira.issue.index;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import com.atlassian.fugue.Effect;
import com.atlassian.fugue.Option;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.index.AccumulatingResultBuilder;
import com.atlassian.jira.index.DefaultIndex;
import com.atlassian.jira.index.Index;
import com.atlassian.jira.index.Index.Operation;
import com.atlassian.jira.index.Index.Result;
import com.atlassian.jira.index.Index.UpdateMode;
import com.atlassian.jira.index.IndexingStrategy;
import com.atlassian.jira.index.MultiThreadedIndexingConfiguration;
import com.atlassian.jira.index.MultiThreadedIndexingStrategy;
import com.atlassian.jira.index.Operations;
import com.atlassian.jira.index.SimpleIndexingStrategy;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistoryGroup;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.index.IndexDirectoryFactory.Mode;
import com.atlassian.jira.issue.index.IndexDirectoryFactory.Name;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.security.RequestCacheKeys;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.collect.EnclosedIterable;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;

import net.jcip.annotations.GuardedBy;

import static com.atlassian.jira.config.properties.PropertiesUtil.getIntProperty;
import static com.atlassian.jira.index.Operations.newCompletionDelegate;
import static com.atlassian.jira.index.Operations.newConditionalUpdate;
import static com.atlassian.jira.index.Operations.newCreate;
import static com.atlassian.jira.index.Operations.newDelete;
import static com.atlassian.jira.index.Operations.newUpdate;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultIssueIndexer implements IssueIndexer
{
    //
    // members
    //

    private final CommentRetriever commentRetriever;
    private final ChangeHistoryRetriever changeHistoryRetriever;
    private final MultiThreadedIndexingConfiguration multiThreadedIndexingConfiguration;

    private final Lifecycle lifecycle;

    private final IssueDocumentFactory issueDocumentFactory;

    private final CommentDocumentFactory commentDocumentFactory;
    private final CommentDocumentBuilder commentDocumentBuilder = new CommentDocumentBuilder();

    private final ChangeHistoryDocumentBuilder changeHistoryDocumentBuilder = new ChangeHistoryDocumentBuilder();
    private final ChangeHistoryDocumentFactory changeHistoryDocumentFactory;

    /**
     * simple indexing strategy just asks the operation for its result, and it is stateless, so we can reuse it.
     */
    private final IndexingStrategy simpleIndexingStrategy = new SimpleIndexingStrategy();

    private final DocumentCreationStrategy documentCreationStrategy = new DefaultDocumentCreationStrategy();

    //
    // ctors
    //

    public DefaultIssueIndexer(
            @Nonnull final IndexDirectoryFactory indexDirectoryFactory,
            @Nonnull final CommentRetriever commentRetriever,
            @Nonnull final ChangeHistoryRetriever changeHistoryRetriever,
            @Nonnull final ApplicationProperties applicationProperties,
            @Nonnull final IssueDocumentFactory issueDocumentFactory,
            @Nonnull final CommentDocumentFactory commentDocumentFactory,
            @Nonnull final ChangeHistoryDocumentFactory changeHistoryDocumentFactory)
    {
        this.lifecycle = new Lifecycle(indexDirectoryFactory);
        this.commentRetriever = notNull("commentRetriever", commentRetriever);
        this.changeHistoryRetriever = notNull("changeHistoryReriever", changeHistoryRetriever);
        this.issueDocumentFactory = notNull("issueDocumentFactory", issueDocumentFactory);
        this.commentDocumentFactory = notNull("commentDocumentFactory", commentDocumentFactory);
        this.changeHistoryDocumentFactory = notNull("changeHistoryDocumentFactory", changeHistoryDocumentFactory);
        this.multiThreadedIndexingConfiguration = new PropertiesAdapter(applicationProperties);
    }

    //
    // methods
    //

    @GuardedBy ("external index read lock")
    public Index.Result deindexIssues(@Nonnull final EnclosedIterable<Issue> issues, @Nonnull final Context context)
    {
        return perform(issues, simpleIndexingStrategy, context, new IndexOperation()
        {
            public Index.Result perform(final Issue issue, final Context.Task task)
            {
                try
                {
                    final Term issueTerm = issueDocumentFactory.getIdentifyingTerm(issue);
                    final Operation delete = newDelete(issueTerm, UpdateMode.INTERACTIVE);
                    final Operation onCompletion = newCompletionDelegate(delete, new TaskCompleter(task));
                    final AccumulatingResultBuilder results = new AccumulatingResultBuilder();
                    results.add("Issue", issue.getId(), lifecycle.getIssueIndex().perform(onCompletion));
                    results.add("Comment For Issue", issue.getId(), lifecycle.getCommentIndex().perform(delete));
                    results.add("Change History For Issue", issue.getId(), lifecycle.getChangeHistoryIndex().perform(delete));
                    return results.toResult();
                }
                catch (final Exception ex)
                {
                    return new DefaultIndex.Failure(ex);
                }
            }
        });
    }

    @GuardedBy ("external index read lock")
    public Index.Result indexIssues(@Nonnull final EnclosedIterable<Issue> issues, @Nonnull final Context context)
    {
        return perform(issues, simpleIndexingStrategy, context, new IndexIssuesOperation(UpdateMode.INTERACTIVE));
    }

    /**
     * No other index operations should be called while this method is being called
     */
    @GuardedBy ("external index write lock")
    public Index.Result indexIssuesBatchMode(@Nonnull final EnclosedIterable<Issue> issues, @Nonnull final Context context)
    {
        if (issues.size() < multiThreadedIndexingConfiguration.minimumBatchSize())
        {
            return indexIssues(issues, context);
        }

        lifecycle.close();
        lifecycle.setMode(Mode.DIRECT);
        try
        {
            return perform(issues, new MultiThreadedIndexingStrategy(simpleIndexingStrategy, multiThreadedIndexingConfiguration, "IssueIndexer"),
                    context, new IndexIssuesOperation(UpdateMode.BATCH));
        }
        finally
        {
            lifecycle.close();
            lifecycle.setMode(Mode.QUEUED);
        }
    }

    @GuardedBy ("external index read lock")
    public Index.Result reindexIssues(@Nonnull final EnclosedIterable<Issue> issues, @Nonnull final Context context,
            final boolean reIndexComments, final boolean reIndexChangeHistory, final boolean conditionalUpdate)
    {
        return perform(issues, simpleIndexingStrategy, context, new IndexOperation()
        {
            public Index.Result perform(final Issue issue, final Context.Task task)
            {
                try
                {
                    final UpdateMode mode = UpdateMode.INTERACTIVE;
                    final Documents documents = documentCreationStrategy.get(issue, reIndexComments, reIndexChangeHistory);
                    final Term issueTerm = documents.getIdentifyingTerm();
                    final Operation update;
                    if (conditionalUpdate)
                    {
                        // do a conditional update using "updated" as the optimistic lock
                        update = newConditionalUpdate(issueTerm, documents.getIssue(), mode, IssueFieldConstants.UPDATED);
                    }
                    else
                    {
                        update = newUpdate(issueTerm, documents.getIssue(), mode);
                    }
                    final Operation onCompletion = newCompletionDelegate(update, new TaskCompleter(task));
                    final AccumulatingResultBuilder results = new AccumulatingResultBuilder();
                    results.add("Issue", issue.getId(), lifecycle.getIssueIndex().perform(onCompletion));
                    if (reIndexComments)
                    {
                        results.add("Comment For Issue", issue.getId(), lifecycle.getCommentIndex().perform(newUpdate(issueTerm, documents.getComments(), mode)));
                    }
                    if (reIndexChangeHistory)
                    {
                        results.add("Change History For Issue", issue.getId(), lifecycle.getChangeHistoryIndex().perform(newUpdate(issueTerm, documents.getChanges(), mode)));
                    }
                    flushCustomFieldValueCache();
                    return results.toResult();
                }
                catch (final Exception ex)
                {
                    return new DefaultIndex.Failure(ex);
                }
            }
        });
    }

    @GuardedBy ("external index read lock")
    public Index.Result reindexComments(@Nonnull final Collection<Comment> comments, @Nonnull final Context context)
    {
        return perform(comments, simpleIndexingStrategy, context, new CommentOperation()
        {
            public Index.Result perform(final Comment comment, final Context.Task task)
            {
                try
                {
                    final UpdateMode mode = UpdateMode.INTERACTIVE;
                    final Option<Document> document = commentDocumentFactory.apply(comment);
                    final Term commentTerm = commentDocumentFactory.getIdentifyingTerm(comment);
                    return document.fold(new com.google.common.base.Supplier<Result>()
                                         {
                                             @Override
                                             public Result get()
                                             {
                                                 return new DefaultIndex.Failure(new RuntimeException("Comment undefined"));
                                             }
                                         }, new Function<Document, Result>()
                                         {
                                             @Override
                                             public Result apply(final Document document)
                                             {
                                                 return lifecycle.getCommentIndex().perform(newUpdate(commentTerm, document, mode));
                                             }

                                         }
                    );

                }
                catch (final Exception ex)
                {
                    return new DefaultIndex.Failure(ex);
                }
            }
        });
    }

    public void deleteIndexes()
    {
        for (final Index.Manager manager : lifecycle)
        {
            manager.deleteIndexDirectory();
        }
    }

    public IndexSearcher openCommentSearcher()
    {
        return lifecycle.get(Name.COMMENT).openSearcher();
    }

    public IndexSearcher openIssueSearcher()
    {
        return lifecycle.get(Name.ISSUE).openSearcher();
    }

    public IndexSearcher openChangeHistorySearcher()
    {
        return lifecycle.get(Name.CHANGE_HISTORY).openSearcher();
    }

    public Index.Result optimize()
    {
        final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
        for (final Index.Manager manager : lifecycle)
        {
            builder.add(manager.getIndex().perform(Operations.newOptimize()));
        }
        return builder.toResult();
    }

    public void shutdown()
    {
        lifecycle.close();
    }

    public List<String> getIndexPaths()
    {
        return lifecycle.getIndexPaths();
    }

    public String getIndexRootPath()
    {
        return lifecycle.getIndexRootPath();
    }

    /**
     * Perform an {@link IndexOperation} on some {@link EnclosedIterable issues} using a particular {@link
     * IndexingStrategy strategy}. There is a {@link Context task context} that must be updated to provide feedback to
     * the user.
     * <p/>
     * The implementation needs to be thread-safe, as it may be run in parallel and maintain a composite result to
     * return to the caller.
     *
     * @param issues the issues to index/deindex/reindex
     * @param strategy single or multi-threaded
     * @param context task context for status feedback
     * @param operation deindex/reindex/index etc.
     * @return the {@link Result} may waited on or not.
     */
    private static Index.Result perform(final EnclosedIterable<Issue> issues, final IndexingStrategy strategy, final Context context, final IndexOperation operation)
    {
        try
        {
            notNull("issues", issues);
            // thread-safe handler for the asynchronous Result
            final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
            // perform the operation for every issue in the collection
            issues.foreach(new Consumer<Issue>()
            {
                public void consume(@Nonnull final Issue issue)
                {
                    // wrap the updater task in a Job and give it a Context.Task so we can tell the user what's happening
                    final Context.Task task = context.start(issue);
                    // ask the Strategy for the Result, this may be performed on a thread-pool
                    // the result may be a future if asynchronous

                    final Result result = strategy.get(new Supplier<Index.Result>()
                    {
                        public Index.Result get()
                        {
                            // the actual index operation
                            return operation.perform(issue, task);
                        }
                    });
                    builder.add("Issue", issue.getId(), result);
                }
            });
            return builder.toResult();
        }
        finally
        {
            strategy.close();
        }
    }

    /**
     * Perform an {@link IndexOperation} on some {@link Collection comments} using a particular {@link IndexingStrategy
     * strategy}. There is a {@link Context task context} that must be updated to provide feedback to the user.
     * <p/>
     * The implementation needs to be thread-safe, as it may be run in parallel and maintain a composite result to
     * return to the caller.
     *
     * @param comments the comments to index/deindex/reindex
     * @param strategy single or multi-threaded
     * @param context task context for status feedback
     * @param operation deindex/reindex/index etc.
     * @return the {@link Result} may waited on or not.
     */
    private static Index.Result perform(final Iterable<Comment> comments, final IndexingStrategy strategy, final Context context, final CommentOperation operation)
    {
        try
        {
            notNull("comments", comments);
            // thread-safe handler for the asynchronous Result
            final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
            // perform the operation for every comment in the collection
            for (final Comment comment : comments)
            {
                // wrap the updater task in a Job and give it a Context.Task so we can tell the user what's happening
                final Context.Task task = context.start(comment);
                // ask the Strategy for the Result, this may be performed on a thread-pool
                // the result may be a future if asynchronous
                final Result result = strategy.get(new Supplier<Index.Result>()
                {
                    public Index.Result get()
                    {
                        // the actual index operation
                        return operation.perform(comment, task);
                    }
                });
                builder.add("Comment", comment.getId(), result);
            }
            return builder.toResult();
        }
        finally
        {
            strategy.close();
        }
    }

    //
    // inner classes
    //

    public interface CommentRetriever extends Function<Issue, List<Comment>>
    {
    }

    public interface ChangeHistoryRetriever extends Function<Issue, List<ChangeHistoryGroup>>
    {
    }

    /**
     * Manage the life-cycle of the three index managers.
     */
    private static class Lifecycle implements Iterable<Index.Manager>
    {
        private final AtomicReference<Map<IndexDirectoryFactory.Name, Index.Manager>> ref = new AtomicReference<Map<IndexDirectoryFactory.Name, Index.Manager>>();
        private final IndexDirectoryFactory factory;

        public Lifecycle(@Nonnull final IndexDirectoryFactory factory)
        {
            this.factory = notNull("factory", factory);
        }

        public Iterator<Index.Manager> iterator()
        {
            return open().values().iterator();
        }

        void close()
        {
            final Map<IndexDirectoryFactory.Name, Index.Manager> indexes = ref.getAndSet(null);
            if (indexes == null)
            {
                return;
            }
            for (final Index.Manager manager : indexes.values())
            {
                manager.close();
            }
        }

        Map<IndexDirectoryFactory.Name, Index.Manager> open()
        {
            Map<IndexDirectoryFactory.Name, Index.Manager> result = ref.get();
            while (result == null)
            {
                ref.compareAndSet(null, factory.get());
                result = ref.get();
            }
            return result;
        }

        Index getIssueIndex()
        {
            return get(Name.ISSUE).getIndex();
        }

        Index getCommentIndex()
        {
            return get(Name.COMMENT).getIndex();
        }

        Index getChangeHistoryIndex()
        {
            return get(Name.CHANGE_HISTORY).getIndex();
        }

        Index.Manager get(final Name key)
        {
            return open().get(key);
        }

        List<String> getIndexPaths()
        {
            return factory.getIndexPaths();
        }

        String getIndexRootPath()
        {
            return factory.getIndexRootPath();
        }

        void setMode(final Mode type)
        {
            factory.setIndexingMode(type);
        }
    }

    /**
     * Used when indexing to do the actual indexing of an issue.
     */
    private class IndexIssuesOperation implements IndexOperation
    {
        final UpdateMode mode;

        IndexIssuesOperation(final UpdateMode mode)
        {
            this.mode = mode;
        }

        public Index.Result perform(final Issue issue, final Context.Task task)
        {
            try
            {
                final Documents documents = documentCreationStrategy.get(issue, true, true);
                final Operation issueCreate = newCreate(documents.getIssue(), mode);
                final Operation onCompletion = newCompletionDelegate(issueCreate, new TaskCompleter(task));
                final AccumulatingResultBuilder results = new AccumulatingResultBuilder();
                results.add("Issue", issue.getId(), lifecycle.getIssueIndex().perform(onCompletion));
                if (!documents.getComments().isEmpty())
                {
                    final Operation commentsCreate = newCreate(documents.getComments(), mode);
                    results.add("Comment For Issue", issue.getId(), lifecycle.getCommentIndex().perform(commentsCreate));
                }
                if (!documents.getChanges().isEmpty())
                {
                    final Operation changeHistoryCreate = newCreate(documents.getChanges(), mode);
                    results.add("Change History For Issue", issue.getId(), lifecycle.getChangeHistoryIndex().perform(changeHistoryCreate));
                }
                flushCustomFieldValueCache();
                return results.toResult();
            }
            catch (final Exception ex)
            {
                return new DefaultIndex.Failure(ex);
            }
        }
    }

    private void flushCustomFieldValueCache()
    {
        // TODO This is a horrible hack to stop the vield values cache growing out of control during a reindex.  Find a better way!
        final Map<?, ?> customFieldValueCache = (Map<?, ?>) JiraAuthenticationContextImpl.getRequestCache().get(RequestCacheKeys.CUSTOMFIELD_VALUES_CACHE);
        if (customFieldValueCache != null)
        {
            customFieldValueCache.clear();
        }
        // TODO:end
    }

    /**
     * An {@link IndexOperation} performs the actual update to the index for a specific {@link Issue}.
     */
    private interface IndexOperation
    {
        Index.Result perform(Issue issue, Context.Task task);
    }

    /**
     * An {@link IndexOperation} performs the actual update to the index for a specific {@link Comment}.
     */
    private interface CommentOperation
    {
        Index.Result perform(Comment comment, Context.Task task);
    }

    private static class TaskCompleter implements Runnable
    {
        private final Context.Task task;

        public TaskCompleter(final Context.Task task)
        {
            this.task = task;
        }

        public void run()
        {
            task.complete();
        }
    }

    interface DocumentCreationStrategy
    {
        Documents get(Issue input, boolean includeComments, boolean includeChangeHistory);
    }

    class Documents
    {
        private final Document issueDocument;
        private final List<Document> comments;
        private final List<Document> changes;
        private final Term term;

        Documents(final Issue issue, final Option<Document> issueDocument, final Collection<Option<Document>> comments, final Collection<Option<Document>> changes)
        {
            Preconditions.checkArgument(issueDocument.isDefined(), "Issue document bust be defined");
            this.issueDocument = issueDocument.get();
            this.comments = LuceneDocumentsBuilder.foreach(comments);
            this.changes = LuceneDocumentsBuilder.foreach(changes);
            term = issueDocumentFactory.getIdentifyingTerm(issue);
        }

        Document getIssue()
        {
            return issueDocument;
        }

        List<Document> getComments()
        {
            return comments;
        }

        List<Document> getChanges()
        {
            return changes;
        }

        Term getIdentifyingTerm()
        {
            return term;
        }
    }

    private static class LuceneDocumentsBuilder implements Effect<Document>
    {
        private final ImmutableList.Builder<Document> builder = ImmutableList.builder();

        public static List<Document> foreach(final Collection<Option<Document>> documents)
        {
            final LuceneDocumentsBuilder luceneDocumentsBuilder = new LuceneDocumentsBuilder();
            for (final Option<Document> document : documents)
            {
                document.foreach(luceneDocumentsBuilder);
            }
            return luceneDocumentsBuilder.builder.build();
        }

        @Override
        public void apply(final Document luceneDocument)
        {
            builder.add(luceneDocument);
        }

    }

    /**
     * Get the list of comment documents for indexing
     */
    class CommentDocumentBuilder implements Function<Issue, Collection<Option<Document>>>
    {
        public Collection<Option<Document>> apply(final Issue issue)
        {
            return Collections2.transform(commentRetriever.apply(issue), commentDocumentFactory);
        }
    }

    /**
     * Get the list of change documents for indexing
     */
    class ChangeHistoryDocumentBuilder implements Function<Issue, Collection<Option<Document>>>
    {
        public Collection<Option<Document>> apply(final Issue issue)
        {
            return Collections2.transform(changeHistoryRetriever.apply(issue), changeHistoryDocumentFactory);
        }
    }

    /**
     * Get the documents (issue and comments) for the issue.
     */
    class DefaultDocumentCreationStrategy implements DocumentCreationStrategy
    {
        public Documents get(final Issue issue, final boolean includeComments, final boolean includeChangeHistory)
        {
            final Collection<Option<Document>> comments = includeComments ? commentDocumentBuilder.apply(issue) : Collections.<Option<Document>>emptyList();
            final Collection<Option<Document>> changes = includeChangeHistory ? changeHistoryDocumentBuilder.apply(issue) : Collections.<Option<Document>>emptyList();
            return new Documents(issue, issueDocumentFactory.apply(issue), comments, changes);
        }
    }

    static class PropertiesAdapter implements MultiThreadedIndexingConfiguration
    {
        private final ApplicationProperties applicationProperties;

        PropertiesAdapter(ApplicationProperties applicationProperties)
        {
            this.applicationProperties = notNull("applicationProperties", applicationProperties);
        }

        public int minimumBatchSize()
        {
            return getIntProperty(applicationProperties, APKeys.JiraIndexConfiguration.Issue.MIN_BATCH_SIZE, 50);
        }

        public int maximumQueueSize()
        {
            return getIntProperty(applicationProperties, APKeys.JiraIndexConfiguration.Issue.MAX_QUEUE_SIZE, 1000);
        }

        public int noOfThreads()
        {
            return getIntProperty(applicationProperties, APKeys.JiraIndexConfiguration.Issue.THREADS, 20);
        }
    }
}