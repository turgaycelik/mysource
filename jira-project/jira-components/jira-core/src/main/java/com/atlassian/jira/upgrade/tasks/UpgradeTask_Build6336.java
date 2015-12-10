package com.atlassian.jira.upgrade.tasks;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * Removes old indexes from jiraaction table that are rarely if ever helpful and may even be harmful to performance.
 *
 * @since v6.3.6
 */
public class UpgradeTask_Build6336 extends DropIndexTask
{
    private static final Logger LOG = Logger.getLogger(UpgradeTask_Build6336.class);

    public UpgradeTask_Build6336()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "6336";
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
    public void doUpgrade(boolean setupMode) throws Exception
    {
        if (setupMode)
        {
            // There is no need to do this on a clean install
            return;
        }

        final Connection connection = getDatabaseConnection();
        try
        {
            dropIndex(connection, "jiraaction", "action_authorcreated");
            dropIndex(connection, "jiraaction", "action_authorupdated");
        }
        finally
        {
            connection.close();
        }
    }

    @Override
    public String getShortDescription()
    {
        return "Remove old indexes from jiraaction table";
    }
}
