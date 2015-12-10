package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.index.indexers.impl.LabelsIndexer;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.LabelIndexInfoResolver;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

/**
 * A clause query factory that handles the Labels system field.
 *
 * @since v4.2
 */
public class LabelsClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;
    private final String fieldId;

    public LabelsClauseQueryFactory(JqlOperandResolver operandResolver, final String fieldId)
    {
        this.fieldId = fieldId;
        delegateClauseQueryFactory = getDelegate(operandResolver, fieldId);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }

    ClauseQueryFactory getDelegate(final JqlOperandResolver operandResolver, final String fieldId)
    {
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        operatorFactories.add(new EqualityWithSpecifiedEmptyValueQueryFactory<Label>(new LabelIndexInfoResolver(true), LabelsIndexer.NO_VALUE_INDEX_VALUE));
        return new GenericClauseQueryFactory(fieldId, operatorFactories, operandResolver);
    }

    //equals mainly here for testability
    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final LabelsClauseQueryFactory that = (LabelsClauseQueryFactory) o;

        if (fieldId != null ? !fieldId.equals(that.fieldId) : that.fieldId != null)
        {
            return false;
        }

        return true;
    }

}