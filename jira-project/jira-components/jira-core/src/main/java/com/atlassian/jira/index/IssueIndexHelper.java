package com.atlassian.jira.index;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.index.BackgroundIndexListener;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.IndexReconciler;
import com.atlassian.jira.issue.index.IssueIndexer;
import com.atlassian.jira.issue.util.IssueIdsIssueIterable;
import com.atlassian.jira.issue.util.IssueObjectIssuesIterable;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.task.context.Contexts;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

public class IssueIndexHelper
{
    private static final Logger log = Logger.getLogger(IssueIndexHelper.class);
    private final IssueManager issueManager;
    private final IssueIndexer issueIndexer;
    private final IssueFactory issueFactory;

    public IssueIndexHelper(final IssueManager issueManager, final IssueIndexer issueIndexer, final IssueFactory issueFactory)
    {
        this.issueManager = issueManager;
        this.issueIndexer = issueIndexer;
        this.issueFactory = issueFactory;
    }

    /**
     * Get all the issue ids known that are present in the index. The issue ids are returned in a Sorted array.
     *
     * @return array of issue ids.
     */
    public long[] getAllIssueIds()
    {
        return withIssueSearcher(new SearcherFunction<long[]>()
        {
            @Override
            public long[] apply(final IndexSearcher issueSearcher) throws IOException
            {
                IndexReader indexReader = issueSearcher.getIndexReader();
                // We know implicitly that the there is exactly one and only one issue Id per document
                TermEnum termEnum = indexReader.terms(new Term(DocumentConstants.ISSUE_ID, ""));
                try
                {
                    int i = 0;
                    long[] issueIds = new long[indexReader.numDocs()];
                    do
                    {
                        Term term = termEnum.term();
                        // Lucene terms are interned so the != comparison is safe.
                        if (term == null || term.field() != DocumentConstants.ISSUE_ID)
                        {
                            // No issues. May happen
                            break;
                        }
                        String issueId = term.text();
                        issueIds = ensureCapacity(issueIds, i);
                        issueIds[i] = Long.parseLong(issueId);
                        i++;
                    }
                    while (termEnum.next());

                    return issueIds;
                }
                finally
                {
                    termEnum.close();
                }
            }
        });
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

    public void fixupConcurrentlyIndexedIssues(final Context context, final AccumulatingResultBuilder resultBuilder, final BackgroundIndexListener backgroundIndexListener, final boolean reIndexComments, final boolean reIndexChangeHistory)
    {
        // Safely reindex any issue that were concurrently updated - even if we have been cancelled.
        IssueIdsIssueIterable issueIterable = new IssueIdsIssueIterable(backgroundIndexListener.getUpdatedIssues(), issueManager);

        resultBuilder.add(issueIndexer.reindexIssues(issueIterable, context, reIndexComments, reIndexChangeHistory, true));
        resultBuilder.toResult().await();

        // Make sure we haven't accidentally replaced any issues that were concurrently deleted.
        safelyRemoveOrphans(resultBuilder, backgroundIndexListener.getDeletedIssues());
        resultBuilder.toResult().await();

    }

    public void fixupIndexCorruptions(final AccumulatingResultBuilder resultBuilder, final IndexReconciler reconciler)
    {
        // Get issue that were found in the database but not in the index, They need to be reindexed again
        // if they still exist in the database and if they are still not in the index.
        // If they are in the database, then they have been indexed since we began the reindex and so all is well.
        safelyAddMissing(resultBuilder, reconciler.getUnindexed());
        resultBuilder.toResult().await();

        log.debug("" + reconciler.getUnindexed().size() + " missing issues add to the index.");

        // These issue were not found in the database but were in the index, They need to be removed
        // if they still do not exist in the database.
        safelyRemoveOrphans(resultBuilder, reconciler.getOrphans());
        resultBuilder.toResult().await();

        log.debug("" + reconciler.getOrphans().size() + " deleted issues removed from the index.");
    }


    public void safelyAddMissing(final AccumulatingResultBuilder resultBuilder, final Collection<Long> unindexed)
    {
        withIssueSearcher(new SearcherFunction<Void>()
        {
            @Override
            public Void apply(final IndexSearcher issueSearcher)
            {
                for (Long issueId : unindexed)
                {
                    try
                    {
                        MutableIssue issue = issueManager.getIssueObject(issueId);
                        if (issue != null)
                        {
                            TermQuery query = new TermQuery(new Term(DocumentConstants.ISSUE_ID, String.valueOf(issueId)));
                            TopDocs topDocs = issueSearcher.search(query, 1);
                            if (topDocs.totalHits == 0)
                            {
                                IssueObjectIssuesIterable issues = new IssueObjectIssuesIterable(Collections.singletonList(issue));
                                resultBuilder.add(issueIndexer.reindexIssues(issues, Contexts.nullContext(), true, true, false));
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        resultBuilder.add(new DefaultIndex.Failure(e));
                    }
                }

                return null;
            }
        });
    }

    public void safelyRemoveOrphans(final AccumulatingResultBuilder resultBuilder, final Collection<Long> orphans)
    {
        withIssueSearcher(new SearcherFunction<Void>()
        {
            @Override
            public Void apply(final IndexSearcher issueSearcher)
            {
                for (Long issueId : orphans)
                {
                    try
                    {
                        MutableIssue issue = issueManager.getIssueObject(issueId);
                        if (issue == null)
                        {
                            TermQuery query = new TermQuery(new Term(DocumentConstants.ISSUE_ID, String.valueOf(issueId)));
                            TopDocs topDocs = issueSearcher.search(query, 1);
                            for (ScoreDoc scoreDoc : topDocs.scoreDocs)
                            {
                                Document doc = issueSearcher.doc(scoreDoc.doc);
                                Issue issueToDelete = issueFactory.getIssue(doc);
                                IssueObjectIssuesIterable issues = new IssueObjectIssuesIterable(Collections.singletonList(issueToDelete));
                                resultBuilder.add(issueIndexer.deindexIssues(issues, Contexts.nullContext()));
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        resultBuilder.add(new DefaultIndex.Failure(e));
                    }
                }
                return null;
            }
        });
    }

    /**
     * Throws IOException - that the difference from standard Function
     * @param <T> return type
     */
    private static interface SearcherFunction<T>
    {
        public T apply(IndexSearcher reader) throws IOException;
    }

    /**
     * Takes care of opening/closing issueSearcher, and also wraps IOExceptions in
     * runtime exceptions - everythin to remove all that pollution from function (in future
     * try-with-resources may help)
     *
     * @param searcherFunction function that expects IndexSearcher
     * @param <T> expected return type from function
     * @return what SearcherFunction returned
     */
    private <T> T withIssueSearcher(SearcherFunction<T> searcherFunction)
    {
        try
        {
            IndexSearcher issueSearcher = issueIndexer.openIssueSearcher();
            try
            {
                T result = searcherFunction.apply(issueSearcher);

                return result;
            }
            finally
            {
                issueSearcher.close();
            }
        }
        catch (IOException x)
        {
            throw new RuntimeException(x);
        }
    }
}