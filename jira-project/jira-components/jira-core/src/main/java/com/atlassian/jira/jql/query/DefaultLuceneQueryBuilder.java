package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.ClauseTooComplexSearchException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.util.LuceneQueryModifier;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.clause.Clause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

/**
 * Creates a Lucene Query from a JQL clause.
 *
 * @since v4.0
 */
@InjectableComponent
public class DefaultLuceneQueryBuilder implements LuceneQueryBuilder
{
    private final QueryRegistry queryRegistry;
    private final LuceneQueryModifier luceneQueryModifier;
    private final WasClauseQueryFactory wasClauseQueryFactory;
    private final ChangedClauseQueryFactory changedClauseQueryFactory;

    public DefaultLuceneQueryBuilder(QueryRegistry queryRegistry, LuceneQueryModifier luceneQueryModifier,
            WasClauseQueryFactory wasClauseQueryFactory, ChangedClauseQueryFactory changedClauseQueryFactory)
    {
        this.queryRegistry = queryRegistry;
        this.luceneQueryModifier = luceneQueryModifier;
        this.wasClauseQueryFactory = wasClauseQueryFactory;
        this.changedClauseQueryFactory = changedClauseQueryFactory;
    }

    public Query createLuceneQuery(QueryCreationContext queryCreationContext, Clause clause) throws SearchException
    {
        final QueryVisitor queryVisitor = createQueryVisitor(queryCreationContext);
        final Query luceneQuery;
        try
        {
            luceneQuery = queryVisitor.createQuery(clause);
        }
        catch (QueryVisitor.JqlTooComplex jqlTooComplex)
        {
            throw new ClauseTooComplexSearchException(jqlTooComplex.getClause());
        }

        //we need to process the returned query so that it will run in lucene correctly. For instance, we
        //will add positive queries where necessary so that negations work.
        try
        {
            return luceneQueryModifier.getModifiedQuery(luceneQuery);
        }
        catch (BooleanQuery.TooManyClauses tooManyClauses)
        {
            throw new ClauseTooComplexSearchException(clause);
        }
    }

    ///CLOVER:OFF
    QueryVisitor createQueryVisitor(QueryCreationContext context)
    {
        return new QueryVisitor(queryRegistry, context, wasClauseQueryFactory, changedClauseQueryFactory);
    }
    ///CLOVER:ON
}
