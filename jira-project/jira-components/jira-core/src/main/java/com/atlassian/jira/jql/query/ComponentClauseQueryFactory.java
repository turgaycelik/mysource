package com.atlassian.jira.jql.query;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.ComponentIndexInfoResolver;
import com.atlassian.jira.jql.resolver.ComponentResolver;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates queries for component clauses.
 *
 * @since v4.0
 */
public class ComponentClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;

    public ComponentClauseQueryFactory(ComponentResolver componentResolver, JqlOperandResolver operandResolver)
    {
        final SimpleFieldSearchConstantsWithEmpty searchConstants = SystemSearchConstants.forComponent();
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        final ComponentIndexInfoResolver indexInfoResolver = new ComponentIndexInfoResolver(componentResolver);
        operatorFactories.add(new EqualityWithSpecifiedEmptyValueQueryFactory<ProjectComponent>(indexInfoResolver, searchConstants.getEmptyIndexValue()));
        delegateClauseQueryFactory = new GenericClauseQueryFactory(searchConstants.getIndexField(), operatorFactories, operandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }
}