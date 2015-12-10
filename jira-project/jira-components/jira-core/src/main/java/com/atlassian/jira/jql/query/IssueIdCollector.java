package com.atlassian.jira.jql.query;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import com.atlassian.jira.issue.index.DocumentConstants;

import com.google.common.collect.ImmutableSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.util.OpenBitSet;

/**
 * Collect Issue Ids for subquery searchers.
 *
 * @since v6.1
 */
public class IssueIdCollector extends Collector
{
    /** This value is set to a large (conservative) value by trial and error */
    private static final int SMALL_COLLECT_RATIO = 10000;
    final int maxDocs;
    private int docBase;
    final OpenBitSet docIds;

    /** This is the reader of the whole index (not each segment). */
    private final IndexReader indexReader;
    private int hitCount = 0;
    private int minDocId = Integer.MAX_VALUE;
    private int maxDocId = 0;

    public IssueIdCollector(final IndexReader indexReader)
    {
        this.indexReader = indexReader;
        this.maxDocs = indexReader.maxDoc();
        this.docIds = new OpenBitSet(maxDocs);
    }

    @Override
    public void setScorer(final Scorer scorer) throws IOException
    {
        // do nothing
    }

    @Override
    public void collect(final int doc) throws IOException
    {
        int index = docBase + doc;
        docIds.fastSet(index);
        hitCount++;
        minDocId = Math.min(minDocId, index);
        maxDocId = Math.max(maxDocId, index);
    }

    @Override
    public void setNextReader(final IndexReader reader, final int docBase) throws IOException
    {
        this.docBase = docBase;
    }

    @Override
    public boolean acceptsDocsOutOfOrder()
    {
        return true;
    }

    public Set<String> getIssueIds() throws IOException
    {
        int smallDocLimit = Math.max(50, maxDocs / SMALL_COLLECT_RATIO);
        if (hitCount == 0)
        {
            return ImmutableSet.of();
        }
        if (hitCount <= smallDocLimit)
        {
            return getIssueIdsDirectly();
        }
        else
        {
            return getIssueIdsByTerms();
        }
    }

    private Set<String> getIssueIdsDirectly() throws IOException
    {
        final Set<String> issueIds = new TreeSet<String>();
        FieldSelector selector = new FieldSelector()
        {
            @Override
            public FieldSelectorResult accept(final String fieldName)
            {
                return fieldName.equals(DocumentConstants.ISSUE_ID) ? FieldSelectorResult.LOAD_AND_BREAK : FieldSelectorResult.NO_LOAD;
            }
        };

        // Do a virtual trim of the bit set so we don't unnecessarily scan to the end
        docIds.setNumWords(OpenBitSet.bits2words(maxDocId + 1));

        for (int docId = docIds.nextSetBit(minDocId); docId >= 0; docId = docIds.nextSetBit(docId+1))
        {
            Document doc = indexReader.document(docId, selector);
            issueIds.add(doc.get(DocumentConstants.ISSUE_ID));
        }
        return issueIds;
    }


    private Set<String> getIssueIdsByTerms() throws IOException
    {
        final Set<String> issueIds = new TreeSet<String>();

        TermDocs termDocs = indexReader.termDocs();
        TermEnum termEnum = indexReader.terms(new Term(DocumentConstants.ISSUE_ID, ""));
        try
        {
            do
            {
                Term term = termEnum.term();
                // Lucene terms are interned so the != comparison is safe.
                if (term == null || term.field() != DocumentConstants.ISSUE_ID)
                {
                    // No comments. May happen
                    break;
                }
                String issueId = term.text();

                termDocs.seek(termEnum);
                while (termDocs.next())
                {
                    int doc = termDocs.doc();

                    if (docIds.get(doc))
                    {
                        issueIds.add(issueId);
                        break;
                    }
                }
            }
            while (termEnum.next());
        }
        finally
        {
            termDocs.close();
            termEnum.close();
        }
        return issueIds;
    }

    public Set<String> getAllIssueIds() throws IOException
    {
        final Set<String> issueIds = new TreeSet<String>();

        TermEnum termEnum = indexReader.terms(new Term(DocumentConstants.ISSUE_ID, ""));
        try
        {
            do
            {
                Term term = termEnum.term();
                // Lucene terms are interned so the != comparison is safe.
                if (term == null || term.field() != DocumentConstants.ISSUE_ID)
                {
                    // No comments. May happen
                    break;
                }
                String issueId = term.text();
                issueIds.add(issueId);
            }
            while (termEnum.next());
        }
        finally
        {
            termEnum.close();
        }
        return issueIds;
    }
}