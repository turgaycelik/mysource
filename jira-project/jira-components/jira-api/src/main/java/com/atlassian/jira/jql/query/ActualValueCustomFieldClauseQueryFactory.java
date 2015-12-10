package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.IndexValueConverter;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for producing clauses for the custom fields that have a raw index value
 *
 * @since v4.0
 */
public class ActualValueCustomFieldClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;
    //CLOVER:OFF
    public ActualValueCustomFieldClauseQueryFactory(String luceneField, JqlOperandResolver operandResolver, final IndexValueConverter indexValueConverter, boolean supportsRelational)
    {
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        operatorFactories.add(new ActualValueEqualityQueryFactory(indexValueConverter));
        if (supportsRelational)
        {
            operatorFactories.add(new ActualValueRelationalQueryFactory(indexValueConverter));
        }
        this.delegateClauseQueryFactory = new GenericClauseQueryFactory(luceneField, operatorFactories, operandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }
    //CLOVER:ON
}
