package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.ofbiz.core.entity.model.ModelEntity;

import java.sql.Connection;
import java.sql.Statement;

import static java.lang.String.format;

public class UpgradeTask_Build6258 extends AbstractUpgradeTask {
    private static String ENTITY_NAME = "AuditLog";

    public UpgradeTask_Build6258() {
        super(false);
    }

    @Override
    public String getBuildNumber() {
        return "6258";
    }

    @Override
    public String getShortDescription() {
        return "Change AuditLog search field to CLOB on Oracle";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception {
        if (!isORACLE())
        {
            return;
        }

        final ModelEntity auditLogTable = getOfBizDelegator().getModelReader().getModelEntity(ENTITY_NAME);
        final String searchFieldColumn = auditLogTable.getField("searchField").getColName();
        final String tableName = convertToSchemaTableName(auditLogTable.getPlainTableName());

        final Connection connection = getDatabaseConnection();
        try
        {
            // DDL-s in Oracle auto commit after each statement by design, no use of keeping this in a transaction
            final Statement statement = connection.createStatement();
            statement.execute(format("ALTER TABLE %s ADD (tmp_sf CLOB)", tableName));
            statement.execute(format("UPDATE %s SET tmp_sf = %s", tableName, searchFieldColumn));
            statement.execute(format("ALTER TABLE %s DROP COLUMN %s", tableName, searchFieldColumn));
            statement.execute(format("ALTER TABLE %s RENAME COLUMN tmp_sf TO %s", tableName, searchFieldColumn));
        }
        finally
        {
            connection.close();
        }
    }
}
