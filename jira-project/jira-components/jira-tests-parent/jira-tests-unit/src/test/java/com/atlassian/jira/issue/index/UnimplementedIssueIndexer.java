/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.index;

import com.atlassian.jira.index.Index.Result;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.task.context.Context;
import javax.annotation.Nonnull;
import com.atlassian.jira.util.collect.EnclosedIterable;
import org.apache.lucene.search.IndexSearcher;

import java.util.Collection;
import java.util.List;

public class UnimplementedIssueIndexer implements IssueIndexer
{
    public Result deindexIssues(final EnclosedIterable<Issue> issues, final Context event)
    {
        throw new UnsupportedOperationException();
    }

    public Result indexIssues(final EnclosedIterable<Issue> issues, final Context event)
    {
        throw new UnsupportedOperationException();
    }

    public Result indexIssuesBatchMode(final EnclosedIterable<Issue> issuesIterable, final Context event)
    {
        throw new UnsupportedOperationException();
    }

    public Result reindexIssues(final EnclosedIterable<Issue> issuesIterable, final Context event, boolean reIndexComments, boolean reIndexChangeHistory, boolean conditionalUpdate)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Result reindexComments(@Nonnull Collection<Comment> comments, @Nonnull Context context)
    {
        throw new UnsupportedOperationException();
    }

    public Result indexIssuesBackgroundMode(@Nonnull EnclosedIterable<Issue> issues, @Nonnull Context context, boolean reIndexComments, boolean reIndexChangeHistory)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Result optimize()
    {
        throw new UnsupportedOperationException();
    }

    public void deleteIndexes()
    {
        throw new UnsupportedOperationException();
    }

    public void shutdown()
    {
        throw new UnsupportedOperationException();
    }

    public IndexSearcher openIssueSearcher()
    {
        throw new UnsupportedOperationException();
    }

    public IndexSearcher openCommentSearcher()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public IndexSearcher openChangeHistorySearcher()
    {
         throw new UnsupportedOperationException();
    }

    public List<String> getIndexPaths()
    {
        throw new UnsupportedOperationException();
    }

    public void setIndexRootPath(final String path)
    {
        throw new UnsupportedOperationException();
    }

    public String getIndexRootPath()
    {
        throw new UnsupportedOperationException();
    }
}
