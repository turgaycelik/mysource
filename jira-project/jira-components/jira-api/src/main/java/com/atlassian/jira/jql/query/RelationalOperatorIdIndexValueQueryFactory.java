package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.operator.Operator;
import org.apache.log4j.Logger;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Generates queries for the operators '>', '>=', '<', '<='.
 *
 * @since v4.0
 */
public class RelationalOperatorIdIndexValueQueryFactory<T> extends AbstractOperatorQueryFactory<T> implements OperatorSpecificQueryFactory
{
    private static final Logger log = Logger.getLogger(RelationalOperatorIdIndexValueQueryFactory.class);

    private final Comparator<? super T> comparator;
    private final NameResolver<T> resolver;
    private final IndexInfoResolver<T> indexInfoResolver;

    public RelationalOperatorIdIndexValueQueryFactory(final Comparator<? super T> comparator, final NameResolver<T> resolver, final IndexInfoResolver<T> indexInfoResolver)
    {
        super(indexInfoResolver);
        this.indexInfoResolver = indexInfoResolver;
        this.comparator = Assertions.notNull("comparator", comparator);
        this.resolver = resolver;
    }

    public QueryFactoryResult createQueryForSingleValue(String fieldName, Operator operator, List<QueryLiteral> rawValues)
    {
        if (!handlesOperator(operator))
        {
            log.debug(String.format("Create query for single value was called with operator '%s', this only handles relational operators.", operator.getDisplayString()));
            return QueryFactoryResult.createFalseResult();
        }

        final List<String> indexValues = getIndexValues(rawValues);
        if (indexValues == null || indexValues.isEmpty())
        {
            return QueryFactoryResult.createFalseResult();
        }
        else if (indexValues.size() == 1)
        {
            final Query query = generateQueryForValue(fieldName, operator, indexValues.get(0));
            if (query == null)
            {
                return QueryFactoryResult.createFalseResult();
            }
            return new QueryFactoryResult(query);
        }
        else
        {
            BooleanQuery query = new BooleanQuery();
            for (String id : indexValues)
            {
                if (id != null)
                {
                    final Query subQuery = generateQueryForValue(fieldName, operator, id);
                    if (subQuery != null)
                    {
                        query.add(subQuery, BooleanClause.Occur.SHOULD);
                    }
                }
            }

            return checkQueryForEmpty(query);
        }
    }

    public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
    {
        log.debug("Empty operands are not supported by this query factory.");
        return QueryFactoryResult.createFalseResult();
    }

    public QueryFactoryResult createQueryForMultipleValues(String fieldName, Operator operator, List<QueryLiteral> listOfIds)
    {
        log.debug("Multi value operands are not supported by this query factory.");
        return QueryFactoryResult.createFalseResult();
    }

    public boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.RELATIONAL_ONLY_OPERATORS.contains(operator);
    }

    Query generateQueryForValue(String fieldName, Operator operator, String indexValue)
    {
        // We generate a false query if we are passed bad values
        // If we are passed null, this indicates that we should not generate a query
        if (indexValue == null)
        {
            return null;
        }
        // Lets turn the id into a domain object so that we can use it in the comparison
        final Long id = getValueAsLong(indexValue);
        final T domainObject = resolver.get(id);
        final Predicate<T> relationalOperatorMatch = createPredicate(operator, domainObject);
        return generateRangeQueryForPredicate(fieldName, relationalOperatorMatch);
    }

    /**
     * Override this method if you wish to have more than just relational operator predicate evaluation.
     *
     * @param operator the relational operator to use in comparisons
     * @param domainObject the domain object as the basis of all comparisons
     * @return a predicate which will exclude all domain objects that do not meet the comparison for the domain object
     */
    Predicate<T> createPredicate(final Operator operator, final T domainObject) 
    {
        return operator.getPredicateForValue(comparator, domainObject);
    }

    protected BooleanQuery generateRangeQueryForPredicate(final String fieldName, final Predicate<T> match)
    {
        final Collection<T> domainObjects = resolver.getAll();
        BooleanQuery bq = new BooleanQuery();
        for (T indexedObject : CollectionUtil.filter(domainObjects, match))
        {
            bq.add(getTermQuery(fieldName, indexInfoResolver.getIndexedValue(indexedObject)), BooleanClause.Occur.SHOULD);
        }
        return bq;
    }

    private Long getValueAsLong(final String value)
    {
        try
        {
            return new Long(value);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
