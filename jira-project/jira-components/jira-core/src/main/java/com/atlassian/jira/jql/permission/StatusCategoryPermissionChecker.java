package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;

import java.util.Set;

/**
 * Checks to see if Status Lozenges are enabled or disabled to determine if the status category handler should be seen.
 *
 * @since v6.2
 */
public class StatusCategoryPermissionChecker implements ClausePermissionChecker
{
    private final StatusCategoryManager statusCategoryManager;

    public StatusCategoryPermissionChecker(StatusCategoryManager statusCategoryManager)
    {
        this.statusCategoryManager = statusCategoryManager;
    }

    @Override
    public boolean hasPermissionToUseClause(User user)
    {
        return statusCategoryManager.isStatusAsLozengeEnabled();
    }

    @Override
    public boolean hasPermissionToUseClause(User user, Set<FieldLayout> fieldLayouts)
    {
        return statusCategoryManager.isStatusAsLozengeEnabled();
    }
}
