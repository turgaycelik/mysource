package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.FixForVersionsSearcher;
import com.atlassian.jira.jql.context.VersionClauseContextFactory;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.FixForVersionClauseQueryFactory;
import com.atlassian.jira.jql.validator.FixForVersionValidator;
import com.atlassian.jira.jql.values.VersionClauseValuesGenerator;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the "Fix For" field.
 *
 * @since v4.0
 */
@InjectableComponent
public final class FixForVersionSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public FixForVersionSearchHandlerFactory(final ComponentFactory componentFactory, FixForVersionClauseQueryFactory factory,
            FixForVersionValidator validator, final FieldClausePermissionChecker.Factory clausePermissionFactory,
            final VersionClauseContextFactory versionClauseContextFactory,
            final PermissionManager permissionManager, final VersionManager versionManager,
            I18nHelper.BeanFactory beanFactory)
    {
        super(componentFactory, SystemSearchConstants.forFixForVersion(),
                FixForVersionsSearcher.class, factory, validator, clausePermissionFactory,
                versionClauseContextFactory, new VersionClauseValuesGenerator(versionManager, permissionManager, beanFactory));
    }
}
