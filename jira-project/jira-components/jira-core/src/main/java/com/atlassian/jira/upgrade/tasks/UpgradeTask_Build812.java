package com.atlassian.jira.upgrade.tasks;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Reapplies Upgrade Task 787 to shorten Oracle MAnagedConfigurationItem index
 *
 * @since v5.2.1
 */
public class UpgradeTask_Build812 extends DropIndexTask
{
    public UpgradeTask_Build812()
    {
        super(false);
    }
    @Override
    public String getBuildNumber()
    {
        return "812";
    }
    @Override
    public String getShortDescription()
    {
        return "Shorten name of index on ManagedConfigurationItem entity";
    }
    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        final Connection connection = getDatabaseConnection();
        final String schemaName = getSchemaName();
        final String tableName = "managedconfigurationitem";
        final String index = "managedconfigurationitem_id_type_index";
        try
        {
            connection.setAutoCommit(false);
            final String sql = buildDropIndexSql(tableName, index);
            Statement update = connection.createStatement();
            update.execute(sql);
            connection.commit();
        }
        catch (Exception ignore) {
            // the index will only exist for 5.1.7 - 5..2 upgrades, so most of the time it will throw exception
        }
        finally
        {
            connection.close();
        }
    }
}

