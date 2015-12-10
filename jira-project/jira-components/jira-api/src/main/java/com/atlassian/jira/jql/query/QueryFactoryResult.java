package com.atlassian.jira.jql.query;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.containsNoNulls;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Represents the result of a call to the {@link ClauseQueryFactory#getQuery(QueryCreationContext,com.atlassian.query.clause.TerminalClause)}
 * method. The result contains the a Lucene Query and a flag to indicate whether or not the Lucene Query should be negated.
 * When the flag is set to true JIRA will automatically negate the Lucene Query when it is run in Lucene. 
 *
 * @since v4.0
 */
public final class QueryFactoryResult
{
    private static final QueryFactoryResult FALSE_RESULT = new QueryFactoryResult(new BooleanQuery(), false);

    private final Query luceneQuery;
    private final boolean mustNotOccur;

    /**
     * Creates a {@link QueryFactoryResult} instance that will return no results.
     * @return A {@link QueryFactoryResult} instance that will return no results.
     */
    public static QueryFactoryResult createFalseResult()
    {
        return FALSE_RESULT;
    }

    /**
     * @param fieldName the field to be visible
     * @param result the result to wrap
     * @return a new {@link com.atlassian.jira.jql.query.QueryFactoryResult} that combines the visibility query with
     * the input result, and that always has mustNotOccur() == false
     */
    static QueryFactoryResult wrapWithVisibilityQuery(final String fieldName, final QueryFactoryResult result)
    {
        // don't bother wrapping a false result because it will return nothing anyway
        if (FALSE_RESULT.equals(result))
        {
            return result;
        }
        
        final BooleanQuery finalQuery = new BooleanQuery();
        addToBooleanWithMust(result, finalQuery);
        finalQuery.add(TermQueryFactory.visibilityQuery(fieldName), BooleanClause.Occur.MUST);
        return new QueryFactoryResult(finalQuery);
    }

    /**
     * @param results a list of results you want to merge; must not be null or contain nulls
     * @return non-false results merged in a new boolean query with SHOULD. The result should never need negation, i.e.
     * {@link #mustNotOccur()} will always be false.
     */
    public static QueryFactoryResult mergeResultsWithShould(final List<QueryFactoryResult> results)
    {
        containsNoNulls("results", results);

        final BooleanQuery finalQuery = new BooleanQuery();
        for (QueryFactoryResult result : results)
        {
            if (!FALSE_RESULT.equals(result))
            {
                addToBooleanWithShould(result, finalQuery);
            }
        }
        
        return new QueryFactoryResult(finalQuery);
    }

    private static void addToBooleanWithMust(final QueryFactoryResult result, final BooleanQuery booleanQuery)
    {
        addToBooleanWithOccur(result, booleanQuery, BooleanClause.Occur.MUST);
    }

    private static void addToBooleanWithShould(final QueryFactoryResult result, final BooleanQuery booleanQuery)
    {
        addToBooleanWithOccur(result, booleanQuery, BooleanClause.Occur.SHOULD);
    }

    private static void addToBooleanWithOccur(final QueryFactoryResult result, final BooleanQuery booleanQuery, final BooleanClause.Occur occur)
    {
        if (result.mustNotOccur())
        {
            booleanQuery.add(result.getLuceneQuery(), BooleanClause.Occur.MUST_NOT);
        }
        else
        {
            booleanQuery.add(result.getLuceneQuery(), occur);
        }
    }

    /**
     * Default constructor that sets mustNotOccur to false.
     *
     * @param luceneQuery the query to wrap. Must not be null.
     * @throws IllegalArgumentException if luceneQuery is null.
     */
    public QueryFactoryResult(final Query luceneQuery)
    {
        this(luceneQuery, false);
    }

    /**
     * Create the result with the passed result and flag.
     *
     * @param luceneQuery the query to add. Must not be null.
     * @param mustNotOccur the flag to add to the result.
     * @throws IllegalArgumentException if luceneQuery is null.
     */
    public QueryFactoryResult(final Query luceneQuery, final boolean mustNotOccur)
    {
        this.luceneQuery = notNull("luceneQuery", luceneQuery);
        this.mustNotOccur = mustNotOccur;
    }

    public Query getLuceneQuery()
    {
        return luceneQuery;
    }

    public boolean mustNotOccur()
    {
        return mustNotOccur;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final QueryFactoryResult that = (QueryFactoryResult) o;

        if (mustNotOccur != that.mustNotOccur)
        {
            return false;
        }
        if (!luceneQuery.equals(that.luceneQuery))
        {
            return false;
        }

        return true;
    }

    ///CLOVER:OFF
    @Override
    public int hashCode()
    {
        int result = luceneQuery.hashCode();
        result = 31 * result + (mustNotOccur ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "QueryFactoryResult{" +
                "luceneQuery=" + luceneQuery +
                ", mustNotOccur=" + mustNotOccur +
                '}';
    }
    ///CLOVER:ON
}

