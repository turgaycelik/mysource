package com.atlassian.jira.jql.query;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.index.indexers.impl.BaseFieldIndexer;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlTimetrackingDurationSupport;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Factory for producing clauses for the Time tracking system fields.
 *
 * @since v4.0
 */
public class AbstractTimeTrackingClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;
    private final ApplicationProperties applicationProperties;

    public AbstractTimeTrackingClauseQueryFactory(final String indexField, final JqlOperandResolver operandResolver, final JqlTimetrackingDurationSupport jqlTimetrackingDurationSupport, final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        operatorFactories.add(new ActualValueEqualityQueryFactory(jqlTimetrackingDurationSupport, BaseFieldIndexer.NO_VALUE_INDEX_VALUE));
        operatorFactories.add(new ActualValueRelationalQueryFactory(jqlTimetrackingDurationSupport, BaseFieldIndexer.NO_VALUE_INDEX_VALUE));
        this.delegateClauseQueryFactory = createGenericClauseQueryFactory(indexField, operandResolver, operatorFactories);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        if (applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING))
        {
            return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
        }
        else
        {
            return QueryFactoryResult.createFalseResult();
        }
    }

    ///CLOVER:OFF

    GenericClauseQueryFactory createGenericClauseQueryFactory(final String indexField, final JqlOperandResolver operandResolver, final List<OperatorSpecificQueryFactory> operatorFactories)
    {
        return new GenericClauseQueryFactory(indexField, operatorFactories, operandResolver);
    }

    ///CLOVER:ON
}
