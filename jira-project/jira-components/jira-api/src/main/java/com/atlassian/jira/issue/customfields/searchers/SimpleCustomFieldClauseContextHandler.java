package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.jql.context.ClauseContextFactory;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.query.operator.Operator;

import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A simple class for createing a context aware clause handler for a custom field.
 *
 * @since v4.0
 */
@NonInjectableComponent
public class SimpleCustomFieldClauseContextHandler extends SimpleCustomFieldSearcherClauseHandler implements CustomFieldClauseContextHandler
{
    private final ClauseContextFactory clauseContextFactory;

    public SimpleCustomFieldClauseContextHandler(final ClauseValidator validator, final ClauseQueryFactory clauseQueryFactory,
            final ClauseContextFactory clauseContextFactory, final Set<Operator> supportedOperators,
            final JiraDataType supportedType)
    {
        super(validator, clauseQueryFactory, supportedOperators, supportedType);
        this.clauseContextFactory = notNull("clauseContextFactory", clauseContextFactory);
    }

    public ClauseContextFactory getClauseContextFactory()
    {
        return clauseContextFactory;
    }
}
