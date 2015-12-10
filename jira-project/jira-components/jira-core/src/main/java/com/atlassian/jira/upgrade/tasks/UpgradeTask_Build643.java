package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Accumulates the "watchers" for issues.
 *
 * @since v4.4
 */
public class UpgradeTask_Build643 extends AbstractUpgradeTask
{
    public UpgradeTask_Build643()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "643";
    }

    @Override
    public String getShortDescription()
    {
        return "Accumulate total watchers by issue";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        final Connection connection = getDatabaseConnection();

        boolean committed = false;
        try
        {
            connection.setAutoCommit(false);

            patchIssues(connection);

            connection.commit();
            committed = true;
        }
        finally
        {
            if (!committed)
            {
                connection.rollback();
            }
            connection.close();
        }
    }

    private void patchIssues(Connection connection) throws SQLException
    {
        String selectSql  = "select sink_node_id, count(*) as watches from " + convertToSchemaTableName("userassociation") + " where association_type = 'WatchIssue' group by sink_node_id";
        String updateSql  = "update " + convertToSchemaTableName("jiraissue") + " set watches = ? where id = ? ";
        String updateSql2 = "update " + convertToSchemaTableName("jiraissue") + " set watches = 0";

        PreparedStatement selectStmt = connection.prepareStatement(selectSql);
        PreparedStatement updateStmt = connection.prepareStatement(updateSql);
        PreparedStatement updateStmt2 = connection.prepareStatement(updateSql2);

        updateStmt2.execute();
        ResultSet rs = selectStmt.executeQuery();
        while (rs.next())
        {
            int id = rs.getInt("sink_node_id");
            int watches = rs.getInt("watches");

            updateStmt.setInt(1, watches);
            updateStmt.setInt(2, id);
            updateStmt.execute();
        }
        rs.close();
        selectStmt.close();
        updateStmt.close();
    }
}
