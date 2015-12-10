package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.query.operator.Operator;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;

import java.util.Date;
import java.util.List;

/**
 * A query factory that handles equality operators for dates.
 *
 * @since v4.0
 */
public class DateEqualityQueryFactory extends AbstractDateOperatorQueryFactory implements OperatorSpecificQueryFactory
{
    private static final Logger log = Logger.getLogger(DateEqualityQueryFactory.class);

    private final JqlDateSupport jqlDateSupport;

    public DateEqualityQueryFactory(final JqlDateSupport jqlDateSupport)
    {
        super(jqlDateSupport);
        this.jqlDateSupport = jqlDateSupport;
    }

    public QueryFactoryResult createQueryForSingleValue(final String fieldName, final Operator operator, final List<QueryLiteral> rawValues)
    {
        if ((operator != Operator.EQUALS) && (operator != Operator.NOT_EQUALS))
        {
            log.debug(String.format("Creating an equality query for a single value for date field '%s' using unsupported operator: '%s', returning "
                    + "a false result (no issues). Supported operators are: '%s' and '%s'", fieldName, operator, Operator.EQUALS, Operator.NOT_EQUALS));          

            return QueryFactoryResult.createFalseResult();
        }

        return createResult(fieldName, operator, rawValues);
    }

    public QueryFactoryResult createQueryForMultipleValues(final String fieldName, final Operator operator, final List<QueryLiteral> rawValues)
    {
        if ((operator == Operator.IN) || (operator == Operator.NOT_IN))
        {
            return createResult(fieldName, operator, rawValues);
        }
        else
        {
            log.debug(String.format("Creating an equality query for multiple values for date field '%s' using unsupported operator: '%s', returning "
                        + "a false result (no issues). Supported operators are: '%s' and '%s'", fieldName, operator, Operator.IN, Operator.NOT_IN));

            return QueryFactoryResult.createFalseResult();
        }
    }

    private QueryFactoryResult createResult(final String fieldName, final Operator operator, final List<QueryLiteral> rawValues)
    {
        if ((operator == Operator.IN) || (operator == Operator.EQUALS))
        {
            return handleIn(fieldName, getDateValues(rawValues));
        }
        else if ((operator == Operator.NOT_IN) || (operator == Operator.NOT_EQUALS))
        {
            return handleNotIn(fieldName, getDateValues(rawValues));
        }
        else
        {
            return QueryFactoryResult.createFalseResult();
        }
    }

    public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
    {
        if ((operator == Operator.IS) || (operator == Operator.EQUALS))
        {
            return new QueryFactoryResult(getIsEmptyQuery(fieldName));
        }
        else if ((operator == Operator.IS_NOT) || (operator == Operator.NOT_EQUALS))
        {
            return new QueryFactoryResult(getIsNotEmptyQuery(fieldName));
        }
        else
        {

            log.debug(String.format("Creating an equality query for an empty value for date field '%s' using unsupported operator: '%s', returning "
                    + "a false result (no issues). Supported operators are: '%s','%s', '%s' and '%s'", fieldName, operator,
                    Operator.IS, Operator.EQUALS, Operator.IS_NOT, Operator.NOT_EQUALS));

            return QueryFactoryResult.createFalseResult();
        }
    }

    public boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY.contains(operator);
    }

    /*
     * Equals is represented by the range [date .. date + 1)
     */
    private Query handleEquals(final String fieldName, final Date value)
    {
        return new TermQuery(new Term(fieldName, jqlDateSupport.getIndexedValue(value)));
    }

    /*
     * Not equals is represented by the ranges [* .. date) and [date + 1 .. *] i.e. the inverse of Equals
     */
    private Query handleNotEquals(final String fieldName, final Date value)
    {
        final BooleanQuery combined = new BooleanQuery();
        final String indexedValue = jqlDateSupport.getIndexedValue(value);
        combined.add(new TermRangeQuery(fieldName, null, indexedValue, true, false), BooleanClause.Occur.SHOULD);
        combined.add(new TermRangeQuery(fieldName, indexedValue, null, false, true), BooleanClause.Occur.SHOULD);
        return combined;
    }

    /*
     * The IN operator is represented by a series of Equals clauses, ORed together
     */
    private QueryFactoryResult handleIn(final String fieldName, final List<Date> values)
    {
        if (values.size() == 1)
        {
            final Date date = values.get(0);
            final Query query = (date == null) ? getIsEmptyQuery(fieldName) : handleEquals(fieldName, date);
            return new QueryFactoryResult(query);
        }
        else
        {
            final BooleanQuery combined = new BooleanQuery();
            for (final Date value : values)
            {
                if (value == null)
                {
                    combined.add(getIsEmptyQuery(fieldName), BooleanClause.Occur.SHOULD);
                }
                else
                {
                    combined.add(handleEquals(fieldName, value), BooleanClause.Occur.SHOULD);
                }
            }
            return new QueryFactoryResult(combined);
        }
    }

    /*
     * The NOT IN operator is represented by a series of Not Equals clauses, ANDed together
     *
     * Note: this never requires a visibility query, since there are no negations.
     * - getIsNotEmptyQuery() generates a positive query
     * - handleNotEquals() generates range queries
     */
    private QueryFactoryResult handleNotIn(final String fieldName, final List<Date> values)
    {
        if (values.size() == 1)
        {
            final Date date = values.get(0);
            final Query query = (date == null) ? getIsNotEmptyQuery(fieldName) : handleNotEquals(fieldName, date);
            return new QueryFactoryResult(query);
        }
        else
        {
            final BooleanQuery combined = new BooleanQuery();
            for (final Date value : values)
            {
                if (value == null)
                {
                    combined.add(getIsNotEmptyQuery(fieldName), BooleanClause.Occur.SHOULD);
                }
                else
                {
                    combined.add(handleNotEquals(fieldName, value), BooleanClause.Occur.MUST);
                }
            }
            return new QueryFactoryResult(combined);
        }
    }

    private Query getIsEmptyQuery(final String fieldName)
    {
        // We are returning a query that will include empties by specifying a MUST_NOT occurrance.
        // We should add the visibility query so that we exclude documents which don't have fieldName indexed.
        final QueryFactoryResult result = new QueryFactoryResult(TermQueryFactory.nonEmptyQuery(fieldName), true);
        return QueryFactoryResult.wrapWithVisibilityQuery(fieldName, result).getLuceneQuery();
    }

    private Query getIsNotEmptyQuery(final String fieldName)
    {
        return TermQueryFactory.nonEmptyQuery(fieldName);
    }
}
