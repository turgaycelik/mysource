package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.CustomFieldOptionResolver;
import com.atlassian.jira.jql.resolver.SelectCustomFieldIndexInfoResolver;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for producing clauses for the cascading select custom fields
 *
 * @since v4.0
 */
@NonInjectableComponent
public class SelectCustomFieldClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;

    public SelectCustomFieldClauseQueryFactory(final CustomField customField, final JqlSelectOptionsUtil jqlSelectOptionsUtil,
            final JqlOperandResolver operandResolver, CustomFieldOptionResolver customFieldOptionResolver)
    {
        final SelectCustomFieldIndexInfoResolver selectIndexInfoResolver = new SelectCustomFieldIndexInfoResolver(customFieldOptionResolver);
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        operatorFactories.add(new EqualityQueryFactory<CustomField>(selectIndexInfoResolver));
        delegateClauseQueryFactory = new GenericClauseQueryFactory(customField.getId(), operatorFactories, operandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }
}
