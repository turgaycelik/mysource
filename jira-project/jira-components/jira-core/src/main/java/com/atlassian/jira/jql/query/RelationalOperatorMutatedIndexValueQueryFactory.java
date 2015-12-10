package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.query.operator.Operator;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates relational queries for clauses with operands whose index value representation is based on mutated raw values
 * as opposed to domain values.
 *
 * @since v4.0
 */
public class RelationalOperatorMutatedIndexValueQueryFactory implements OperatorSpecificQueryFactory
{
    private static final Logger log = Logger.getLogger(RelationalOperatorMutatedIndexValueQueryFactory.class);

    private final IndexInfoResolver<?> indexInfoResolver;
    private final RangeQueryFactory<String> rangeQueryFactory = RangeQueryFactory.stringRangeQueryFactory();

    public RelationalOperatorMutatedIndexValueQueryFactory(final IndexInfoResolver<?> indexInfoResolver)
    {
        this.indexInfoResolver = indexInfoResolver;
    }

    public QueryFactoryResult createQueryForSingleValue(final String fieldName, final Operator operator, final List<QueryLiteral> rawValues)
    {
        if (!handlesOperator(operator))
        {
            log.debug(String.format("Integer operands do not support operator '%s'.", operator.getDisplayString()));
            return QueryFactoryResult.createFalseResult();
        }

        final List<String> mutatedValues = getIndexValues(rawValues);

        // most operators only expect one value
        // if we somehow got null as the value, don't error out but just return a false query
        if (mutatedValues.isEmpty() || (mutatedValues.get(0) == null))
        {
            return QueryFactoryResult.createFalseResult();
        }

        return new QueryFactoryResult(rangeQueryFactory.get(operator, fieldName, mutatedValues.get(0)));
    }

    public QueryFactoryResult createQueryForMultipleValues(final String fieldName, final Operator operator, final List<QueryLiteral> rawValues)
    {
        log.debug("Empty operands are not supported by this query factory.");
        return QueryFactoryResult.createFalseResult();
    }

    public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
    {
        log.debug("Multi value operands are not supported by this query factory.");
        return QueryFactoryResult.createFalseResult();
    }

    public boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.RELATIONAL_ONLY_OPERATORS.contains(operator);
    }

    /**
     * @param rawValues the raw values to convert
     * @return a list of index values in String form; never null, but may contain null values if empty literals were passed in.
     */
    List<String> getIndexValues(final List<QueryLiteral> rawValues)
    {
        if ((rawValues == null) || rawValues.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<String> indexValues = new ArrayList<String>();
        for (final QueryLiteral rawValue : rawValues)
        {
            if (rawValue != null)
            {
                final List<String> vals;
                // Turn the raw values into index values
                if (rawValue.getStringValue() != null)
                {
                    vals = indexInfoResolver.getIndexedValues(rawValue.getStringValue());
                }
                else if (rawValue.getLongValue() != null)
                {
                    vals = indexInfoResolver.getIndexedValues(rawValue.getLongValue());
                }
                else
                {
                    // Note: we expect that the IndexInfoResolver result above does not contain nulls, so when we
                    // add null here to the indexValues, this is signifying that an Empty query literal was seen
                    indexValues.add(null);
                    continue;
                }

                if ((vals != null) && !vals.isEmpty())
                {
                    // Just aggregate all the values together into one big list.
                    indexValues.addAll(vals);
                }
            }
        }
        return indexValues;
    }
}
