package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.query.operator.Operator;
import org.apache.log4j.Logger;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v4.0
 */
public abstract class AbstractEqualityQueryFactory<T> extends AbstractOperatorQueryFactory<T> implements OperatorSpecificQueryFactory
{
    private static final Logger log = Logger.getLogger(AbstractEqualityQueryFactory.class);

    public AbstractEqualityQueryFactory(final IndexInfoResolver<T> indexInfoResolver)
    {
        super(indexInfoResolver);
    }

    public QueryFactoryResult createQueryForSingleValue(final String fieldName, final Operator operator, final List<QueryLiteral> rawValues)
    {
        if (Operator.EQUALS.equals(operator))
        {
            return handleEquals(fieldName, getIndexValues(rawValues));
        }
        else if (Operator.NOT_EQUALS.equals(operator))
        {
            return handleNotEquals(fieldName, getIndexValues(rawValues));
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug("Create query for single value was called with operator '" + operator.getDisplayString() + "', this only handles '=' and '!='.");
            }
            return QueryFactoryResult.createFalseResult();
        }
    }

    public QueryFactoryResult createQueryForMultipleValues(final String fieldName, final Operator operator, final List<QueryLiteral> rawValues)
    {
        if (Operator.IN.equals(operator))
        {
            return handleEquals(fieldName, getIndexValues(rawValues));
        }
        else if (Operator.NOT_IN.equals(operator))
        {
            return handleNotEquals(fieldName, getIndexValues(rawValues));
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug("Create query for multiple value was called with operator '" + operator.getDisplayString() + "', this only handles 'in'.");
            }
            return QueryFactoryResult.createFalseResult();
        }
    }

    public boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY.contains(operator);
    }

    QueryFactoryResult handleNotEquals(final String fieldName, final List<String> indexValues)
    {
        List<Query> notQueries = new ArrayList<Query>();

        if (indexValues != null)
        {
            for (String indexValue : indexValues)
            {
                // don't bother keeping track of empty literals - empty query gets added later anyway
                if (indexValue != null)
                {
                    notQueries.add(getTermQuery(fieldName, indexValue));
                }
            }
        }
        if (notQueries.isEmpty())
        {
            // if we didn't find non-empty literals, then return the isNotEmpty query
            return new QueryFactoryResult(getIsNotEmptyQuery(fieldName));
        }
        else
        {
            BooleanQuery boolQuery = new BooleanQuery();
            // Because this is a NOT equality query we are generating we always need to explicity exclude the
            // EMPTY results from the query we are generating.
            boolQuery.add(getIsNotEmptyQuery(fieldName), BooleanClause.Occur.MUST);

            // Add all the not queries that were specified by the user.
            for (Query query : notQueries)
            {
                boolQuery.add(query, BooleanClause.Occur.MUST_NOT);
            }

            // We should add the visibility query so that we exclude documents which don't have fieldName indexed.
            boolQuery.add(TermQueryFactory.visibilityQuery(fieldName), BooleanClause.Occur.MUST);

            return new QueryFactoryResult(boolQuery, false);
        }
    }

    QueryFactoryResult handleEquals(final String fieldName, final List<String> indexValues)
    {
        if (indexValues == null)
        {
            return QueryFactoryResult.createFalseResult();
        }
        if (indexValues.size() == 1)
        {
            final String id = indexValues.get(0);
            return (id == null) ? new QueryFactoryResult(getIsEmptyQuery(fieldName)) : new QueryFactoryResult(getTermQuery(fieldName, id));
        }
        else
        {
            BooleanQuery orQuery = new BooleanQuery();
            for (String id : indexValues)
            {
                if (id != null)
                {
                    orQuery.add(getTermQuery(fieldName, id), BooleanClause.Occur.SHOULD);
                }
                else
                {
                    orQuery.add(getIsEmptyQuery(fieldName), BooleanClause.Occur.SHOULD);
                }
            }

            return new QueryFactoryResult(orQuery);
        }
    }

    /**
     * Get the query for the concrete impl class that means "give me all results for this field that are empty".
     * This query will also include an additional visibility query if necessary. For example, a negative query such as
     * <code>-nonemptyfieldids=duedate</code> will be combined with <code>visiblefields:duedate</code>. Hence, further
     * wrapping is not required.
     *
     * @param fieldName the field to search on empty.
     * @return a lucene Query, possibly combined with a visibility query
     */
    abstract Query getIsEmptyQuery(final String fieldName);

    /**
     * Get the query for the concrete impl class that means "give me all results for this field that are not empty".
     * This query will also include an additional visibility query if necessary. For example, a negative query such as
     * <code>-priority=-1</code> will be combined with <code>visiblefields:priority</code>. Hence, further
     * wrapping is not required.
     *
     * @param fieldName the field to search on empty.
     * @return a lucene Query, possibly combined with a visibility query
     */
    abstract Query getIsNotEmptyQuery(final String fieldName);
}
