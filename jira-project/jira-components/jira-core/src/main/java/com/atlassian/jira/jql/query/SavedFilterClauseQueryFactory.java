package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.SavedFilterResolver;
import com.atlassian.jira.jql.validator.SavedFilterCycleDetector;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;
import org.apache.log4j.Logger;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A query factory that will generate a query for a saved filter.
 *
 * @since v4.0
 */
@InjectableComponent
public class SavedFilterClauseQueryFactory implements ClauseQueryFactory
{
    private static final Logger log = Logger.getLogger(SavedFilterClauseQueryFactory.class);

    private final SavedFilterResolver savedFilterResolver;
    private final QueryRegistry queryRegistry;
    private final JqlOperandResolver jqlOperandResolver;
    private final SavedFilterCycleDetector savedFilterCycleDetector;
    private final WasClauseQueryFactory wasClauseQueryFactory;
    private final ChangedClauseQueryFactory changedClauseQueryFactory;

    public SavedFilterClauseQueryFactory(final SavedFilterResolver savedFilterResolver,
            final QueryRegistry queryRegistry, final JqlOperandResolver jqlOperandResolver,
            final SavedFilterCycleDetector savedFilterCycleDetector, final WasClauseQueryFactory wasClauseQueryFactory,
            final ChangedClauseQueryFactory changedClauseQueryFactory)
    {
        this.savedFilterResolver = savedFilterResolver;
        this.queryRegistry = queryRegistry;
        this.jqlOperandResolver = jqlOperandResolver;
        this.savedFilterCycleDetector = savedFilterCycleDetector;
        this.wasClauseQueryFactory = wasClauseQueryFactory;
        this.changedClauseQueryFactory = changedClauseQueryFactory;
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        notNull("queryCreationContext", queryCreationContext);

        if (!OperatorClasses.EQUALITY_OPERATORS.contains(terminalClause.getOperator()))
        {
            return QueryFactoryResult.createFalseResult();
        }

        final List<Query> queries = new ArrayList<Query>();
        final List<QueryLiteral> rawValues = jqlOperandResolver.getValues(queryCreationContext, terminalClause.getOperand(), terminalClause);
        final List<SearchRequest> matchingFilters = queryCreationContext.isSecurityOverriden() ?
                savedFilterResolver.getSearchRequestOverrideSecurity(rawValues)
                : savedFilterResolver.getSearchRequest(queryCreationContext.getUser(), rawValues);
        for (SearchRequest filter : matchingFilters)
        {
            if (savedFilterCycleDetector.containsSavedFilterReference(queryCreationContext.getQueryUser(), queryCreationContext.isSecurityOverriden(), filter, null))
            {
                log.debug(String.format("Saved filter with id '%d' contains a reference to itself; a query cannot be generated from this.", filter.getId()));
                return QueryFactoryResult.createFalseResult();
            }
            queries.add(getQueryFromSavedFilter(queryCreationContext, filter));
        }

        final Operator operator = terminalClause.getOperator();
        boolean mustNotOccur = Operator.NOT_EQUALS == operator || Operator.NOT_IN == operator;

        if (queries.isEmpty())
        {
            // Did not find any matching filters so we will return false
            return QueryFactoryResult.createFalseResult();
        }
        else if(queries.size() == 1)
        {
            // Only return one clause and let the negation be handled by the visitor
            return new QueryFactoryResult(queries.get(0), mustNotOccur);
        }
        else
        {
            // OR all the queries together and let the negation be handled by the visitor
            BooleanQuery boolQuery = new BooleanQuery();
            for (Query query : queries)
            {
                boolQuery.add(query, BooleanClause.Occur.SHOULD);
            }
            return new QueryFactoryResult(boolQuery, mustNotOccur);
        }
    }

    ///CLOVER:OFF
    Query getQueryFromSavedFilter(QueryCreationContext queryCreationContext, SearchRequest savedFilter)
    {
        if (savedFilter.getQuery().getWhereClause() == null)
        {
            return new MatchAllDocsQuery();
        }
        final QueryVisitor queryVisitor = new QueryVisitor(queryRegistry, queryCreationContext, wasClauseQueryFactory, changedClauseQueryFactory);
        return queryVisitor.createQuery(savedFilter.getQuery().getWhereClause());
    }
    ///CLOVER:ON

}
