package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;

import java.util.Set;

/**
 * A No-Op clause permission checker that always allows you to use a clause.
 *
 * @since v4.0
 */
public final class NoOpClausePermissionChecker implements ClausePermissionChecker
{
    public static final NoOpClausePermissionChecker NOOP_CLAUSE_PERMISSION_CHECKER = new NoOpClausePermissionChecker();

    // shouldn't need construction
    private NoOpClausePermissionChecker()
    {
    }

    public boolean hasPermissionToUseClause(final User user)
    {
        return true;
    }

    @Override
    public boolean hasPermissionToUseClause(User searcher, Set<FieldLayout> fieldLayouts)
    {
        return true;
    }
}
