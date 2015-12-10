package com.atlassian.jira.issue.search.parameters.lucene.sort;

import com.atlassian.jira.issue.Issue;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

/**
  * A means of comparing two issue documents in an index, using Lucene.  Although implementations of this interface
  * are free to look up the issue from the database for sorting, this is an order of magnitude slower than
  * accessing the index directly.
  */
public interface IssueSortComparator
{
    /**
     * Compare 2 issues.
     * @param issue1 First Issue
     * @param issue2 Second Issue
     * @return -1,0 or 1 depending upon this comparators determination if issue 1 should sort before, equal to or after issue 2.
     */
    public int compare(Issue issue1, Issue issue2);

    /**
     * Get an Issue object from a Lucene Document
     *
     * @param document A lucene document, that must be an Issue document
     * @return An Issue Object
     */
    public Issue getIssueFromDocument(Document document);
}
