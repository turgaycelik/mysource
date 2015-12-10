package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.model.ModelEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Fix field type for AuditLog searchField
 *
 * @since v6.3
 */
public class UpgradeTask_Build6301 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6301.class);

    private static String ENTITY_NAME = "AuditLog";
    private static String TABLE_NAME = "audit_log";

    public UpgradeTask_Build6301()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "6301";
    }

    @Override
    public String getShortDescription()
    {
        return "Fix field type for AuditLog searchField";
    }

    @Override
    public void doUpgrade(boolean setupMode)
    {
        try
        {
            // the alter table syntax is specific to postgresql
            // JDEV-27398 affected only customers of OnDemand (root cause was fixed in 6.2-OD9) so
            //   we do not have to deal with other databases
            if (isPostgreSQL())
            {
                final ModelEntity auditLogTable = getOfBizDelegator().getModelReader().getModelEntity(ENTITY_NAME);
                final String searchFieldColumn = auditLogTable.getField("searchField").getColName();
                final Connection connection = getDatabaseConnection();
                try
                {
                    final Statement statement = connection.createStatement();
                    statement.execute("ALTER TABLE " + convertToSchemaTableName(TABLE_NAME) + " ALTER COLUMN " + searchFieldColumn + " SET DATA TYPE TEXT");
                }
                finally
                {
                    connection.close();
                }
            }
        }
        catch (SQLException e)
        {
            log.warn("Problem while changing field type for audit_log search_field. "
                    + "If search_field is already of type text this can be safely ignored.", e);
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
    }
}
