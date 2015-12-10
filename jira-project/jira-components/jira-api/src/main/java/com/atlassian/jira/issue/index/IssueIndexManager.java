/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.index;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.index.LuceneVersion;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.util.IssuesIterable;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.Version;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Set;

/**
 * Manages Lucene search indexes.
 */
@PublicApi
public interface IssueIndexManager extends IndexLifecycleManager
{
    /**
     * @deprecated Use {@link com.atlassian.jira.index.LuceneVersion#get()} instead.
     * @since 6.0.5
     */
    @Deprecated
    Version LUCENE_VERSION = LuceneVersion.get();

    /**
     * Reindex all issues.
     *
     * @return Reindex time in ms.
     */
    long reIndexAll() throws IndexException;


    /**
     * Reindex all issues.
     * @param context used to report progress back to the user or to the logs. Must not be null
     * @param useBackgroundReindexing whether to index in the background or not. If the useBackgroundReindexing option is
     * set to true, then comments and change history will not be reindexed.
     * @param updateReplicatedIndexStore whether to update the replicated index or not
     * @return Reindex time in ms.
     */
    long reIndexAll(Context context, boolean useBackgroundReindexing, boolean updateReplicatedIndexStore) throws IndexException;

    /**
     * Reindex all issues.
     * If the useBackgroundReindexing option is set to true, then only the basic issue information will be reindexed, unless the
     * reIndexComments or reIndexChangeHistory parameters are also set.
     * This is considered the normal mode for background re-indexing and is sufficient to correct the index for changes in the
     * system configuration, but not for changes to the indexing language.
     * If useBackgroundReindexing is set to false, than everything is always reindexed.
     *
     * @param context used to report progress back to the user or to the logs. Must not be null
     * @param useBackgroundReindexing whether to index in the background or not
     * @param reIndexComments Also reindex all the issue comments. Only relevant for background reindex operations.
     * @param reIndexChangeHistory Also reindex the issue change history. Only relevant for background reindex operations.
     * @param updateReplicatedIndexStore whether to update the replicated index or not
     * @return Reindex time in ms.
     * @since 6.2
     */
    long reIndexAll(Context context, boolean useBackgroundReindexing, boolean reIndexComments, boolean reIndexChangeHistory, boolean updateReplicatedIndexStore) throws IndexException;

    /**
     * Reindex an issue (eg. after field updates).
     * @deprecated Use {@link #reIndex(com.atlassian.jira.issue.Issue)} instead. Since v5.0.
     */
    void reIndex(GenericValue issue) throws IndexException;

    /**
     * Reindex a list of issues, passing an optional event that will be set progress
     * 
     * @param issuesIterable IssuesIterable
     * @param context used to report progress back to the user or to the logs. Must not be null.
     * @return Reindex time in ms.
     */
    long reIndexIssues(IssuesIterable issuesIterable, Context context) throws IndexException;

    /**
     * Reindex a list of issues, passing an optional event that will be set progress. This method can optionally also
     * index the comments and change history.
     *
     * @param issuesIterable IssuesIterable
     * @param context used to report progress back to the user or to the logs. Must not be null.
     * @param reIndexComments    a boolean indicating whether to index issue comments
     * @param reIndexChangeHistory a boolean indicating whether to index issue change history
     * @return Reindex time in ms.
     * @since v5.2
     */
    long reIndexIssues(IssuesIterable issuesIterable, Context context, boolean reIndexComments, boolean reIndexChangeHistory) throws IndexException;

    /**
     * Reindex an issue (eg. after field updates).
     */
    void reIndex(Issue issue) throws IndexException;

    /**
     * Reindex an issue (eg. after field updates).
     * @since v5.2
     */
    void reIndex(Issue issue, boolean reIndexComments, boolean reIndexChangeHistory) throws IndexException;

    /**
     * Reindexes a collection of comments.
     *
     * @param comments a collection of Comment
     * @since v5.2
     */
    long reIndexComments(Collection<Comment> comments) throws IndexException;

    /**
     * Reindexes a collection of comments.
     *
     * @param comments a collection of Comment
     * @param context used to report progress back to the user or to the logs. Must not be null.
     * @since v5.2
     */
    long reIndexComments(Collection<Comment> comments, Context context) throws IndexException;


    /**
     * Reindexes a collection of comments.
     *
     * @param comments a collection of Comment
     * @param context used to report progress back to the user or to the logs. Must not be null.
     * @param updateReplicatedIndexStore whether to update the replicated index or not
     * @since v6.1
     */
    long reIndexComments(Collection<Comment> comments, Context context, boolean updateReplicatedIndexStore) throws IndexException;

    /**
     * Remove an issue from the search index.
     * @deprecated Use {@link #deIndex(com.atlassian.jira.issue.Issue)} instead. Since v5.0.
     */
    void deIndex(GenericValue issue) throws IndexException;

    /**
     * Remove an issue from the search index.
     */
    void deIndex(Issue issue) throws IndexException;

    /**
     * Remove a set of issues from the search index.
     */
     void deIndexIssueObjects(Set<Issue> issuesToDelete, boolean updateReplicatedIndexStore) throws IndexException;

    /**
     * Reindex a set of issues (GenericValues). Use {@link #reIndexIssueObjects(Collection)} instead when possible.
     * 
     * @param issues The Issue {@link GenericValue}s to reindex.
     * @return Reindex time in ms.
     *
     * @deprecated Use {@link #reIndexIssueObjects(java.util.Collection)} instead. Since v5.0.
     */
    long reIndexIssues(final Collection<GenericValue> issues) throws IndexException;

    /**
     * Reindex a set of issues.
     * 
     * @param issueObjects Set of {@link com.atlassian.jira.issue.Issue}s to reindex.
     * @return Reindex time in ms.
     * @since v5.2
     */
    long reIndexIssueObjects(final Collection<? extends Issue> issueObjects) throws IndexException;

    /**
     * Reindex a set of issues.
     *
     * @param issueObjects Set of {@link com.atlassian.jira.issue.Issue}s to reindex.
     * @return Reindex time in ms.
     */
    long reIndexIssueObjects(final Collection<? extends Issue> issueObjects, boolean reIndexComments, boolean reIndexChangeHistory) throws IndexException;

    /**
     * Reindex a set of issues.
     *
     * @param issueObjects Set of {@link com.atlassian.jira.issue.Issue}s to reindex.
     * @param reIndexComments whether to reindex the comments or not
     * @param reIndexChangeHistory whether to reindex changeHistory or not
     * @param updateReplicatedIndexStore whether to store index operations in the replicated index store
     * @return Reindex time in ms.
     */
    long reIndexIssueObjects(final Collection<? extends Issue> issueObjects, boolean reIndexComments, boolean reIndexChangeHistory, boolean updateReplicatedIndexStore) throws IndexException;

    /**
     * Temporarily suspend indexing on this thread.  All index requests will be queued and processed
     * when release is called.
     * @since v5.1
     */
    void hold();

    /**
     * Return true if the index is held.
     * @since v5.1
     */
    boolean isHeld();

    /**
     * Release indexing on this thread.  All queued index requests will be processed.
     * @return Reindex time in ms.
     * @throws IndexException if an error occurs
     * @since v5.1
     */
    long release() throws IndexException;

    /**
     * Get the root path of the index directory for plugins. Any plugin that keeps indexes should create its own sub-directory under this path and
     * create its indexes in its own sub-directory
     */
    String getPluginsRootPath();

    /**
     * Returns a collection of Strings, each one representing the absolute path to the actual <b>existing</b> directory where a plugin keeps its
     * indexes. Each directory in the collection should be a sub-directory under the plugin's index root path. See {@link #getPluginsRootPath()}.
     * <p>
     * If a plugin index root path does not exist, or is empty (no sub-directopries exist) then an empty collection will be returned.
     * </p>
     */
    Collection<String> getExistingPluginsPaths();

    /**
     * Get an {@link IndexSearcher} that can be used to search the issue index.
     * <p />
     * Note: This is an unmanaged IndexSearcher. You MUST call {@link IndexSearcher#close()} when you are done with it. Alternatively you should
     * really call {@link SearchProviderFactory#getSearcher(String))} passing in {@link SearchProviderFactory#ISSUE_INDEX} as it is a managed searcher
     * and all the closing semantics are handled for you.
     */
    IndexSearcher getIssueSearcher();

    /**
     * Get an {@link IndexSearcher} that can be used to search the comment index.
     * <p />
     * Note: This is an unmanaged IndexSearcher. You MUST call {@link IndexSearcher#close()} when you are done with it. Alternatively you should
     * really call {@link SearchProviderFactory#getSearcher(String))} passing in {@link SearchProviderFactory#COMMENT_INDEX} as it is a managed
     * searcher and all the closing semantics are handled for you.
     */
    IndexSearcher getCommentSearcher();


      /**
     * Get an {@link IndexSearcher} that can be used to search the change history index.
     * <p />
     * Note: This is an unmanaged IndexSearcher. You MUST call {@link IndexSearcher#close()} when you are done with it. Alternatively you should
     * really call {@link SearchProviderFactory#getSearcher(String))} passing in {@link SearchProviderFactory#CHANGE_HISTORY_INDEX} as it is a managed
     * searcher and all the closing semantics are handled for you.
     */
    IndexSearcher getChangeHistorySearcher();

    /**
     * Returns an {@link Analyzer} for searching.
     *
     * @return an analyzer for searching
     */
    Analyzer getAnalyzerForSearching();

    /**
     * Returns an {@link Analyzer} for indexing.
     *
     * @return an analyzer for indexing.
     */
    Analyzer getAnalyzerForIndexing();

    /**
     * Runs the given runnable under the 'stop the world' reindex lock.
     *
     * @param runnable The runnable to be executed
     * @return true if the lock could be acquired, false otherwise
     * @since v6.3
     */
    boolean withReindexLock(Runnable runnable);
}