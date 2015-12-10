package com.atlassian.jira.upgrade.util;

import com.atlassian.jira.appconsistency.db.TableColumnCheckResult;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.config.DatasourceInfo;
import org.ofbiz.core.entity.jdbc.DatabaseUtil;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelReader;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpgradeUtils
{
    private static final Logger log = Logger.getLogger(UpgradeUtils.class);

    private final OfBizDelegator delegator;
    private static final OfBizConnectionFactory factory = DefaultOfBizConnectionFactory.getInstance();
    private static final Pattern FULLY_QUALIFIED_TABLE_NAME = Pattern.compile("(.+\\.)?(.*)");

    public UpgradeUtils(OfBizDelegator delegator)
    {
        this.delegator = delegator;
    }

    /**
     * Transform a column in a table into another column on that same table. If the source object is null, then the
     * target field will not be set
     *
     * @param tableName name of table @Nonnull
     * @param sourceColumn name of source column @Nonnull
     * @param targetColumn name of target columns @Nonnull
     * @param transformer the transformer used to transform the column
     */
    public void transformTableColumn(String tableName, String sourceColumn, String targetColumn, Transformer transformer)
    {
        for (GenericValue row : delegator.findAll(tableName))
        {
            try
            {
                Object sourceValue = row.get(sourceColumn);
                if (sourceValue != null)
                {
                    Object transformedValue = transformer.transform(sourceValue);
                    row.set(targetColumn, transformedValue);
                    row.store();
                }
            }
            catch (GenericEntityException e)
            {
                log.error("Failed to upgrade table row " + row + " exception being thrown", e);
                throw new DataAccessException(e);
            }
        }
    }

    public void clearColumn(String tableName, String column)
    {
        for (final GenericValue row : delegator.findAll(tableName))
        {
            try
            {
                row.set(column, null);
                row.store();
            }
            catch (GenericEntityException e)
            {
                log.error("Failed to upgrade table row " + row + " exception being thrown", e);
                throw new DataAccessException(e);
            }
        }
    }

    /**
     * This method is used to verify if a named table exists or not within the database.
     *
     * @param tableName is the name of the table to look for.
     * @return true if the table, exists false otherwise.
     * @throws DataAccessException If there is an error getting a DB connection, or getting the DB meta data
     */
    public static boolean tableExists(String tableName) throws DataAccessException
    {
        ResultSet tables = null;
        Connection connection = null;
        try
        {
            // Validate old table column
            connection = factory.getConnection();
            if (connection == null)
            {
                throw new DataAccessException("Unable to get a database connection for the OfBiz default data source.");
            }
            DatabaseMetaData metaData = connection.getMetaData();

            DatasourceInfo datasourceInfo = factory.getDatasourceInfo();
            String schemaName = datasourceInfo.getSchemaName();

            String schemaPattern = DatabaseUtil.getSchemaPattern(metaData, schemaName);
            tables = metaData.getTables(null, schemaPattern, null, null);
            while (tables.next())
            {
                String currentTableName = tables.getString("TABLE_NAME");

                if (areTableNamesEquivalent(tableName, currentTableName))
                {
                    return true;
                }
            }

            // table not found
            return false;
        }
        catch (SQLException e)
        {
            throw new DataAccessException(e);
        }
        finally
        {
            silentlyClose(tables);
            silentlyClose(connection);
        }
    }

    /**
     * Returns the exact column name according to the database. This is found by case insensitively looking for the
     * given column on the given table.
     *
     * @param tableName Table name
     * @param columnName Column name
     * @return the exact column name or null if it doesn't exist.
     */
    public static String getExactColumnName(String tableName, String columnName)
    {

        ResultSet columns = null;
        Connection connection = null;
        try
        {
            // Validate old table column
            connection = factory.getConnection();

            DatabaseMetaData metaData = connection.getMetaData();

            DatasourceInfo datasourceInfo = factory.getDatasourceInfo();
            String schemaName = datasourceInfo.getSchemaName();

            String schemaPattern = DatabaseUtil.getSchemaPattern(metaData, schemaName);
            columns = metaData.getColumns(null, schemaPattern, null, null);
            while (columns.next())
            {
                String currentTableName = columns.getString("TABLE_NAME");
                String exactColumnName = columns.getString("COLUMN_NAME");

                if (areTableNamesEquivalent(tableName, currentTableName)
                        && exactColumnName.equalsIgnoreCase(columnName))
                {
                    return exactColumnName;
                }
            }

            // column not found
            return null;
        }
        catch (SQLException e)
        {
            throw new DataAccessException(e);
        }
        finally
        {
            silentlyClose(columns);
            silentlyClose(connection);
        }
    }

    public static String getExactTableName(String entityName)
    {

        String tableName;
        ModelReader modelReader;
        try
        {
            modelReader = ModelReader.getModelReader(factory.getDelegatorName());
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        try
        {
            ModelEntity modelEntity = modelReader.getModelEntity(entityName);
            tableName = modelEntity.getTableName(factory.getDatasourceInfo().getName());
        }
        catch (GenericEntityException e)
        {
            throw new IllegalArgumentException("EntityName not found: " + entityName + ": " + e.getMessage());
        }
        return tableName;
    }

    private static String stripSchemaPrefix(String tableName)
    {
        String justTable = tableName;
        Matcher matcher = FULLY_QUALIFIED_TABLE_NAME.matcher(tableName);
        if (matcher.matches())
        {
            justTable = matcher.group(2);
        }
        return justTable;
    }

    /**
     * Compares the two given table names case insensitively ignoring the schemas
     *
     * @param table1 table name to compare
     * @param table2 table name to compare
     * @return true only if the table names are equivalent.
     */
    private static boolean areTableNamesEquivalent(String table1, String table2)
    {
        return stripSchemaPrefix(table1).equalsIgnoreCase(stripSchemaPrefix(table2));
    }

    /**
     * This will return the current build version of JIRA present in the database. If JIRA has not been setup then 0
     * (zero) will be returned as the currnet build number.
     *
     * @return int the current build version of JIRA
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings (value = "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE", justification = "The string is nonconstant but still safe.")
    public static int getJIRABuildVersionNumber()
    {
        int buildVersionNumber = 0;
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        // Find 'real' table names
        String osPropertyEntry = getExactTableName("OSPropertyEntry");
        String osPropertyString = getExactTableName("OSPropertyString");

        // Find real column names
        String psPropertyValue = getExactColumnName(osPropertyString, "propertyvalue");
        String pePropertyKey = getExactColumnName(osPropertyEntry, "property_key");
        String peId = getExactColumnName(osPropertyEntry, "id");
        String psId = getExactColumnName(osPropertyString, "id");

        StringBuilder sql = new StringBuilder();
        try
        {
            connection = factory.getConnection();

            sql.append("SELECT ps.").append(psPropertyValue).append(" ");
            sql.append("FROM ").append(osPropertyEntry).append(" pe, ").append(osPropertyString).append(" ps ");
            sql.append("WHERE pe.").append(pePropertyKey).append("=").append("'jira.version.patched' ");
            sql.append("AND pe.").append(peId).append("=").append("ps.").append(psId);

            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql.toString());
            if (resultSet.next())
            {
                buildVersionNumber = Integer.parseInt(resultSet.getString(psPropertyValue));
            }
        }
        catch (Exception e)
        {
            log.error("JDBC get version number failed. SQL: " + sql.toString(), e);
            throw new DataAccessException(e);
        }
        finally
        {
            if (statement != null)
            {
                try
                {
                    statement.close();
                }
                catch (SQLException e)
                {
                    // Oh Well :(
                }
            }

            silentlyClose(resultSet);
            silentlyClose(connection);
        }
        return buildVersionNumber;
    }

    public static void doColumnsOrTablesExist(List<TableColumnCheckResult> tableColumnCheckResults)
    {
        ResultSet columns = null;
        Connection connection = null;
        try
        {
            // Validate old table column
            connection = factory.getConnection();

            DatabaseMetaData metaData = connection.getMetaData();

            DatasourceInfo datasourceInfo = factory.getDatasourceInfo();
            String schemaName = datasourceInfo.getSchemaName();

            String schemaPattern = DatabaseUtil.getSchemaPattern(metaData, schemaName);
            columns = metaData.getColumns(null, schemaPattern, null, null);
            while (columns.next())
            {
                String currentTableName = columns.getString("TABLE_NAME");
                String exactColumnName = columns.getString("COLUMN_NAME");

                // Iterate over all the table/columns we are looking for
                for (final TableColumnCheckResult tableColumnCheckResult : tableColumnCheckResults)
                {
                    if (!tableColumnCheckResult.isExists())
                    {
                        // If the column name is null then we will only try to match on the table name
                        if (areTableNamesEquivalent(tableColumnCheckResult.getTableName(), currentTableName)
                                && (tableColumnCheckResult.getColumnName() == null || exactColumnName.equalsIgnoreCase(tableColumnCheckResult.getColumnName())))
                        {
                            // If the column/table exists lets record it
                            tableColumnCheckResult.setExists(true);
                        }
                    }
                }

            }
        }
        catch (SQLException e)
        {
            throw new DataAccessException(e);
        }
        finally
        {
            silentlyClose(columns);
            silentlyClose(connection);
        }
    }


    private static void silentlyClose(ResultSet resultSet)
    {
        if (resultSet != null)
        {
            try
            {
                resultSet.close();
            }
            catch (SQLException e)
            {
                // Oh well
            }
        }
    }

    private static void silentlyClose(Connection connection)
    {
        if (connection != null)
        {
            try
            {
                connection.close();
            }
            catch (SQLException e)
            {
                // Oh well
            }
        }
    }


}
