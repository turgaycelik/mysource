package com.atlassian.jira.scheduler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.TimeZone;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.cluster.ClusterNodeProperties;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.scheduler.quartz1.Quartz1DefaultSettingsFactory;
import com.atlassian.scheduler.quartz1.spi.Quartz1SchedulerConfiguration;

import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericHelper;
import org.ofbiz.core.entity.config.EntityConfigUtil;
import org.ofbiz.core.entity.jdbc.dbtype.DatabaseType;
import org.ofbiz.core.entity.jdbc.dbtype.DatabaseTypeFactory;
import org.quartz.simpl.RAMJobStore;

/**
 * @since v6.3
 */
public class JiraQuartz1SchedulerConfiguration implements Quartz1SchedulerConfiguration
{
    private static final String PREFIX = "JQUARTZ_";
    private static final String USE_RAM_JOB_STORE = "jira.scheduler.RamJobStore";

    private final ApplicationProperties applicationProperties;
    private final ClusterNodeProperties clusterNodeProperties;
    private final DelegatorInterface delegatorInterface;
    private final JiraProperties jiraProperties;

    public JiraQuartz1SchedulerConfiguration(final ApplicationProperties applicationProperties,
            final ClusterNodeProperties clusterNodeProperties, final DelegatorInterface delegatorInterface,
            final JiraProperties jiraProperties)
    {
        this.applicationProperties = applicationProperties;
        this.clusterNodeProperties = clusterNodeProperties;
        this.delegatorInterface = delegatorInterface;
        this.jiraProperties = jiraProperties;
    }

    // Note: the Properties objects returned by this class are only retrieved once during the
    // construction of the SchedulerService.  There is no need to keep them around and reuse them.

    @Nonnull
    @Override
    public Properties getLocalSettings()
    {
        return Quartz1DefaultSettingsFactory.getDefaultLocalSettings();
    }

    @Nonnull
    @Override
    public Properties getClusteredSettings()
    {
        if (jiraProperties.getBoolean(USE_RAM_JOB_STORE))
        {
            return getRamJobStoreClusteredSettings();
        }

        final Properties clusteredProperties = Quartz1DefaultSettingsFactory.getDefaultClusteredSettings();
        clusteredProperties.setProperty("org.quartz.jobStore.tablePrefix", getTablePrefix());
        clusteredProperties.setProperty("org.quartz.jobStore.dataSource", "JiraDataSource");
        clusteredProperties.setProperty("org.quartz.jobStore.driverDelegateClass", getDelegateClassName());
        clusteredProperties.setProperty("org.quartz.dataSource.JiraDataSource.connectionProvider.class",
                Quartz1ConnectionProvider.class.getName());
        clusteredProperties.setProperty("org.quartz.scheduler.classLoadHelper.class", Quartz1ClassLoadHelper.class.getName());
        clusteredProperties.setProperty("org.quartz.jobStore.isClustered", getIsClustered());
        clusteredProperties.setProperty("org.quartz.scheduler.instanceId", getInstanceName());
        return clusteredProperties;
    }

    private Properties getRamJobStoreClusteredSettings()
    {
        // Trying to use the RAM job store in a cluster would be very, very broken.
        if (isClustered())
        {
            throw new IllegalArgumentException("You can have any setting for isClustered that you want, as long as it's \"false\".");
        }

        final Properties clusteredProperties = Quartz1DefaultSettingsFactory.getDefaultClusteredSettings();
        clusteredProperties.setProperty("org.quartz.jobStore.class", RAMJobStore.class.getName());
        clusteredProperties.setProperty("org.quartz.scheduler.instanceId", getInstanceName());
        clusteredProperties.remove("org.quartz.jobStore.isClustered");  // RAMJobStore doesn't like this
        return clusteredProperties;
    }

    private String getInstanceName()
    {
        String nodeId = clusterNodeProperties.getNodeId();
        if (nodeId == null)
        {
            return "NON_CLUSTERED";
        }
        return nodeId;
    }

    private boolean isClustered()
    {
        // Using ClusterManager.isClustered() instead would create a circular dependency
        return clusterNodeProperties.getNodeId() != null;
    }

    private String getIsClustered()
    {
        return String.valueOf(isClustered());
    }

    private String getTablePrefix()
    {
        String schemaName = getSchemaName();
        String tablePrefix;
        if (StringUtils.isEmpty(schemaName))
        {
            tablePrefix = PREFIX;
        }
        else
        {
            tablePrefix = schemaName + '.' + PREFIX;
        }
        return tablePrefix;
    }


    protected String getSchemaName()
    {
        GenericHelper helper = null;
        try
        {
            helper = delegatorInterface.getEntityHelper("User");
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
        return EntityConfigUtil.getInstance().getDatasourceInfo(helper.getHelperName()).getSchemaName();
    }



    @Nullable
    @Override
    public TimeZone getDefaultTimeZone()
    {
        final String zoneId = applicationProperties.getString(APKeys.JIRA_DEFAULT_TIMEZONE);
        return (zoneId != null) ? TimeZone.getTimeZone(zoneId) : null;
    }



    private static String getDelegateClassName()
    {
        try
        {
            final Connection connection = new Quartz1ConnectionProvider().getConnection();
            if (connection == null)
            {
                throw new DataAccessException("Unable to obtain a DB connection");
            }
            try
            {
                return getDatabaseDelegate(DatabaseTypeFactory.getTypeForConnection(connection));
            }
            finally
            {
                connection.close();
            }
        }
        catch (SQLException e)
        {
            throw new DataAccessException("Unable to obtain a DB connection", e);
        }
    }

    @SuppressWarnings({ "OverlyComplexMethod", "ObjectEquality" })
    private static String getDatabaseDelegate(final DatabaseType type)
    {
        if (type == DatabaseTypeFactory.DB2) return "org.quartz.impl.jdbcjobstore.DB2v8Delegate";
        if (type == DatabaseTypeFactory.CLOUDSCAPE) return "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";
        if (type == DatabaseTypeFactory.HSQL) return "org.quartz.impl.jdbcjobstore.HSQLDBDelegate";
        if (type == DatabaseTypeFactory.H2) return "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";
        if (type == DatabaseTypeFactory.MYSQL) return "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";
        if (type == DatabaseTypeFactory.MSSQL) return "org.quartz.impl.jdbcjobstore.MSSQLDelegate";
        if (type == DatabaseTypeFactory.ORACLE_10G) return "org.quartz.impl.jdbcjobstore.oracle.OracleDelegate";
        if (type == DatabaseTypeFactory.ORACLE_8I) return "org.quartz.impl.jdbcjobstore.oracle.OracleDelegate";
        if (type == DatabaseTypeFactory.POSTGRES_7_2) return "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate";
        if (type == DatabaseTypeFactory.POSTGRES_7_3) return "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate";
        if (type == DatabaseTypeFactory.POSTGRES) return "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate";
        if (type == DatabaseTypeFactory.SYBASE) return "org.quartz.impl.jdbcjobstore.SybaseDelegate";
        return "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";
    }
}
