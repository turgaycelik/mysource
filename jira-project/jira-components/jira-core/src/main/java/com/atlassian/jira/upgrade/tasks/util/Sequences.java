package com.atlassian.jira.upgrade.tasks.util;

import com.atlassian.core.ofbiz.CoreFactory;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericHelper;
import org.ofbiz.core.entity.config.DatasourceInfo;
import org.ofbiz.core.entity.config.EntityConfigUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility methods to manipulate entity engine database sequences.
 *
 * @since v4.4
 */
public class Sequences
{
    /**
     * Update the sequence value for the next id to use for this table.
     *
     * @param connection  The database connection to use to update the sequence.
     * @param sequenceName The name of the sequence to update.
     * @param tableName The name of the table that uses the value of the sequence for its primary key. The column which
     * stores the PK is assumed to be named &quot;id&quot;
     * @throws SQLException SQL exception
     */
    public void update(final Connection connection, final String sequenceName, final String tableName)
            throws SQLException
    {
        // Select the current maximum id in the table
        PreparedStatement stmt = connection.prepareStatement("select max(id) from " + convertToSchemaTableName(tableName));
        ResultSet rs = stmt.executeQuery();
        rs.next();
        int maxId = rs.getInt(1);
        rs.close();
        stmt.close();

        // Check if a row already exists
        stmt = connection.prepareStatement("select count(*) from " + convertToSchemaTableName("SEQUENCE_VALUE_ITEM") + " where seq_name = ?");
        stmt.setString(1, sequenceName);
        rs = stmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        stmt.close();


        // Generate a new id higher than the rest and rounded to base 100
        int id = maxId + 100;
        id -= id % 100;

        // So as not to clash with manually inserted system ids, by tradition we start at 10000 or above.
        if (id < 10000)
        {
            id = 10000;
        }

        if (count == 0)
        {
            stmt = connection.prepareStatement("insert into " + convertToSchemaTableName("SEQUENCE_VALUE_ITEM") + " (seq_name, seq_id) values(?, ?)");
            stmt.setString(1, sequenceName);
            stmt.setInt(2, id);
            stmt.execute();
            stmt.close();
        }
        else
        {
            stmt = connection.prepareStatement("update " + convertToSchemaTableName("SEQUENCE_VALUE_ITEM") + " set seq_id = ? where seq_name = ?");
            stmt.setInt(1, id);
            stmt.setString(2, sequenceName);
            stmt.execute();
            stmt.close();
        }
    }

    protected GenericDelegator getDelegator()
    {
        return CoreFactory.getGenericDelegator();
    }

    protected String convertToSchemaTableName(final String tableName)
    {
        GenericHelper helper;
        try
        {
            helper = getDelegator().getEntityHelper("User");
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
        DatasourceInfo datasourceInfo = EntityConfigUtil.getInstance().getDatasourceInfo(helper.getHelperName());
        String schemaName = datasourceInfo.getSchemaName();

        if (schemaName != null && schemaName.length() > 0 && !tableName.startsWith(schemaName))
        {
            // Prepend the schema name
            return schemaName + "." + tableName;
        }
        return tableName;
    }
}
