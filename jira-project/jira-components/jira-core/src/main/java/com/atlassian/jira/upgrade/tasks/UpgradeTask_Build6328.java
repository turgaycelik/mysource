package com.atlassian.jira.upgrade.tasks;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.model.ModelEntity;

/**
 * Increase width of time-related columns in Quartz tables on Oracle.
 *
 * @since v6.3
 */
public class UpgradeTask_Build6328 extends AbstractUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(UpgradeTask_Build6328.class);
    private Statement statement;

    private static final Map<String,List<String>> TARGETS = ImmutableMap.<String,List<String>>builder()
            .put("JQRTZTriggers", fields("nextFireTime", "prevFireTime", "startTime", "endTime"))
            .put("JQRTZSimpleTriggers", fields("repeatCount", "repeatInterval", "timesTriggered"))
            .put("JQRTZSimplePropTriggers", fields("long_prop_1", "long_prop_2"))
            .put("JQRTZFiredTriggers", fields("firedTime", "schedTime"))
            .put("JQRTZSchedulerState", fields("lastCheckinTime", "checkinInterval"))
            .build();

    public UpgradeTask_Build6328()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "6328";
    }

    @Override
    public String getShortDescription()
    {
        return "Widen Quartz time fields from NUMBER(13) to NUMBER(18) on Oracle";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        if (setupMode || !isORACLE())
        {
            // This upgrade task is Oracle-specific and unnecessary on a fresh database.
            return;
        }

        final Connection conn = getDatabaseConnection();
        try
        {
            alterAllEntities(conn);
        }
        finally
        {
            conn.close();
        }
    }

    private void alterAllEntities(final Connection conn) throws SQLException, GenericEntityException
    {
        statement = conn.createStatement();
        try
        {
            alterAllEntities();
        }
        finally
        {
            final Statement oldStatement = statement;
            statement = null;
            oldStatement.close();
        }
    }

    private void alterAllEntities() throws SQLException, GenericEntityException
    {
        for (Map.Entry<String, List<String>> entry : TARGETS.entrySet())
        {
            alterEntityFields(entry.getKey(), entry.getValue());
        }
    }

    private void alterEntityFields(final String entityName, final List<String> fieldNames)
            throws SQLException, GenericEntityException
    {
        final ModelEntity modelEntity = getOfBizDelegator().getModelReader().getModelEntity(entityName);
        final String tableName = convertToSchemaTableName(modelEntity.getPlainTableName());
        for (String fieldName : fieldNames)
        {
            final String columnName = modelEntity.getField(fieldName).getColName();
            execute("ALTER TABLE " + tableName + " MODIFY (" + columnName + " NUMBER(18))");
        }
    }

    private int execute(final String sql) throws SQLException
    {
        LOG.debug(sql);
        statement.execute(sql);
        return statement.getUpdateCount();
    }

    private static List<String> fields(final String... fields)
    {
        return ImmutableList.copyOf(fields);
    }
}
