package com.atlassian.jira.issue.search.util;

import com.atlassian.jira.util.dbc.Assertions;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v4.0
 */
public class DefaultLuceneQueryModifier implements LuceneQueryModifier
{
    public Query getModifiedQuery(final Query originalQuery)
    {
        Assertions.notNull("originalQuery", originalQuery);

        if (originalQuery instanceof BooleanQuery)
        {
            return transformBooleanQuery((BooleanQuery)originalQuery);
        }
        return originalQuery;
    }

    private BooleanQuery transformBooleanQuery(final BooleanQuery originalQuery)
    {
        // See what kind of clauses our current BooleanQuery has
        final QueryBucket queryBucket = new QueryBucket(originalQuery);

        if (queryBucket.containsOnlyNot())
        {
            // Case 1
            return handleOnlyNot(queryBucket);
        }
        else if (queryBucket.containsMust())
        {
            // Case 2
            return handleContainsMust(queryBucket);
        }
        else if (queryBucket.containsShould())
        {
            // Case 3
            return handleContainsShould(queryBucket);
        }

        // The query was empty
        return new BooleanQuery();
    }

    // Handles Case 3 as described in the interface
    private BooleanQuery handleContainsShould(final QueryBucket queryBucket)
    {
        BooleanQuery query = new BooleanQuery();
        final BooleanQuery originalQuery = queryBucket.getOriginalBooleanQuery();
        query.setBoost(originalQuery.getBoost());
        query.setMinimumNumberShouldMatch(originalQuery.getMinimumNumberShouldMatch());

        // Add all the positive SHOULD queries making sure to complete a deep dive on the BooleanQueries
        for (Query shouldQuery : queryBucket.getShouldQueries())
        {
            if (shouldQuery instanceof BooleanQuery)
            {
                query.add(transformBooleanQuery((BooleanQuery) shouldQuery), BooleanClause.Occur.SHOULD);
            }
            else
            {
                query.add((Query) shouldQuery.clone(), BooleanClause.Occur.SHOULD);
            }
        }

        // Handle all the MUST_NOT queries expanding them as needed
        for (Query origNotQuery : queryBucket.getNotQueries())
        {
            // We need to expand this to a BooleanQuery that contains a MatchAll
            BooleanQuery notWithMatchAll = new BooleanQuery();
            Query notQuery = (Query) origNotQuery.clone();
            notWithMatchAll.setBoost(notQuery.getBoost());
            notWithMatchAll.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
            // Reset this clauses boost since we have moved the boost onto the new parent BooleanClause
            notQuery.setBoost(1);

            if (notQuery instanceof BooleanQuery)
            {
                notWithMatchAll.setMinimumNumberShouldMatch(((BooleanQuery)notQuery).getMinimumNumberShouldMatch());
                // Reset this on the current query since the new parent BooleanClause is taking over its value
                ((BooleanQuery)notQuery).setMinimumNumberShouldMatch(0);
                notWithMatchAll.add(transformBooleanQuery((BooleanQuery) notQuery), BooleanClause.Occur.MUST_NOT);
            }
            else
            {
                notWithMatchAll.add(notQuery, BooleanClause.Occur.MUST_NOT);
            }
            query.add(notWithMatchAll, BooleanClause.Occur.SHOULD);
        }

        return query;
    }

    // Handles Case 2 as described in the interface
    private BooleanQuery handleContainsMust(final QueryBucket queryBucket)
    {
        BooleanQuery query = new BooleanQuery();

        // There is nothing to do here except complete a deep dive on each query
        final BooleanQuery originalBooleanQuery = queryBucket.getOriginalBooleanQuery();
        query.setBoost(originalBooleanQuery.getBoost());
        query.setMinimumNumberShouldMatch(originalBooleanQuery.getMinimumNumberShouldMatch());
        final BooleanClause[] booleanClauses = originalBooleanQuery.getClauses();
        for (BooleanClause booleanClause : booleanClauses)
        {
            final Query subQuery = booleanClause.getQuery();
            final BooleanClause.Occur subOccur = booleanClause.getOccur();
            if (subQuery instanceof BooleanQuery)
            {
                query.add(transformBooleanQuery((BooleanQuery) subQuery), subOccur);
            }
            else
            {
                query.add((Query) subQuery.clone(), subOccur);
            }
        }
        return query;
    }

    // Handles Case 1 as described in the interface
    private BooleanQuery handleOnlyNot(final QueryBucket queryBucket)
    {
        BooleanQuery query = new BooleanQuery();

        BooleanQuery originalQuery = queryBucket.getOriginalBooleanQuery();
        query.setBoost(originalQuery.getBoost());
        query.setMinimumNumberShouldMatch(originalQuery.getMinimumNumberShouldMatch());
        // We always add a match all in this case
        query.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
        for (Query origNotQuery : queryBucket.getNotQueries())
        {
            Query notQuery = (Query) origNotQuery.clone();
            // Make sure we continue to dive the tree if we need to
            if (notQuery instanceof BooleanQuery)
            {
                query.add(transformBooleanQuery((BooleanQuery) notQuery), BooleanClause.Occur.MUST_NOT);
            }
            else
            {
                query.add(notQuery, BooleanClause.Occur.MUST_NOT);
            }
        }
        return query;
    }

    /**
     * Takes the BooleanClauses and puts them into MUST_NOT, MUST, and SHOULD buckets.
     */
    private static class QueryBucket
    {
        private final List<Query> notQueries;
        private final List<Query> mustQueries;
        private final List<Query> shouldQueries;
        private final BooleanQuery booleanQuery;

        public QueryBucket(BooleanQuery booleanQuery)
        {
            this.booleanQuery = booleanQuery;
            this.notQueries = new ArrayList<Query>();
            this.mustQueries = new ArrayList<Query>();
            this.shouldQueries = new ArrayList<Query>();
            init(booleanQuery.getClauses());
        }

        public BooleanQuery getOriginalBooleanQuery()
        {
            return booleanQuery;
        }

        public boolean containsOnlyNot()
        {
            return !notQueries.isEmpty() && mustQueries.isEmpty() && shouldQueries.isEmpty();
        }

        public boolean containsMust()
        {
            return !mustQueries.isEmpty();
        }

        public boolean containsShould()
        {
            return !shouldQueries.isEmpty();
        }

        public List<Query> getNotQueries()
        {
            return notQueries;
        }

        public List<Query> getShouldQueries()
        {
            return shouldQueries;
        }

        private void init(final BooleanClause[] booleanClauses)
        {
            // Run through all the clauses and bucket them by the occurances we encounter
            for (BooleanClause booleanClause : booleanClauses)
            {
                final BooleanClause.Occur clauseOccur = booleanClause.getOccur();
                final Query clauseQuery = booleanClause.getQuery();

                if (BooleanClause.Occur.MUST_NOT.equals(clauseOccur))
                {
                    // We don't want to add these right away since we may be re-writing the single terms into BooleanQueries with a MatchAll
                    notQueries.add(clauseQuery);
                }
                else if (BooleanClause.Occur.MUST.equals(clauseOccur))
                {
                    mustQueries.add(clauseQuery);
                }
                else if (BooleanClause.Occur.SHOULD.equals(clauseOccur))
                {
                    shouldQueries.add(clauseQuery);
                }
            }
        }

    }
}
