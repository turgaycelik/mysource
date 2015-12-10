package com.atlassian.jira.jql.permission;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestNoOpClausePermissionChecker
{
    @Test
    public void testAlwaysReturnsTrue() throws Exception
    {
        assertTrue(NoOpClausePermissionChecker.NOOP_CLAUSE_PERMISSION_CHECKER.hasPermissionToUseClause(null));
    }
}
