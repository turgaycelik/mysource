package com.atlassian.jira.issue.search.filters;

/**
 * This filter will return only the list of issues that match the issue Ids passed in.
 * <p>
 * This is useful for queries that query other data sources, before being combined with
 * an issue search (eg comment or change history).   It was removed with the JQL work undertaken in JIRA 4.0,
 * but has been resurrected to get rid of the too many clauses error that often accompany these searches  see JRA-22453
 *
 * @since v4.3
 */

import com.atlassian.jira.issue.index.DocumentConstants;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;

import java.io.IOException;
import java.util.Set;
import java.util.SortedSet;

/**
 *
 */
public class IssueIdFilter extends Filter
{
    private final Set<String> issuesIds;

    /**
     * @param issuesIds The list of issue ids to include in this filter
     */
    public IssueIdFilter(Set<String> issuesIds)
    {
        this.issuesIds = issuesIds;
    }

    @Override
    public DocIdSet getDocIdSet(IndexReader indexReader) throws IOException
    {
        OpenBitSet bits = new OpenBitSet(indexReader.maxDoc());
        TermDocs termDocs = indexReader.termDocs();
        // Seek through the term docs to see if we find each term
        for (String issueId : issuesIds)
        {
            Term term = new Term(DocumentConstants.ISSUE_ID, issueId);
            termDocs.seek(term);
            // There is only one document per issue so just get it.
            if (termDocs.next())
            {
                bits.set(termDocs.doc());
            }
        }
        return bits;
    }
}
