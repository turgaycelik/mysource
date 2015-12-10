package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.jql.ValueGeneratingClauseHandler;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.jql.values.ClauseValuesGenerator;
import com.atlassian.query.operator.Operator;

import java.util.Set;

/**
 * @since v4.0
 */
public class SimpleCustomFieldValueGeneratingClauseHandler extends SimpleCustomFieldSearcherClauseHandler implements ValueGeneratingClauseHandler
{
    private final ClauseValuesGenerator clauseValuesGenerator;

    public SimpleCustomFieldValueGeneratingClauseHandler(final ClauseValidator validator, final ClauseQueryFactory clauseQueryFactory,
            final ClauseValuesGenerator clauseValuesGenerator, final Set<Operator> supportedOperators, final JiraDataType supportedType)
    {
        super(validator, clauseQueryFactory, supportedOperators, supportedType);
        this.clauseValuesGenerator = clauseValuesGenerator;
    }

    public ClauseValuesGenerator getClauseValuesGenerator()
    {
        return clauseValuesGenerator;
    }
}
