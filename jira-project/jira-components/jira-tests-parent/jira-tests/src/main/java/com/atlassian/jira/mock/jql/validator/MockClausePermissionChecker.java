package com.atlassian.jira.mock.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.jql.permission.ClausePermissionChecker;

import java.util.Set;

/**
 * @since v4.0
 */
public class MockClausePermissionChecker implements ClausePermissionChecker
{
    private final boolean hasPerm;

    public MockClausePermissionChecker()
    {
        this(true);
    }

    public MockClausePermissionChecker(boolean hasPerm)
    {
        this.hasPerm = hasPerm;
    }

    public boolean hasPermissionToUseClause(final User user)
    {
        return hasPerm;
    }

    @Override
    public boolean hasPermissionToUseClause(User searcher, Set<FieldLayout> fieldLayouts)
    {
        return hasPerm;
    }
}
