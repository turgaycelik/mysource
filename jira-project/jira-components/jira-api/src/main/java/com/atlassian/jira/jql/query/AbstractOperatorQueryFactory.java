package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for operator query factories that can generate a query for a fieldName and a predicate.
 *
 * @since v4.0
 */
public abstract class AbstractOperatorQueryFactory<T>
{
    private final IndexInfoResolver indexInfoResolver;

    protected AbstractOperatorQueryFactory(IndexInfoResolver indexInfoResolver)
    {
        this.indexInfoResolver = indexInfoResolver;
    }

    protected TermQuery getTermQuery(String fieldName, String indexValue)
    {
        return new TermQuery(new Term(fieldName, indexValue));
    }

    protected QueryFactoryResult checkQueryForEmpty(BooleanQuery query)
    {
        // There is a special case where we were unable to resolve any of the id's provided to this method in
        // listOfIds and we therefore need to return a false query. If we were only unable to resolve some of the
        // id's this does not matter since they are all OR'ed together and TRUE || FALSE = TRUE
        if (query.clauses().isEmpty())
        {
            return QueryFactoryResult.createFalseResult();
        }
        return new QueryFactoryResult(query);
    }

    /**
     * @param rawValues the raw values to convert
     * @return a list of index values in String form; never null, but may contain null values if empty literals were passed in.
     */
    List<String> getIndexValues(List<QueryLiteral> rawValues)
    {
        if (rawValues == null || rawValues.isEmpty())
        {
            return Collections.emptyList();
        }
        
        List<String> indexValues = new ArrayList<String>();
        for (QueryLiteral rawValue : rawValues)
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

                if (vals != null && !vals.isEmpty())
                {
                    // Just aggregate all the values together into one big list.
                    indexValues.addAll(vals);
                }
            }
        }
        return indexValues;
    }
}
