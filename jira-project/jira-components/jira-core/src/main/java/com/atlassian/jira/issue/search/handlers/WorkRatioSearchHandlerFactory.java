package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.WorkRatioSearcher;
import com.atlassian.jira.jql.context.SimpleClauseContextFactory;
import com.atlassian.jira.jql.permission.ClausePermissionChecker;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.permission.TimeTrackingPermissionChecker;
import com.atlassian.jira.jql.query.WorkRatioClauseQueryFactory;
import com.atlassian.jira.jql.validator.WorkRatioValidator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the work ratio clause.
 *
 * @since v4.0
 */
@InjectableComponent
public final class WorkRatioSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public WorkRatioSearchHandlerFactory(final ComponentFactory factory, final WorkRatioClauseQueryFactory workRatioClauseQueryFactory,
            final WorkRatioValidator workRatioValidator, final FieldClausePermissionChecker.Factory clausePermissionFactory,
            final ApplicationProperties applicationProperties)
    {
        super(factory, SystemSearchConstants.forWorkRatio(), WorkRatioSearcher.class,
                workRatioClauseQueryFactory, workRatioValidator, new WorkRatioClausePermissionCheckerFactory(clausePermissionFactory, applicationProperties),
                new SimpleClauseContextFactory(), null);
    }
    
    static class WorkRatioClausePermissionCheckerFactory implements FieldClausePermissionChecker.Factory
    {
        private final FieldClausePermissionChecker.Factory clausePermissionFactory;
        private final ApplicationProperties applicationProperties;

        WorkRatioClausePermissionCheckerFactory(final FieldClausePermissionChecker.Factory clausePermissionFactory, final ApplicationProperties applicationProperties)
        {
            this.clausePermissionFactory = clausePermissionFactory;
            this.applicationProperties = applicationProperties;
        }

        public ClausePermissionChecker createPermissionChecker(final Field field)
        {
            return new TimeTrackingPermissionChecker(clausePermissionFactory, applicationProperties);
        }

        public ClausePermissionChecker createPermissionChecker(final String fieldId)
        {
            return new TimeTrackingPermissionChecker(clausePermissionFactory, applicationProperties);
        }
    }
}
