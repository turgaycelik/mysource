package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.index.indexers.impl.BaseFieldIndexer;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.IssueSecurityLevelResolver;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.clause.TerminalClause;
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
 * A query factory that will generate a query for a issue security levels.
 *
 * @since v4.0
 */
@InjectableComponent
public class IssueSecurityLevelClauseQueryFactory implements ClauseQueryFactory
{
    private static final Logger log = Logger.getLogger(IssueSecurityLevelClauseQueryFactory.class);
    private static final String indexFieldName = SystemSearchConstants.forSecurityLevel().getIndexField();

    private final IssueSecurityLevelResolver issueSecurityLevelResolver;
    private final JqlOperandResolver jqlOperandResolver;

    public IssueSecurityLevelClauseQueryFactory(final IssueSecurityLevelResolver issueSecurityLevelResolver,
            final JqlOperandResolver jqlOperandResolver)
    {
        this.issueSecurityLevelResolver = notNull("issueSecurityLevelResolver", issueSecurityLevelResolver);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        notNull("queryCreationContext", queryCreationContext);
        final Operator operator = terminalClause.getOperator();

        final List<String> securityLevelIds = transformRawValuesIntoIds(queryCreationContext, terminalClause);
        return createQueryForValues(operator, securityLevelIds);
    }

    QueryFactoryResult createQueryForValues(final Operator operator, final List<String> securityLevelIds)
    {
        if (isPositiveOperator(operator))
        {
            return handleIn(securityLevelIds);
        }
        else if (isNegationOperator(operator))
        {
            return handleNotIn(securityLevelIds);
        }
        else
        {
            log.debug(String.format("Issue Security Level operands do not support operator '%s'.", operator.getDisplayString()));
            return QueryFactoryResult.createFalseResult();
        }
    }

    private List<String> transformRawValuesIntoIds(final QueryCreationContext queryCreationContext, final TerminalClause clause)
    {
        final List<QueryLiteral> rawValues = jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause);
        final List<String> idStrings = new ArrayList<String>();

        if (rawValues != null)
        {
            final List<IssueSecurityLevel> securityLevels = queryCreationContext.isSecurityOverriden() ?
                    issueSecurityLevelResolver.getIssueSecurityLevelsOverrideSecurity(rawValues)
                    : issueSecurityLevelResolver.getIssueSecurityLevels(queryCreationContext.getQueryUser(), rawValues);
            for (IssueSecurityLevel securityLevel : securityLevels)
            {
                if (securityLevel != null)
                {
                    idStrings.add(securityLevel.getId().toString());
                }
                else
                {
                    // null security level indicates empty literal
                    idStrings.add(null);
                }
            }
        }
        return idStrings;
    }

    /*
     * The IN operator is represented by a series of Equals clauses, ORed together
     */
    private QueryFactoryResult handleIn(final List<String> values)
    {
        if (values.size() == 1)
        {
            final String value = values.get(0);
            return new QueryFactoryResult(getPossibleTermQuery(value));
        }
        else
        {
            BooleanQuery combined = new BooleanQuery();
            for (String value : values)
            {
                combined.add(getPossibleTermQuery(value), BooleanClause.Occur.SHOULD);
            }
            return new QueryFactoryResult(combined);
        }
    }

    /*
     * Note: does not require wrapping as security is always visible/searchable
     */
    private QueryFactoryResult handleNotIn(final List<String> values)
    {
        List<Query> notQueries = new ArrayList<Query>();
        boolean emptyLiteralFound = false;

        for (String value : values)
        {
            if (value != null)
            {
                notQueries.add(getTermQuery(value));
            }
            else
            {
                // don't bother keeping track of every empty literal we come across - empty query gets added later anyway
                emptyLiteralFound = true;
            }
        }

        if (notQueries.isEmpty())
        {
            // if all we found was empty literals, then return the isNotEmpty query
            return emptyLiteralFound ? new QueryFactoryResult(getEmptyTermQuery(), true) : QueryFactoryResult.createFalseResult();
        }
        else
        {
            BooleanQuery boolQuery = new BooleanQuery();
            // Because this is a NOT equality query we are generating we always need to explicity exclude the
            // EMPTY results from the query we are generating.
            final QueryFactoryResult notEmptyQuery = new QueryFactoryResult(getEmptyTermQuery(), true);

            // We were returned a query that will exclude empties by specifying a MUST_NOT occurrance.
            boolQuery.add(notEmptyQuery.getLuceneQuery(), BooleanClause.Occur.MUST_NOT);

            // Add all the not queries that were specified by the user.
            for (Query query : notQueries)
            {
                boolQuery.add(query, BooleanClause.Occur.MUST_NOT);
            }
            return new QueryFactoryResult(boolQuery, false);
        }
    }

    private TermQuery getPossibleTermQuery(final String value)
    {
        return value == null ? getEmptyTermQuery() : getTermQuery(value);
    }

    private TermQuery getTermQuery(final String value)
    {
        return new TermQuery(new Term(indexFieldName, value));
    }

    private TermQuery getEmptyTermQuery()
    {
        return getTermQuery(BaseFieldIndexer.NO_VALUE_INDEX_VALUE);
    }

    private boolean isPositiveOperator(final Operator operator)
    {
        return operator == Operator.IS || operator == Operator.EQUALS || operator == Operator.IN;
    }

    private boolean isNegationOperator(final Operator operator)
    {
        return operator == Operator.IS_NOT || operator == Operator.NOT_EQUALS || operator == Operator.NOT_IN;
    }
}
