package com.atlassian.jira.issue.customfields.searchers.transformer;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.searchers.transformer.SimpleNavigatorCollectorVisitor;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A helper class for Number Range Custom field searcher converting between the Query and Searcher
 * views of the clauses.
 */
public class NumberRangeCustomFieldInputHelper
{
    private final ClauseNames clauseNames;
    private final JqlOperandResolver jqlOperandResolver;

    public NumberRangeCustomFieldInputHelper(ClauseNames clauseNames, JqlOperandResolver jqlOperandResolver)
    {
        this.clauseNames = notNull("clauseNames", clauseNames);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
    }

    /**
     * Returns a list of the valid for navigator {@link TerminalClause}s in the {@link com.atlassian.query.Query}. If the structure, operators or values
     * are not valid null is returned.
     *
     * @param query the search query to search for valid terminal clauses
     * @return a list of valid terminal clauses. Null if the search query is not valid for the Number Range searcher
     */
    public List<TerminalClause> getValuesFromQuery(Query query)
    {
        notNull("query", query);
        notNull("whereClause", query.getWhereClause());

        SimpleNavigatorCollectorVisitor simpleNavigatorCollectorVisitor = createSimpleNavigatorCollectorVisitor();
        query.getWhereClause().accept(simpleNavigatorCollectorVisitor);
        if (simpleNavigatorCollectorVisitor.isValid())
        {
            final List<TerminalClause> clauses = simpleNavigatorCollectorVisitor.getClauses();
            if (clauses.isEmpty())
            {
                return Collections.emptyList();
            }
            else if (clauses.size() == 1)
            {
                TerminalClause clause = clauses.get(0);
                if (isValidClause(clause))
                {
                    return Collections.singletonList(clause);
                }
            }
            else if (clauses.size() == 2)
            {
                TerminalClause clause1 = clauses.get(0);
                TerminalClause clause2 = clauses.get(1);
                if (isValidClause(clause1) && isValidClause(clause2) && clause1.getOperator() != clause2.getOperator())
                {
                    return CollectionBuilder.newBuilder(clause1, clause2).asList();
                }
            }
        }
        return null;
    }

    protected SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectorVisitor()
    {
        return new SimpleNavigatorCollectorVisitor(clauseNames);
    }

    private boolean isValidClause(TerminalClause clause)
    {
        if (!jqlOperandResolver.isEmptyOperand(clause.getOperand()) && !jqlOperandResolver.isListOperand(clause.getOperand()))
        {
            if (clause.getOperator() == Operator.GREATER_THAN_EQUALS || clause.getOperator() == Operator.LESS_THAN_EQUALS)
            {
                return true;
            }
        }
        return false;
    }

}
