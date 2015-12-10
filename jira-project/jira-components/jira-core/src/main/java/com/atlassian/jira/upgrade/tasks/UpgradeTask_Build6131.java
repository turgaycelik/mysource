package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Removes issue keys from jiraissue table.
 *
 * @since v6.1
 */
public class UpgradeTask_Build6131 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6131.class);

    public UpgradeTask_Build6131()
    {
        super(false);
    }


    @Override
    public String getBuildNumber()
    {
        return "6131";
    }

    @Override
    public String getShortDescription()
    {
        return "Removes issue keys from jiraissue table.";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        final Connection connection = getDatabaseConnection();
        try
        {
            final String updateSql = "UPDATE " + convertToSchemaTableName("jiraissue") + " SET pkey = NULL WHERE pkey IS NOT NULL";

            PreparedStatement updateStmt = connection.prepareStatement(updateSql);
            try
            {
                int updatedCount = updateStmt.executeUpdate();
                log.info(String.format("Updated %d issues.", updatedCount));
            }
            finally
            {
                updateStmt.close();
            }
        }
        finally
        {
            connection.close();
        }
    }
}
