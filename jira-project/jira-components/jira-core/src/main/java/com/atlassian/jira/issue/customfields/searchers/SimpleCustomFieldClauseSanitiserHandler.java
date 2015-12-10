package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.jql.permission.ClauseSanitiser;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.query.operator.Operator;

import java.util.Set;

/**
 * Used for custom fields which additionally require their clauses to be sanitised. 
 *
 * @since v4.0
 */
@NonInjectableComponent
public class SimpleCustomFieldClauseSanitiserHandler extends SimpleCustomFieldSearcherClauseHandler implements CustomFieldClauseSanitiserHandler
{
    private final ClauseSanitiser sanitiser;

    public SimpleCustomFieldClauseSanitiserHandler(final ClauseValidator validator, final ClauseQueryFactory clauseQueryFactory,
            final ClauseSanitiser sanitiser, final Set<Operator> supportedOperators, final JiraDataType supportedType)
    {
        super(validator, clauseQueryFactory, supportedOperators, supportedType);
        this.sanitiser = sanitiser;
    }

    public ClauseSanitiser getClauseSanitiser()
    {
        return sanitiser;
    }
}
