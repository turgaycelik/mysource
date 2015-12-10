package com.atlassian.jira.upgrade.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

/**
 * Finds the earliest reporter of an issue and sets the creator to that reporter
 *
 * @since v6.2
 */
public class UpgradeTask_Build6208 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6208.class);

    private final EntityEngine entityEngine;

    public UpgradeTask_Build6208(final EntityEngine entityEngine)
    {
        super(true);
        this.entityEngine = entityEngine;
    }

    @Override
    public String getBuildNumber()
    {
        return "6208";
    }

    @Override
    public String getShortDescription()
    {
        return "Populates the issue creator field";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        Connection connection = null;
        try
        {
            connection = getDatabaseConnection();
            copyReporterToCreator(connection);
            fixUpChangedReporters();
        }
        finally
        {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private void copyReporterToCreator(Connection connection) throws Exception
    {
        PreparedStatement updateStmt = null;
        try
        {
            final String updateSql = "UPDATE " + convertToSchemaTableName("jiraissue") + " SET creator = reporter";
            updateStmt = connection.prepareStatement(updateSql);
            int updatedCount = updateStmt.executeUpdate();
            log.info(String.format("Updated %d issues.", updatedCount));
        }
        finally
        {
            if (updateStmt != null)
            {
                updateStmt.close();
            }
        }
    }

    private void fixUpChangedReporters()
    {
        List<GenericValue> reporterChanges = Select.from("ChangeGroupChangeItemView").whereEqual("field", "reporter").orderBy("changegroupid desc").runWith(entityEngine).asList();
        for (GenericValue change : reporterChanges)
        {
            long issueId = change.getLong("issue");
            String reporter = change.getString("oldvalue");
            entityEngine.execute(Update.into("Issue").set("creator", reporter).whereEqual("id", issueId));
        }
    }
}