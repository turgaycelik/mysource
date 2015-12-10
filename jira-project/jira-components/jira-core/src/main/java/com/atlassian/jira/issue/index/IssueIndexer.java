package com.atlassian.jira.issue.index;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.jira.index.Index;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.collect.EnclosedIterable;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;

import net.jcip.annotations.GuardedBy;

public interface IssueIndexer
{
    public static class Analyzers
    {
        public static final Analyzer SEARCHING = JiraAnalyzer.ANALYZER_FOR_SEARCHING;
        public static final Analyzer INDEXING = JiraAnalyzer.ANALYZER_FOR_INDEXING;
    }

    /**
     * Add documents for the supplied issues.
     *
     * @param issues An iterable of issues to index.
     * @param context for showing the user the current status.
     */
    Index.Result indexIssues(@Nonnull EnclosedIterable<Issue> issues, @Nonnull Context context);

    /**
     * Delete any existing documents for the supplied issues.
     *
     * @param issues An iterable of issues to index.
     * @param context for showing the user the current status.
     */
    Index.Result deindexIssues(@Nonnull EnclosedIterable<Issue> issues, @Nonnull Context context);

    /**
     * Re-index the given issues, delete any existing documents and add new ones.
     *
     * @param issues An iterable of issues to index.
     * @param context for showing the user the current status.
     * @param reIndexComments Set to true if you require issue comments to also be reindexed.
     * @param reIndexChangeHistory Set to true if you require issue change history to also be reindexed.
     * @param conditionalUpdate set to true to use conditional updates when writing to the index
     */
    Index.Result reindexIssues(@Nonnull EnclosedIterable<Issue> issues, @Nonnull Context context, boolean reIndexComments,
            boolean reIndexChangeHistory, boolean conditionalUpdate);

    /**
     * Reindex a collection of issue comments.
     * @param comments Comments to be reindexed.
     * @param context for showing the user the current status.
     */
    Index.Result reindexComments(@Nonnull final Collection<Comment> comments, @Nonnull final Context context);

    /**
     * Index the given issues, use whatever is in your arsenal to do it as FAST as possible.
     *
     * @param issues An iterable of issues to index.
     * @param context for showing the user the current status.
     */
    @GuardedBy("external indexing lock")
    Index.Result indexIssuesBatchMode(@Nonnull EnclosedIterable<Issue> issues, @Nonnull Context context);

    @GuardedBy("external indexing lock")
    Index.Result optimize();

    void deleteIndexes();

    void shutdown();

    /**
     * Issue searcher has to be closed after doing stuff.
     */
    IndexSearcher openIssueSearcher();

    /*
     * Issue searcher has to be closed after doing stuff.
     */
    IndexSearcher openCommentSearcher();

    /**
     * Issue searcher has to be closed after doing stuff.
     */
    IndexSearcher openChangeHistorySearcher();

    List<String> getIndexPaths();

    String getIndexRootPath();
}
