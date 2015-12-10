package com.atlassian.jira.jql.query;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.WorkRatioIndexInfoResolver;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Creates queries for {@link com.atlassian.jira.issue.fields.WorkRatioSystemField} clauses.
 *
 * @since v4.0
 */
@InjectableComponent
public class WorkRatioClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;
    private final ApplicationProperties applicationProperties;

    public WorkRatioClauseQueryFactory(final JqlOperandResolver operandResolver, final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        final WorkRatioIndexInfoResolver workRatioIndexInfoResolver = new WorkRatioIndexInfoResolver();
        operatorFactories.add(new EqualityQueryFactory<Object>(workRatioIndexInfoResolver));
        operatorFactories.add(new RelationalOperatorMutatedIndexValueQueryFactory(workRatioIndexInfoResolver));
        this.delegateClauseQueryFactory = createGenericClauseQueryFactory(operandResolver, operatorFactories);
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

    GenericClauseQueryFactory createGenericClauseQueryFactory(final JqlOperandResolver operandResolver, final List<OperatorSpecificQueryFactory> operatorFactories)
    {
        return new GenericClauseQueryFactory(SystemSearchConstants.forWorkRatio(), operatorFactories, operandResolver);
    }

    ///CLOVER:ON
}
