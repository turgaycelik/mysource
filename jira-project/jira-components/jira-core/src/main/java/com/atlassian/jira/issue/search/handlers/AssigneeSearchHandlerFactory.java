package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.AssigneeSearcher;
import com.atlassian.jira.jql.context.SimpleClauseContextFactory;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.AssigneeClauseQueryFactory;
import com.atlassian.jira.jql.validator.AssigneeValidator;
import com.atlassian.jira.jql.values.UserClauseValuesGenerator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the {@link com.atlassian.jira.issue.fields.AssigneeSystemField}.
 *
 * @since v4.0
 */
@InjectableComponent
public final class AssigneeSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public AssigneeSearchHandlerFactory(final ComponentFactory componentFactory, AssigneeClauseQueryFactory clauseQueryFactory,
            AssigneeValidator clauseValidator, final FieldClausePermissionChecker.Factory clausePermissionFactory,
            final UserPickerSearchService userPickerSearchService)
    {
        super(componentFactory, SystemSearchConstants.forAssignee(), AssigneeSearcher.class,
                clauseQueryFactory, clauseValidator, clausePermissionFactory,
                new SimpleClauseContextFactory(),
                new UserClauseValuesGenerator(userPickerSearchService));
    }
}