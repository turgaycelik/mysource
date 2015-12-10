package com.atlassian.jira.issue.search.parameters.lucene.sort;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Allows {@link JiraLuceneFieldFinder} to handle matched terms in a customised
 * way.
 *
 * @see JiraLuceneFieldFinder#getMatches(org.apache.lucene.index.IndexReader, String, MatchHandler)
 * @since v5.1
 */
@ExperimentalApi
public interface MatchHandler
{
    /**
     * Invoked by {@link JiraLuceneFieldFinder#getMatches(org.apache.lucene.index.IndexReader, String, MatchHandler)}
     * for each field value for each document.  The calls will be made in
     * order of increasing term values, with the document identifiers supplied
     * in an arbitrary order.
     *
     * @param doc the document identifier for the document that contains the term.  In
     *      JIRA, this indentifies a particular issue
     * @param termValue the value assigned to the term.  In JIRA, this is the value
     *      assigned to the field.
     */
    public void handleMatchedDocument(int doc, String termValue);
}
