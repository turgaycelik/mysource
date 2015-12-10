package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlLocalDateSupport;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates clauses for queries on {@link com.atlassian.jira.datetime.LocalDate} fields.
 *
 * @since v4.4
 */
public class LocalDateClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;

    ///CLOVER:OFF
    public LocalDateClauseQueryFactory(SimpleFieldSearchConstants constants, JqlLocalDateSupport jqlLocalDateSupport, JqlOperandResolver operandResolver)
    {
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        operatorFactories.add(new LocalDateEqualityQueryFactory(jqlLocalDateSupport));
        operatorFactories.add(new LocalDateRelationalQueryFactory(jqlLocalDateSupport));
        this.delegateClauseQueryFactory = new GenericClauseQueryFactory(constants, operatorFactories, operandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }
    ///CLOVER:ON
}
