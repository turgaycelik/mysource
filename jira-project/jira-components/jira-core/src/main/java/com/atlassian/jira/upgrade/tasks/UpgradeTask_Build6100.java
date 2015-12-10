package com.atlassian.jira.upgrade.tasks;

import java.sql.Connection;
import java.sql.Statement;

/**
 * a too long index name was inadvertently merged to branch from master to support downgrades. This upgrade task
 * removes that index
 *
 * @since v6.0.4
 */
public class UpgradeTask_Build6100 extends DropIndexTask
{

    public UpgradeTask_Build6100()
    {
        super(false);
    }
    @Override
    public String getBuildNumber()
    {
        return "6100";
    }
    @Override
    public String getShortDescription()
    {
        return "Shorten name of index on ReplicatedIndexOperation entity";
    }
    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        final Connection connection = getDatabaseConnection();
        final String tableName = "replicatedindexoperation";
        final String index = "node_id_index_operation_time_idx";
        try
        {
            connection.setAutoCommit(false);
            final String sql = buildDropIndexSql(tableName, index);
            Statement update = connection.createStatement();
            update.execute(sql);
            connection.commit();
        }
        catch (Exception ignore) {
            // the index will only exist for (6.0.2,6.0.3)  so most of the time it will throw exception
        }
        finally
        {
            connection.close();
        }
    }
}

