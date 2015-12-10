package com.atlassian.jira.jql.permission;

import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;

/**
 * @since v6.2
 */
public class IssuePropertyClausePermissionChecker implements ClausePermissionChecker
{
    @Override
    public boolean hasPermissionToUseClause(final User user)
    {
        return true;
    }

    @Override
    public boolean hasPermissionToUseClause(final User user, final Set<FieldLayout> fieldLayouts)
    {
        return hasPermissionToUseClause(user);
    }
}
