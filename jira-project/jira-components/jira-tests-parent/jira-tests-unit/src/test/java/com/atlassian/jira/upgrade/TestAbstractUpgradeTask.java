package com.atlassian.jira.upgrade;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @since v4.4
 */
public class TestAbstractUpgradeTask
{
    @Test
    public void testTableNotPrefixedWithBlankSchema() throws Exception
    {
        String result = AbstractUpgradeTask.ensureTablePrefixed("jiraissues", null);
        assertEquals("Table should not be prefixed for null schema", "jiraissues", result);

        result = AbstractUpgradeTask.ensureTablePrefixed("jiraissues", "");
        assertEquals("Table should not be prefixed for blank schema", "jiraissues", result);
    }

    @Test
    public void testTablePrefixedWithUnmatchedSchema() throws Exception
    {
        // A table with no similarity to the schema name should be prefixed
        String result = AbstractUpgradeTask.ensureTablePrefixed("userassociation", "jira");
        assertEquals("Table should be prefixed with schema", "jira.userassociation", result);

        // A table starting with the schema - but not the '.' - should be prefixed
        result = AbstractUpgradeTask.ensureTablePrefixed("jiraissues", "jira");
        assertEquals("Table should be prefixed with schema", "jira.jiraissues", result);
    }

    @Test
    public void testTableNotPrefixedWithMatchedSchema() throws Exception
    {
        // A table already prefixed should not be modified.
        String result = AbstractUpgradeTask.ensureTablePrefixed("jira.jiraissues", "jira");
        assertEquals("Prefixed table should be returned unchanged", "jira.jiraissues", result);
    }
}
