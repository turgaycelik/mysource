package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.jql.ValueGeneratingClauseHandler;
import com.atlassian.jira.jql.context.ClauseContextFactory;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.jql.values.ClauseValuesGenerator;
import com.atlassian.query.operator.Operator;

import java.util.Set;

/**
 * @since v4.0
 */
public class SimpleCustomFieldContextValueGeneratingClauseHandler extends SimpleCustomFieldClauseContextHandler implements ValueGeneratingClauseHandler
{
    private final ClauseValuesGenerator clauseValuesGenerator;

    public SimpleCustomFieldContextValueGeneratingClauseHandler(final ClauseValidator validator, final ClauseQueryFactory clauseQueryFactory,
            final ClauseContextFactory clauseContextFactory, final ClauseValuesGenerator clauseValuesGenerator,
            final Set<Operator> supportedOperators, final JiraDataType supportedType)
    {
        super(validator, clauseQueryFactory, clauseContextFactory, supportedOperators, supportedType);
        this.clauseValuesGenerator = clauseValuesGenerator;
    }

    public ClauseValuesGenerator getClauseValuesGenerator()
    {
        return clauseValuesGenerator;
    }
}
