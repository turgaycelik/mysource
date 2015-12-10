package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.IssueTypeSearcher;
import com.atlassian.jira.jql.context.IssueTypeClauseContextFactory;
import com.atlassian.jira.jql.context.MultiClauseDecoratorContextFactory;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.IssueTypeClauseQueryFactory;
import com.atlassian.jira.jql.resolver.IssueTypeResolver;
import com.atlassian.jira.jql.validator.IssueTypeValidator;
import com.atlassian.jira.jql.values.IssueTypeClauseValuesGenerator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the {@link com.atlassian.jira.issue.fields.IssueTypeSystemField}.
 *
 * @since v4.0
 */
@InjectableComponent
public final class IssueTypeSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public IssueTypeSearchHandlerFactory(final ComponentFactory componentFactory, final IssueTypeClauseQueryFactory clauseQueryFactory,
            final IssueTypeValidator clauseValidator, final FieldClausePermissionChecker.Factory clausePermissionFactory,
            final IssueTypeResolver issueTypeResolver, final JqlOperandResolver jqlOperandResolver,
            final ConstantsManager constantsManager, final MultiClauseDecoratorContextFactory.Factory multiFactory)
    {
        super(componentFactory, SystemSearchConstants.forIssueType(), IssueTypeSearcher.class,
                clauseQueryFactory, clauseValidator, clausePermissionFactory,
                multiFactory.create(new IssueTypeClauseContextFactory(issueTypeResolver, jqlOperandResolver, constantsManager)),
                new IssueTypeClauseValuesGenerator(constantsManager));
    }
}
