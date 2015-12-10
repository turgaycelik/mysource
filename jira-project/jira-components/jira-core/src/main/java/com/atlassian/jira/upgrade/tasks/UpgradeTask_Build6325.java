package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static com.atlassian.jira.permission.LegacyProjectPermissionKeyMapping.ID_TO_KEY;

/**
 * @since v6.3
 */
public class UpgradeTask_Build6325 extends AbstractUpgradeTask
{
    private static final Logger LOG = LoggerFactory.getLogger(UpgradeTask_Build6325.class);

    public UpgradeTask_Build6325()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "6325";
    }

    @Override
    public String getShortDescription()
    {
        return "Populate project permission entry keys based on ID-s";
    }

    @SuppressWarnings(value="SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE", justification="Using a static map for a one-time update")
    @Override
    public void doUpgrade(boolean setupMode) throws SQLException
    {
        Connection connection = null;
        Statement statement = null;
        try
        {
            connection = getDatabaseConnection();
            statement = connection.createStatement();

            StringBuilder sql = new StringBuilder("UPDATE ").
                    append(convertToSchemaTableName("schemepermissions")).
                    append(" SET permission_key = (CASE");

            for (Map.Entry<Integer, ProjectPermissionKey> entry : ID_TO_KEY.entrySet())
            {
                Integer permissionId = entry.getKey();
                String permissionKey = entry.getValue().permissionKey();

                sql.append(" WHEN permission = ").append(permissionId).
                append(" THEN '").append(permissionKey).append("'");
            }

            sql.append(" ELSE permission_key END) WHERE scheme IS NOT NULL");

            int updated = statement.executeUpdate(sql.toString());

            LOG.info("Updated {} project permission entries", updated);
        }
        finally
        {
            if (statement != null)
            {
                statement.close();
            }
            if (connection != null)
            {
                connection.close();
            }
        }
    }
}
