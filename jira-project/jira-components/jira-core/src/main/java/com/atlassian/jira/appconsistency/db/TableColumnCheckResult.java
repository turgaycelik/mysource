package com.atlassian.jira.appconsistency.db;

/**
 * Represents the stateful answer to the query of whether a given table contains a given column.
 */
final public class TableColumnCheckResult
{
    private String tableName;
    private String columnName;
    private boolean exists;

    public TableColumnCheckResult(String tableName)
    {
        this.tableName = tableName;
        this.columnName = null;
    }

    public TableColumnCheckResult(String tableName, String columnName)
    {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public String getTableName()
    {
        return tableName;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public String getColumnName()
    {
        return columnName;
    }

    public void setColumnName(String columnName)
    {
        this.columnName = columnName;
    }

    public boolean isExists()
    {
        return exists;
    }

    public void setExists(boolean exists)
    {
        this.exists = exists;
    }
}
