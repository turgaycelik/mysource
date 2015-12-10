package com.atlassian.jira.jql.permission;

import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.mock.jql.validator.MockClausePermissionChecker;

/**
 * @since v4.0
 */
public class MockFieldClausePermissionFactory implements FieldClausePermissionChecker.Factory
{
    private final ClausePermissionChecker clausePermissionChecker;

    public MockFieldClausePermissionFactory()
    {
        this.clausePermissionChecker = new MockClausePermissionChecker();
    }

    public MockFieldClausePermissionFactory(ClausePermissionChecker clausePermissionChecker)
    {
        this.clausePermissionChecker = clausePermissionChecker;
    }

    public ClausePermissionChecker createPermissionChecker(final Field field)
    {
        return clausePermissionChecker;
    }

    public ClausePermissionChecker createPermissionChecker(final String fieldId)
    {
        return clausePermissionChecker;
    }
}
