package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.IndexValueConverter;
import com.atlassian.query.operator.Operator;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Creates equality queries for clauses whose value is exactly the same as the indexed value(e.g. votes and duration).
 *
 * @since v4.0
 */
public class ActualValueEqualityQueryFactory extends AbstractActualValueOperatorQueryFactory
        implements OperatorSpecificQueryFactory
{
    private static final Logger log = Logger.getLogger(ActualValueEqualityQueryFactory.class);

    private final String emptyIndexValue;

    /**
     * Creates a Query Factory that represents Empty values with emptyIndexValue
     *
     * @param indexValueConverter used for converting query literals to the index representation
     * @param emptyIndexValue the value that is used to represent empty values in the index
     */
    public ActualValueEqualityQueryFactory(final IndexValueConverter indexValueConverter, final String emptyIndexValue)
    {
        super(indexValueConverter);
        this.emptyIndexValue = notNull("emptyIndexValue", emptyIndexValue);
    }

    /**
     * Creates a Query Factory that does not have a specified representation of empty values and instead checks for the
     * field value absence.
     *
     * @param indexValueConverter used for converting query literals to the index representation
     */
    public ActualValueEqualityQueryFactory(final IndexValueConverter indexValueConverter)
    {
        super(indexValueConverter);
        this.emptyIndexValue = null;
    }

    public QueryFactoryResult createQueryForSingleValue(final String fieldName, final Operator operator, final List<QueryLiteral> rawValues)
    {
        if (operator != Operator.EQUALS && operator != Operator.NOT_EQUALS)
        {
            log.debug(String.format("Creating an equality query for a single value for field '%s' using unsupported operator: '%s', returning "
                    + "a false result (no issues). Supported operators are: '%s' and '%s'", fieldName, operator, Operator.EQUALS, Operator.NOT_EQUALS));

            return QueryFactoryResult.createFalseResult();
        }
        return createResult(fieldName, operator, rawValues);
    }

    public QueryFactoryResult createQueryForMultipleValues(final String fieldName, final Operator operator, final List<QueryLiteral> rawValues)
    {
        if (operator == Operator.IN || operator == Operator.NOT_IN)
        {
            return createResult(fieldName, operator, rawValues);
        }
        else
        {
            log.debug(String.format("Creating an equality query for multiple values for field '%s' using unsupported operator: '%s', returning "
                    + "a false result (no issues). Supported operators are: '%s' and '%s'", fieldName, operator, Operator.IN, Operator.NOT_IN));

            return QueryFactoryResult.createFalseResult();
        }
    }

    public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
    {
        if (operator == Operator.IS || operator == Operator.EQUALS)
        {
            return new QueryFactoryResult(getIsEmptyQuery(fieldName));
        }
        else if (operator == Operator.IS_NOT || operator == Operator.NOT_EQUALS)
        {
            return new QueryFactoryResult(getIsNotEmptyQuery(fieldName));
        }
        else
        {
            log.debug(String.format("Creating an equality query for an empty value for field '%s' using unsupported operator: '%s', returning "
                    + "a false result (no issues). Supported operators are: '%s','%s', '%s' and '%s'", fieldName, operator,
                    Operator.IS, Operator.EQUALS, Operator.IS_NOT, Operator.NOT_EQUALS));

            return QueryFactoryResult.createFalseResult();
        }
    }

    public boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY.contains(operator);
    }

    private QueryFactoryResult createResult(final String fieldName, final Operator operator, final List<QueryLiteral> rawValues)
    {
        if (operator == Operator.IN || operator == Operator.EQUALS)
        {
            return handleIn(fieldName, getIndexValues(rawValues));
        }
        else if (operator == Operator.NOT_IN || operator == Operator.NOT_EQUALS)
        {
            return handleNotIn(fieldName, getIndexValues(rawValues));
        }
        else
        {
            return QueryFactoryResult.createFalseResult();
        }
    }

    /*
     * The IN operator is represented by a series of Equals clauses, ORed together.
     */
    private QueryFactoryResult handleIn(final String fieldName, final List<String> values)
    {
        if (values.size() == 1)
        {
            final String value = values.get(0);
            Query query = (value == null) ? getIsEmptyQuery(fieldName) : getTermQuery(fieldName, value);
            return new QueryFactoryResult(query);
        }
        else
        {
            BooleanQuery combined = new BooleanQuery();
            for (String value : values)
            {
                if (value == null)
                {
                    combined.add(getIsEmptyQuery(fieldName), BooleanClause.Occur.SHOULD);
                }
                else
                {
                    combined.add(getTermQuery(fieldName, value), BooleanClause.Occur.SHOULD);
                }
            }
            return new QueryFactoryResult(combined);
        }
    }

    /*
     * The NOT IN operator is represented by a series of Not Equals clauses, ANDed together
     */
    private QueryFactoryResult handleNotIn(final String fieldName, final List<String> values)
    {
        List<Query> notQueries = new ArrayList<Query>();

        for (String indexValue : values)
        {
            // don't bother keeping track of empty literals - empty query gets added later anyway
            if (indexValue != null)
            {
                notQueries.add(getTermQuery(fieldName, indexValue));
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

    private Query getIsEmptyQuery(final String fieldName)
    {
        if (emptyIndexValue != null)
        {
            return getTermQuery(fieldName, emptyIndexValue);
        }
        else
        {
            // We are returning a query that will include empties by specifying a MUST_NOT occurrance.
            // We should add the visibility query so that we exclude documents which don't have fieldName indexed.
            QueryFactoryResult result = new QueryFactoryResult(TermQueryFactory.nonEmptyQuery(fieldName), true);
            return QueryFactoryResult.wrapWithVisibilityQuery(fieldName, result).getLuceneQuery();
        }
    }

    private Query getIsNotEmptyQuery(final String fieldName)
    {
        if (emptyIndexValue != null)
        {
            // We are returning a query that will exclude empties by specifying a MUST_NOT occurrance.
            // We should add the visibility query so that we exclude documents which don't have fieldName indexed.
            QueryFactoryResult result = new QueryFactoryResult(getTermQuery(fieldName, emptyIndexValue), true);
            return QueryFactoryResult.wrapWithVisibilityQuery(fieldName, result).getLuceneQuery();
        }
        else
        {
            return TermQueryFactory.nonEmptyQuery(fieldName);
        }
    }

    private TermQuery getTermQuery(final String fieldName, final String value)
    {
        return new TermQuery(new Term(fieldName, value));
    }
}
