package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.ReporterSearcher;
import com.atlassian.jira.jql.context.SimpleClauseContextFactory;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.ReporterClauseQueryFactory;
import com.atlassian.jira.jql.validator.ReporterValidator;
import com.atlassian.jira.jql.values.UserClauseValuesGenerator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the {@link com.atlassian.jira.issue.fields.ReporterSystemField}.
 *
 * @since v4.0
 */
@InjectableComponent
public final class ReporterSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public ReporterSearchHandlerFactory(final ComponentFactory componentFactory, ReporterClauseQueryFactory clauseQueryFactory,
            ReporterValidator clauseValidator, final FieldClausePermissionChecker.Factory clausePermissionFactory,
            final UserPickerSearchService userPickerSearchService)
    {
        super(componentFactory, SystemSearchConstants.forReporter(), ReporterSearcher.class,
                clauseQueryFactory, clauseValidator, clausePermissionFactory,
                new SimpleClauseContextFactory(),
                new UserClauseValuesGenerator(userPickerSearchService));
    }
}