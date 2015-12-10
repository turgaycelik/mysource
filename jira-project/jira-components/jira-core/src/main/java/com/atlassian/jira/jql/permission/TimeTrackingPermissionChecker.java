package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;

import java.util.Set;

/**
 * Used for the time tracking fields to check if time tracking is enabled and if the TimeTracking field is visible.
 *
 * @since v4.0
 */
public class TimeTrackingPermissionChecker implements ClausePermissionChecker
{
    private final ApplicationProperties applicationProperties;
    private final ClausePermissionChecker permissionChecker;

    public TimeTrackingPermissionChecker(final FieldClausePermissionChecker.Factory fieldClausePermissionHandlerFactory, final ApplicationProperties applicationProperties)
    {
        this.permissionChecker = fieldClausePermissionHandlerFactory.createPermissionChecker(IssueFieldConstants.TIMETRACKING);
        this.applicationProperties = applicationProperties;
    }

    public boolean hasPermissionToUseClause(final User user)
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING) && permissionChecker.hasPermissionToUseClause(user);
    }

    @Override
    public boolean hasPermissionToUseClause(User searcher, Set<FieldLayout> fieldLayouts)
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING) && permissionChecker.hasPermissionToUseClause(searcher, fieldLayouts);
    }
}
