package com.atlassian.jira.appconsistency.db;

import java.util.Collection;
import java.util.List;

import com.atlassian.jira.local.testutils.UtilsForTestSetup;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestBuild178SchemaCheck
{

    @Test
    public void testSimple()
    {
        UtilsForTestSetup.loadDatabaseDriver();

        final MultiMap tableColumns = new MultiHashMap();

        Build178SchemaCheck check = new MockBuild178SchemaCheck(tableColumns);
        assertTrue(check.isOk());

        tableColumns.put("car", "wheel");
        tableColumns.put("car", "roof");
        tableColumns.put("car", "horn");
        tableColumns.put("bike", "handlebars");
        assertTrue(check.isOk());

        tableColumns.put("PortletConfiguration", "position");
        tableColumns.put("fieldlayout", "type");
        assertFalse(check.isOk());
    }

    /**
     * This mock class is to override the actual check only
     */
    private class MockBuild178SchemaCheck extends Build178SchemaCheck
    {
        private final MultiMap tableColumns;

        public MockBuild178SchemaCheck(MultiMap tableColumns)
        {
            this.tableColumns = tableColumns;
        }

        void doColumnTableChecks(List<TableColumnCheckResult> tableColumnCheckResults)
        {
            for (TableColumnCheckResult tableColumnCheckResult : tableColumnCheckResults)
            {
                Collection columns = (Collection) tableColumns.get(tableColumnCheckResult.getTableName());
                if (columns != null && columns.contains(tableColumnCheckResult.getColumnName()))
                {
                    tableColumnCheckResult.setExists(true);
                }
            }
        }
    }

}
