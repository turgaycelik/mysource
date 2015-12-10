package com.atlassian.jira.upgrade.tasks;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * Remove old indexes from jiraaction and changegroup table that are replaced with better performing ones.
 *
 * @since v6.3.12
 */
public class UpgradeTask_Build6343 extends DropIndexTask
{
    private static final Logger LOG = Logger.getLogger(UpgradeTask_Build6343.class);

    public UpgradeTask_Build6343()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "6343";
    }

    private void dropIndex(final Connection connection, final String tableName, final String indexName)
    {
        try
        {
            final String sql = buildDropIndexSql(tableName, indexName);
            final Statement update = connection.createStatement();
            try
            {
                update.execute(sql);
            }
            finally
            {
                update.close();
            }
        }
        catch (SQLException sqle)
        {
            LOG.debug("Ignoring error dropping old index", sqle);
        }
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        if (setupMode)
        {
            // There is no need to do this on a clean install
            return;
        }

        final Connection connection = getDatabaseConnection();
        try
        {
            dropIndex(connection, "jiraaction", "action_author");
            dropIndex(connection, "changegroup", "chggroup_author");
        }
        finally
        {
            connection.close();
        }
    }

    @Override
    public String getShortDescription()
    {
        return "Remove old indexes from jiraaction and changegroup table";
    }
}
