package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

/**
 * A query factory that generates lucene queries for the text fields.
 *
 * @since v4.0
 */
public class FreeTextClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;

    ///CLOVER:OFF
    public FreeTextClauseQueryFactory(JqlOperandResolver operandResolver, String documentConstant)
    {
        delegateClauseQueryFactory = getDelegate(operandResolver, documentConstant);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }

    ClauseQueryFactory getDelegate(final JqlOperandResolver operandResolver, final String documentConstant)
    {
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        operatorFactories.add(new LikeQueryFactory());
        return new GenericClauseQueryFactory(documentConstant, operatorFactories, operandResolver);
    }
    ///CLOVER:ON
}
