package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.DueDateSearcher;
import com.atlassian.jira.jql.context.SimpleClauseContextFactory;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.DueDateClauseQueryFactory;
import com.atlassian.jira.jql.validator.DueDateValidator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the Due Date field.
 *
 * @since v4.0
 */
@InjectableComponent
public final class DueDateSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public DueDateSearchHandlerFactory(final ComponentFactory componentFactory, DueDateClauseQueryFactory factory,
            DueDateValidator validator, final FieldClausePermissionChecker.Factory clausePermissionFactory)
    {
        super(componentFactory, SystemSearchConstants.forDueDate(),
                DueDateSearcher.class, factory, validator, clausePermissionFactory,
                new SimpleClauseContextFactory(), null);
    }
}
