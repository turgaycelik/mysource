package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.model.ModelEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Fills in originalKey in project table.
 *
 * @since v6.1
 */
public class UpgradeTask_Build6135 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6135.class);
    private final ProjectManager projectManager;

    private final String PROJECT_ENTITY_NAME = "Project";
    private final String PROJECT_TABLE_NAME = "project";

    public UpgradeTask_Build6135(ProjectManager projectManager)
    {
        super(false);
        this.projectManager = projectManager;
    }


    @Override
    public String getBuildNumber()
    {
        return "6135";
    }

    @Override
    public String getShortDescription()
    {
        return "Filling in originalKey in project table.";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        final Connection connection = getDatabaseConnection();
        try
        {
            final ModelEntity issueTable = getOfBizDelegator().getModelReader().getModelEntity(PROJECT_ENTITY_NAME);
            final String originalKeyColumn = issueTable.getField("originalkey").getColName();
            final String keyColumn = issueTable.getField("key").getColName();

            final String updateSql = "UPDATE " + convertToSchemaTableName(PROJECT_TABLE_NAME)
                    + " SET " + originalKeyColumn + "=" + keyColumn + " WHERE " + originalKeyColumn + " IS NULL";

            PreparedStatement updateStmt = connection.prepareStatement(updateSql);
            try
            {
                int updatedCount = updateStmt.executeUpdate();
                log.info(String.format("Updated %d projects.", updatedCount));
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
        projectManager.refresh();
    }
}
