package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.clause.Clause;
import org.apache.lucene.search.Query;

/**
 * Converts a JQL query into an lucene query for searching JIRA lucene index.
 *
 * This should be used over using the QueryVisitor directly
 *
 * @since v4.0
 */
@InjectableComponent
public interface LuceneQueryBuilder
{
    /**
     * Converts a JQL {@link com.atlassian.query.clause.Clause} into an lucene {@link org.apache.lucene.search.Query} for searching JIRA lucene index.
     * @param queryCreationContext the secutiry context under which the lucene query should be generated
     * @param clause the JQL clause to convert into a lucene query
     * @return the lucene query generated from the {@link com.atlassian.query.clause.Clause}, Never null.
     * @throws com.atlassian.jira.issue.search.SearchException in case of query generation error
     */
    Query createLuceneQuery(QueryCreationContext queryCreationContext, Clause clause) throws SearchException;
}
