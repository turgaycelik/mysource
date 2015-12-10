package com.atlassian.jira.jql.query;

import com.atlassian.jira.datetime.LocalDate;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlLocalDateSupport;
import com.atlassian.jira.util.Function;
import com.atlassian.query.operator.Operator;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * A query factory that can handle relational operators for dates.
 *
 * @since v4.4
 */
public class LocalDateRelationalQueryFactory extends AbstractLocalDateOperatorQueryFactory implements OperatorSpecificQueryFactory
{
    private static final Logger log = Logger.getLogger(LocalDateRelationalQueryFactory.class);

    private final RangeQueryFactory<LocalDate> rangeQueryFactory;

    public LocalDateRelationalQueryFactory(final JqlLocalDateSupport jqlLocalDateSupport)
    {
        super(jqlLocalDateSupport);
        rangeQueryFactory = new RangeQueryFactory<LocalDate>(new Function<LocalDate, String>()
        {
            public String get(final LocalDate date)
            {
                return jqlLocalDateSupport.getIndexedValue(date);
            }
        });
    }

    public QueryFactoryResult createQueryForSingleValue(final String fieldName, final Operator operator, final List<QueryLiteral> rawValues)
    {
        if (!handlesOperator(operator))
        {
            log.debug(String.format("LocalDate operands do not support operator '%s'.", operator.getDisplayString()));
            return QueryFactoryResult.createFalseResult();
        }

        final List<LocalDate> dates = getLocalDateValues(rawValues);

        // if there were no parsable dates in the literals, the resultant list will be empty
        if (dates.isEmpty())
        {
            return QueryFactoryResult.createFalseResult();
        }

        // most operators only expect one value
        final LocalDate value = dates.get(0);

        // if we somehow got null as the value, don't error out but just return a false query
        if (value == null)
        {
            return QueryFactoryResult.createFalseResult();
        }

        return new QueryFactoryResult(rangeQueryFactory.get(operator, fieldName, value));
    }

    public QueryFactoryResult createQueryForMultipleValues(final String fieldName, final Operator operator, final List<QueryLiteral> rawValues)
    {
        log.debug("Multi value operands are not supported by this query factory.");
        return QueryFactoryResult.createFalseResult();
    }

    public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
    {
        log.debug("Empty operands are not supported by this query factory.");
        return QueryFactoryResult.createFalseResult();
    }

    public boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.RELATIONAL_ONLY_OPERATORS.contains(operator);
    }
}
