package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.LabelsSearcher;
import com.atlassian.jira.jql.context.SimpleClauseContextFactory;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.LabelsClauseQueryFactory;
import com.atlassian.jira.jql.validator.LabelsValidator;
import com.atlassian.jira.jql.values.LabelsClauseValuesGenerator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the labels field.
 *
 * @since v4.2
 */
@InjectableComponent
public final class LabelsSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public LabelsSearchHandlerFactory(final ComponentFactory componentFactory, final LabelsValidator queryValidator,
            final FieldClausePermissionChecker.Factory clausePermissionFactory, final JqlOperandResolver jqlOperandResolver,
            final LabelsClauseValuesGenerator labelsClauseValuesGenerator)
    {
        super(componentFactory, SystemSearchConstants.forLabels(),
                LabelsSearcher.class, new LabelsClauseQueryFactory(jqlOperandResolver, DocumentConstants.ISSUE_LABELS_FOLDED),
                queryValidator, clausePermissionFactory, new SimpleClauseContextFactory(),
                labelsClauseValuesGenerator);
    }
}