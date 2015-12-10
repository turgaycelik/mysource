package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.AffectedVersionsSearcher;
import com.atlassian.jira.jql.context.VersionClauseContextFactory;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.AffectedVersionClauseQueryFactory;
import com.atlassian.jira.jql.validator.AffectedVersionValidator;
import com.atlassian.jira.jql.values.VersionClauseValuesGenerator;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the Affects version clause.
 *
 * @since v4.0
 */
@InjectableComponent
public final class AffectedVersionSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public AffectedVersionSearchHandlerFactory(final ComponentFactory componentFactory, final AffectedVersionClauseQueryFactory factory,
            final AffectedVersionValidator validator, final FieldClausePermissionChecker.Factory clausePermissionFactory,
            final VersionClauseContextFactory versionClauseContextFactory, final PermissionManager permissionManager,
            final VersionManager versionManager, I18nHelper.BeanFactory beanFactory)
    {
        super(componentFactory, SystemSearchConstants.forAffectedVersion(),
                AffectedVersionsSearcher.class, factory, validator, clausePermissionFactory,
                versionClauseContextFactory, new VersionClauseValuesGenerator(versionManager, permissionManager, beanFactory));
    }
}
