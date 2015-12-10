package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.CreatorSearcher;
import com.atlassian.jira.jql.context.SimpleClauseContextFactory;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.CreatorClauseQueryFactory;
import com.atlassian.jira.jql.validator.CreatorValidator;
import com.atlassian.jira.jql.values.UserClauseValuesGenerator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the {@link com.atlassian.jira.issue.fields.CreatorSystemField}.
 *
 * @since v6.2
 */
@InjectableComponent
public final class CreatorSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public CreatorSearchHandlerFactory(final ComponentFactory componentFactory, CreatorClauseQueryFactory clauseQueryFactory,
            CreatorValidator clauseValidator, final FieldClausePermissionChecker.Factory clausePermissionFactory,
            final UserPickerSearchService userPickerSearchService)
    {
        super(componentFactory, SystemSearchConstants.forCreator(), CreatorSearcher.class,
                clauseQueryFactory, clauseValidator, clausePermissionFactory,
                new SimpleClauseContextFactory(),
                new UserClauseValuesGenerator(userPickerSearchService));
    }
}