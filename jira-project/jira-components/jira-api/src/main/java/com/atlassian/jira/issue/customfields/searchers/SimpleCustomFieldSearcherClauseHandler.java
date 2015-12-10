package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.query.operator.Operator;

import java.util.Set;

/**
 * A simple implementation of the {@link CustomFieldSearcherClauseHandler}.
 *
 * @since v4.0
 */
@NonInjectableComponent
public class SimpleCustomFieldSearcherClauseHandler implements CustomFieldSearcherClauseHandler
{
    private final ClauseValidator validator;
    private final ClauseQueryFactory clauseQueryFactory;
    private final Set<Operator> supportedOperators;
    private final JiraDataType supportedType;

    public SimpleCustomFieldSearcherClauseHandler(final ClauseValidator validator, final ClauseQueryFactory clauseQueryFactory,
            final Set<Operator> supportedOperators, final JiraDataType supportedType)
    {
        this.validator = validator;
        this.clauseQueryFactory = clauseQueryFactory;
        this.supportedOperators = supportedOperators;
        this.supportedType = supportedType;
    }

    public ClauseValidator getClauseValidator()
    {
        return validator;
    }

    public ClauseQueryFactory getClauseQueryFactory()
    {
        return clauseQueryFactory;
    }

    public Set<Operator> getSupportedOperators()
    {
        return supportedOperators;
    }

    public JiraDataType getDataType()
    {
        return supportedType;
    }
}
