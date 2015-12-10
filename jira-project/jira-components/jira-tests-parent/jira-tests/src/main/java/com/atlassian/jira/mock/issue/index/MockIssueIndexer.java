package com.atlassian.jira.mock.issue.index;

import com.atlassian.jira.index.Index;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.index.IssueIndexer;
import com.atlassian.jira.task.context.Context;
import javax.annotation.Nonnull;
import com.atlassian.jira.util.collect.EnclosedIterable;
import net.jcip.annotations.ThreadSafe;
import org.apache.lucene.search.IndexSearcher;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mock issue indexer that records calls indexing methods for verification.
 *
 * @since v4.3
 */
@ThreadSafe
public class MockIssueIndexer implements IssueIndexer
{

    public final int indexWorkTime;
    public final int optimizationWorkTime;
    public final Collection<Issue> indexedIssues = new ConcurrentLinkedQueue<Issue>();
    public final Collection<Issue> reIndexedIssues = new ConcurrentLinkedQueue<Issue>();
    public final AtomicInteger optimizations = new AtomicInteger();

    public MockIssueIndexer()
    {
        this(1000, 2000);
    }

    public MockIssueIndexer(int indexSleepTime, int optimizationSleepTime)
    {
        this.indexWorkTime = indexSleepTime;
        this.optimizationWorkTime = optimizationSleepTime;
    }

    @Override
    public Index.Result indexIssues(@Nonnull EnclosedIterable<Issue> issues, @Nonnull Context context)
    {
        indexedIssues.addAll(EnclosedIterable.Functions.toList(issues));
        return new MockResult(indexWorkTime);
    }

    @Override
    public Index.Result deindexIssues(@Nonnull EnclosedIterable<Issue> issues, @Nonnull Context context)
    {
        throw new UnsupportedOperationException("implement if necessary");
    }

    @Override
    public Index.Result reindexIssues(@Nonnull EnclosedIterable<Issue> issues, @Nonnull Context context, boolean reIndexComments, boolean reIndexChangeHistory, boolean conditionalUpdate)
    {
        reIndexedIssues.addAll(EnclosedIterable.Functions.toList(issues));
        return new MockResult(indexWorkTime);
    }

    @Override
    public Index.Result indexIssuesBatchMode(@Nonnull EnclosedIterable<Issue> issues, @Nonnull Context context)
    {
        return null;
    }

    @Override
    public Index.Result reindexComments(@Nonnull Collection<Comment> comments, @Nonnull Context context)
    {
        return null;
    }

    @Override
    public Index.Result optimize()
    {
        optimizations.incrementAndGet();
        return new MockResult(optimizationWorkTime);
    }

    @Override
    public void deleteIndexes()
    {
    }

    @Override
    public void shutdown()
    {
    }

    @Override
    public IndexSearcher openIssueSearcher()
    {
        return null;
    }

    @Override
    public IndexSearcher openCommentSearcher()
    {
        return null;
    }

    @Override
    public IndexSearcher openChangeHistorySearcher()
    {
        return null;
    }

    @Override
    public List<String> getIndexPaths()
    {
        return null;
    }

    @Override
    public String getIndexRootPath()
    {
        return null;
    }

    private static final class MockResult implements Index.Result
    {
        private final int workTime;
        private volatile boolean done = false;

        public MockResult(int sleepTime)
        {
            this.workTime = sleepTime;
        }

        @Override
        public void await()
        {
            simulateWork();
        }

        @Override
        public boolean await(long timeout, TimeUnit unit)
        {
            if (unit.toMillis(timeout) > workTime)
            {
                simulateWork();
                return true;
            }
            else
            {
                try
                {
                    unit.sleep(timeout);
                    return false;
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public boolean isDone()
        {
            return done;
        }

        private void simulateWork()
        {
            try
            {
                Thread.sleep(workTime);
                done = true;
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }
}
