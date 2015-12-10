package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.query.operator.Operator;
import org.apache.log4j.Logger;
import org.apache.lucene.search.Query;

/**
 * Used to generate equality lucene queries. When this searches for EMPTY values it will search the index for the
 * provided fieldName with the value that is provdied to represent an empty value.
 *
 * @since v4.0
 */
public class EqualityWithSpecifiedEmptyValueQueryFactory<T> extends AbstractEqualityQueryFactory<T>
{
    private static final Logger log = Logger.getLogger(EqualityWithSpecifiedEmptyValueQueryFactory.class);
    
    private final String emptyValue;

    public EqualityWithSpecifiedEmptyValueQueryFactory(final IndexInfoResolver<T> indexInfoResolver, String emptyValue)
    {
        super(indexInfoResolver);
        this.emptyValue = emptyValue;
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
            log.debug(String.format("Cannot create a query for an empty operand using the operator '%s'", operator.getDisplayString()));
            return QueryFactoryResult.createFalseResult();
        }
    }

    Query getIsEmptyQuery(final String fieldName)
    {
        return getTermQuery(fieldName, emptyValue);
    }

    Query getIsNotEmptyQuery(final String fieldName)
    {
        // We are returning a query that will exclude empties by specifying a MUST_NOT occurrance.
        // We should add the visibility query so that we exclude documents which don't have fieldName indexed.
        final QueryFactoryResult result = new QueryFactoryResult(getTermQuery(fieldName, emptyValue), true);
        return QueryFactoryResult.wrapWithVisibilityQuery(fieldName, result).getLuceneQuery();
    }
}
