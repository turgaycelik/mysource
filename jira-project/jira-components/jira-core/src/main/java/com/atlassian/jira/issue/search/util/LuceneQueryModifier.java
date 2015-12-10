package com.atlassian.jira.issue.search.util;

import org.apache.lucene.search.Query;

/**
 * This class will clone the {@link org.apache.lucene.search.Query} and add a
 * {@link org.apache.lucene.search.MatchAllDocsQuery} to the portion of the query that require them.
 *
 * This inspects the query to determine if there are any nodes in the query that are marked as
 * {@link org.apache.lucene.search.BooleanClause.Occur#MUST_NOT} AND they do not have a positive query to work
 * against.
 *
 * This is because Lucene will drop queries of this kind instead of trying to find the correct result.
 *
 * When we specify a query that is -A || B lucene treats this as equivilent to B. When we specify this query what we
 * mean is (-A && ALL_VALUES) || B which is obviously not equivilent to B.
 *
 * The algorithm for determining if a {@link org.apache.lucene.search.BooleanQuery} should have a
 * {@link org.apache.lucene.search.MatchAllDocsQuery} added to it with an occur of
 * {@link org.apache.lucene.search.BooleanClause.Occur#MUST_NOT} is:
 *
 * Case 1: BooleanQuery contains only {@link org.apache.lucene.search.BooleanClause.Occur#MUST_NOT} clauses THEN add a
 * {@link org.apache.lucene.search.MatchAllDocsQuery}
 *
 * Case 2: BooleanQuery contains at least one {@link org.apache.lucene.search.BooleanClause.Occur#MUST} and no
 * {@link org.apache.lucene.search.BooleanClause.Occur#SHOULD} THEN do not add a {@link org.apache.lucene.search.MatchAllDocsQuery}
 *
 * Case 3: BooleanQuery contains at least one {@link org.apache.lucene.search.BooleanClause.Occur#SHOULD} THEN
 * add a {@link org.apache.lucene.search.MatchAllDocsQuery} to each
 * {@link org.apache.lucene.search.BooleanClause.Occur#MUST_NOT} portion of the query. This may mean that we need to
 * rewrite the a single term to be a BooleanQuery that contains the single term AND the {@link org.apache.lucene.search.MatchAllDocsQuery}.
 *
 * NOTE: A BooleanQuery that contains at least one {@link org.apache.lucene.search.BooleanClause.Occur#MUST} and at least
 * one {@link org.apache.lucene.search.BooleanClause.Occur#SHOULD} is the same as Case 2 since the MUST portion of the
 * query will provide a positive set of results.
 *
 * @since v4.0
 */
public interface LuceneQueryModifier
{
    /**
     * Will clone and rewrite the query as per the rules defined above.
     *
     * @param originalQuery defines the lucene query to inspect, must not be null.
     * @return the modified query that will return the right results when run.
     */
    Query getModifiedQuery(Query originalQuery);
}
