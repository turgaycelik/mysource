package com.atlassian.jira.upgrade.tasks;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;

public abstract class DropIndexTask extends AbstractUpgradeTask
{
    public DropIndexTask(boolean reindexRequired)
    {
        super(reindexRequired);
    }

    protected String buildDropIndexSql(final String tableName, final String index)
            throws SQLException
    {
        return getDatabaseType().getDropIndexSQL(getSchemaName(), tableName, index);
    }

    protected void dropIndex(final String tableName, final String indexName) throws Exception
    {
        final Connection connection = getDatabaseConnection();
        try
        {
            connection.setAutoCommit(false);
            Statement update = connection.createStatement();
            update.execute(buildDropIndexSql(tableName, indexName));
            connection.commit();
        }
        finally
        {
            connection.close();
        }
    }
}