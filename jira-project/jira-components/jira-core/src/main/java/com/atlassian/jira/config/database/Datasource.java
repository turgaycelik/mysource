package com.atlassian.jira.config.database;

import com.atlassian.config.bootstrap.AtlassianBootstrapManager;
import com.atlassian.config.bootstrap.BootstrapException;
import org.ofbiz.core.entity.config.DatasourceInfo;

import java.sql.Connection;

/**
 * A datasource that JIRA can use to connect to a database. Abstraction above the JNDI or JDBC configuration details and
 * the methods that rely on those details.
 */
public interface Datasource
{
    /**
     * Uses the given {@code link AtlassianBootstrapManager} to acquire a {@link Connection} to the database defined by
     * this {@link Datasource}.
     *
     * @param bootstrapManager the bootstrapManager to use to get the connection.
     * @return the connection.
     * @throws BootstrapException if the connection cannot be established.
     */
    Connection getConnection(AtlassianBootstrapManager bootstrapManager) throws BootstrapException;

    /**
     * Get the equivalent Ofbiz DatasourceInfo config class for this Datasource. This effectively translates between
     * this datasource and the ofbiz entityengine.xml of yore.
     *
     * @param datasourceName the ofbiz name of the datasource.
     * @param databaseType the field-type for the datasource as defined in ofbiz field-types.xml
     * @param schemaName the name of the schema for the database (may be empty)
     * @return an instance of a {@link DatasourceInfo} suitable for configuring Ofbiz.
     */
    DatasourceInfo getDatasource(String datasourceName, String databaseType, String schemaName);

    /**
     * Provides a text description suitable for an administrator that identifies the datasource.
     *
     * @param databaseType the field-type for the datasource as defined in ofbiz field-types.xml
     * @return the description.
     */
    String getDescriptorValue(String databaseType);

    /**
     * Provides the name of the datasource field that this datasource represents. Effectively, it delegates the name of
     * the datasource field when the configuration is being described to an administrator and will say "JDBC Config" or
     * "JNDI Name" as appropriate.
     * @return the label.
     */
    String getDescriptorLabel();
}
