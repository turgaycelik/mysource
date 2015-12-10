package com.atlassian.jira.jql.query;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.query.lucene.parsing.LuceneQueryParserFactory;
import com.atlassian.query.operator.Operator;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.issue.search.util.TextTermEscaper.escape;

/**
 * A factory for creating a Query for the {@link com.atlassian.query.operator.Operator#LIKE equals operator}.
 *
 * @since v4.0
 */
public class LikeQueryFactory implements OperatorSpecificQueryFactory
{
    private static final Logger log = Logger.getLogger(LikeQueryFactory.class);

    private final boolean usesMainIndex;

    public LikeQueryFactory()
    {
        this.usesMainIndex = true;
    }

    public LikeQueryFactory(boolean usesMainIndex)
    {
        this.usesMainIndex = usesMainIndex;
    }

    public QueryFactoryResult createQueryForSingleValue(final String fieldName, final Operator operator, final List<QueryLiteral> rawValues)
    {
        if (operator != Operator.LIKE && operator != Operator.NOT_LIKE)
        {
            if (log.isDebugEnabled())
            {
                log.debug(String.format("Operator '%s' is not a LIKE operator.", operator.getDisplayString()));
            }
            return QueryFactoryResult.createFalseResult();
        }

        if (rawValues == null)
        {
            return QueryFactoryResult.createFalseResult();
        }

        return createResult(fieldName, rawValues, operator, usesMainIndex);
    }

    public QueryFactoryResult createResult(final String fieldName, final List<QueryLiteral> rawValues, final Operator operator, final boolean handleEmpty)
    {
        final List<Query> queries = getQueries(fieldName, rawValues);
        if (queries == null || queries.isEmpty())
        {
            return QueryFactoryResult.createFalseResult();
        }

        BooleanQuery fullQuery = new BooleanQuery();
        boolean hasEmpty = false;

        if (queries.size() == 1)
        {
            if (queries.get(0) == null && handleEmpty)
            {
                return createQueryForEmptyOperand(fieldName, operator);
            }
            else
            {
                fullQuery.add(queries.get(0), operator == Operator.NOT_LIKE ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.MUST);
            }
        }
        else
        {
            BooleanQuery subQuery = new BooleanQuery();
            for (Query query : queries)
            {
                if (query == null)
                {
                    hasEmpty = true;
                }
                else
                {
                    subQuery.add(query, operator == Operator.NOT_LIKE ? BooleanClause.Occur.MUST_NOT: BooleanClause.Occur.SHOULD);
                }
            }
            if (handleEmpty && hasEmpty)
            {
                subQuery.add(createQueryForEmptyOperand(fieldName, operator).getLuceneQuery(), operator == Operator.NOT_LIKE ? BooleanClause.Occur.MUST: BooleanClause.Occur.SHOULD);
            }
            fullQuery.add(subQuery, BooleanClause.Occur.MUST);
        }

        if (handleEmpty && !hasEmpty)
        {
            // For both LIKE and NOT_LIKE we need to add the exclude empty clause because their query could be a negative query
            // generated from the Lucene search syntax. We also need a visibility query in case this field is not searchable.
            fullQuery.add(TermQueryFactory.nonEmptyQuery(fieldName), BooleanClause.Occur.MUST);
            fullQuery.add(TermQueryFactory.visibilityQuery(fieldName), BooleanClause.Occur.MUST);
        }

        return new QueryFactoryResult(fullQuery);
    }

    private List<Query> getQueries(String fieldName, List<QueryLiteral> rawValues)
    {
        final QueryParser parser = getQueryParser(fieldName);
        parser.setDefaultOperator(QueryParser.Operator.AND);
        final List<Query> queries = new ArrayList<Query>();
        for (QueryLiteral rawValue : rawValues)
        {
            if (rawValue.isEmpty())
            {
                queries.add(null);
            }
            else if (!StringUtils.isBlank(rawValue.asString()))
            {
                final Query query;
                try
                {
                    final String value = getEscapedValueFromRawValues(rawValue);
                    query = parser.parse(value);
                }
                catch (final ParseException e)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug(String.format("Unable to parse the text '%s' for field '%s'.", rawValue.asString(), fieldName));
                    }
                    return null;
                }
                catch (final RuntimeException e)
                {
                    // JRA-27018  FuzzyQuery throws IllegalArgumentException instead of ParseException
                    if (log.isDebugEnabled())
                    {
                        log.debug(String.format("Unable to parse the text '%s' for field '%s'.", rawValue.asString(), fieldName));
                    }
                    return null;
                }
                queries.add(query);
            }
        }
        return queries;
    }

    @VisibleForTesting
    QueryParser getQueryParser(final String fieldName)
    {
        return ComponentAccessor.getComponent(LuceneQueryParserFactory.class).createParserFor(fieldName);
    }

    public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
    {
        if (operator == Operator.IS || operator == Operator.LIKE)
        {
            // We are returning a query that will include empties by specifying a MUST_NOT occurrance.
            // We should add the visibility query so that we exclude documents which don't have fieldName indexed.
            final QueryFactoryResult result = new QueryFactoryResult(TermQueryFactory.nonEmptyQuery(fieldName), true);
            return QueryFactoryResult.wrapWithVisibilityQuery(fieldName, result);
        }
        else if (operator == Operator.IS_NOT || operator == Operator.NOT_LIKE)
        {
            return new QueryFactoryResult(TermQueryFactory.nonEmptyQuery(fieldName));
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug(String.format("Create query for empty operand was called with operator '%s', this only handles '=', '!=', 'is' and 'not is'.", operator.getDisplayString()));
            }
            return QueryFactoryResult.createFalseResult();
        }
    }

    private String getEscapedValueFromRawValues(final QueryLiteral rawValue)
    {
        if (rawValue.isEmpty())
        {
            return null;
        }
        final String value = rawValue.asString();

        // NOTE: we need this so that we do not allow users to search a different field by specifying 'field:val'
        // we only want them to search the field they have specified via the JQL.
        return escape(value);
    }

    public QueryFactoryResult createQueryForMultipleValues(final String fieldName, final Operator operator, final List<QueryLiteral> rawValues)
    {
        if (log.isDebugEnabled())
        {
            log.debug("LIKE clauses do not support multi value operands.");
        }
        return QueryFactoryResult.createFalseResult();
    }

    public boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.TEXT_OPERATORS.contains(operator);
    }
}
