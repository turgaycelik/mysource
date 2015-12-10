package com.atlassian.jira.jql.query;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.LinkedList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A {@link com.atlassian.jira.jql.query.ClauseQueryFactory} for the "Issue Parent" JQL clause.
 *
 * @since v4.0
 */
@InjectableComponent
public class IssueParentClauseQueryFactory implements ClauseQueryFactory
{
    private static final Logger log = Logger.getLogger(IssueParentClauseQueryFactory.class);

    private final JqlOperandResolver operandResolver;
    private final JqlIssueSupport issueSupport;
    private final SubTaskManager subTaskManager;

    public IssueParentClauseQueryFactory(final JqlOperandResolver operandResolver, final JqlIssueSupport issueSupport, final SubTaskManager subTaskManager)
    {
        this.issueSupport = notNull("issueSupport", issueSupport);
        this.operandResolver = notNull("operandResolver", operandResolver);
        this.subTaskManager = notNull("subTaskManager", subTaskManager);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        notNull("queryCreationContext", queryCreationContext);
        if (!subTaskManager.isSubTasksEnabled())
        {
            return QueryFactoryResult.createFalseResult();
        }

        Operand operand = terminalClause.getOperand();
        Operator operator = terminalClause.getOperator();

        final List<QueryLiteral> literals = operandResolver.getValues(queryCreationContext, operand, terminalClause);

        if (literals == null)
        {
            log.debug(String.format("Unable to find operand values from operand '%s' for clause '%s'.", operand.getDisplayString(), terminalClause.getName()));
            return QueryFactoryResult.createFalseResult();
        }
        else if (operator == Operator.EQUALS || operator == Operator.IN)
        {
            return handleEquals(queryCreationContext.getQueryUser(), queryCreationContext.isSecurityOverriden(), literals);
        }
        else if (operator == Operator.NOT_EQUALS || operator == Operator.NOT_IN)
        {
            return handleNotEquals(queryCreationContext.getQueryUser(), queryCreationContext.isSecurityOverriden(), literals);
        }
        else
        {
            log.debug(String.format("The '%s' clause does not support the %s operator.", terminalClause.getName(), operator));
            return QueryFactoryResult.createFalseResult();
        }
    }

    /**
     * @param searcher the user performing the search
     * @param overrideSecurity false if permission checks should be performed
     * @param rawValues the query literals representing the issues
     * @return a list of ids of issues as strings; never null but may contain nulls representing empty literal values.
     */
    List<String> getIndexValues(final com.atlassian.crowd.embedded.api.User searcher, final boolean overrideSecurity, final List<QueryLiteral> rawValues)
    {
        final List<String> indexValues = new LinkedList<String>();
        for (QueryLiteral rawValue : rawValues)
        {
            if (rawValue.getStringValue() != null)
            {
                final Issue issue = overrideSecurity ? issueSupport.getIssue(rawValue.getStringValue()) : issueSupport.getIssue(rawValue.getStringValue(), ApplicationUsers.from(searcher));
                if (issue != null) {
                    indexValues.add(issue.getId().toString());
                }
            }
            else if (rawValue.getLongValue() != null)
            {
                indexValues.add(rawValue.asString());
            }
            else
            {
                // empty literal needs to be communicated back to the caller
                indexValues.add(null);
            }
        }
        return indexValues;
    }

    private QueryFactoryResult handleNotEquals(final com.atlassian.crowd.embedded.api.User searcher, final boolean overrideSecurity, final List<QueryLiteral> rawValues)
    {
        return new QueryFactoryResult(createPositiveEqualsQuery(searcher, overrideSecurity, rawValues), true);
    }

    private QueryFactoryResult handleEquals(final com.atlassian.crowd.embedded.api.User searcher, final boolean overrideSecurity, final List<QueryLiteral> rawValues)
    {
        return new QueryFactoryResult(createPositiveEqualsQuery(searcher, overrideSecurity, rawValues));
    }

    private Query createPositiveEqualsQuery(final com.atlassian.crowd.embedded.api.User searcher, final boolean overrideSecurity, final List<QueryLiteral> rawValues)
    {
        final List<String> indexValues = getIndexValues(searcher, overrideSecurity, rawValues);

        if (indexValues.size() == 1)
        {
            final String value = indexValues.get(0);
            // null value means empty literal; generate a false result query
            return value == null ? new BooleanQuery() : createQuery(value);
        }
        else
        {
            BooleanQuery query = new BooleanQuery();
            for (String indexValue : indexValues)
            {
                if (indexValue != null)
                {
                    query.add(createQuery(indexValue), BooleanClause.Occur.SHOULD);
                }
            }
            return query;
        }
    }

    private Query createQuery(final String indexValue)
    {
        return new TermQuery(new Term(SystemSearchConstants.forIssueParent().getIndexField(), indexValue));
    }
}
