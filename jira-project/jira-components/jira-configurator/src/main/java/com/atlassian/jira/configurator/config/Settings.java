package com.atlassian.jira.configurator.config;

import com.atlassian.jira.config.database.DatabaseType;
import com.atlassian.jira.config.database.JdbcDatasource;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.ofbiz.core.entity.config.ConnectionPoolInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores raw settings as read from the config files
 */
public class Settings
{
    // We override the default for this field
    public static final int DEFAULT_POOL_MAX_SIZE = 20;
    public static final long DEFAULT_POOL_MAX_WAIT = 30000L;
    public static final int DEFAULT_REMOVE_ABANDONED_TIMOUT = 300;

    // Defaults specific to HSQL
    public static final Long HSQL_MIN_EVICTABLE_TIME_MILLIS = 4000L;
    public static final Long HSQL_TIME_BETWEEN_EVICTION_RUNS_MILLIS = 5000L;

    // Defaults specifc to MySQL
    public static final Long MYSQL_MIN_EVICTABLE_TIME_MILLIS = 60000L;
    public static final Long MYSQL_TIME_BETWEEN_EVICTION_RUNS_MILLIS = 300000L;
    public static final String MYSQL_VALIDATION_QUERY = "select 1";
    public static final int MYSQL_VALIDATION_QUERY_TIMEOUT = 3;

    private String jiraHome;
    private String schemaName;
    private String httpPort = "8080";
    private String controlPort = "8005";
    private SslSettings sslSettings;
    private WebServerProfile webServerProfile = WebServerProfile.HttpOnly;
    private List<ComplexConfigurationReason> complexConfigurationReasons = new ArrayList<ComplexConfigurationReason>();

    private JdbcDatasource.Builder jdbcDatasourceBuilder = JdbcDatasource.builder();
    private ConnectionPoolInfo.Builder connectionPoolInfoBuilder = newConnectionPoolInfoBuilder();

    public String getJiraHome()
    {
        return jiraHome;
    }

    public void setJiraHome(final String jiraHome)
    {
        this.jiraHome = jiraHome;
    }

    public String getSchemaName()
    {
        return schemaName;
    }

    public void setSchemaName(final String schemaName)
    {
        this.schemaName = schemaName;
    }

    public String getHttpPort()
    {
        return httpPort;
    }

    public void setHttpPort(String httpPort)
    {
        this.httpPort = httpPort;
        updateWebServerProfile();
    }

    public String getControlPort()
    {
        return controlPort;
    }

    public void setControlPort(String controlPort)
    {
        this.controlPort = controlPort;
    }

    @Nullable
    public SslSettings getSslSettings()
    {
        return sslSettings;
    }

    public void setSslSettings(@Nullable final SslSettings sslSettings)
    {
        this.sslSettings = sslSettings;
        updateWebServerProfile();
    }

    @Nonnull
    public WebServerProfile getWebServerProfile()
    {
        return webServerProfile;
    }

    public void updateWebServerConfiguration(@Nullable final String httpPort, @Nullable final SslSettings sslSettings)
    {
        this.httpPort = httpPort;
        this.sslSettings = sslSettings;
        updateWebServerProfile();
    }

    private void updateWebServerProfile()
    {
        this.webServerProfile = WebServerProfile.retrieveByFlags(httpPort != null, sslSettings != null);
    }

    @Nonnull
    public List<ComplexConfigurationReason> getComplexConfigurationReasons()
    {
        return Collections.unmodifiableList(complexConfigurationReasons);
    }

    public void addComplexConfigurationReason(@Nonnull final ComplexConfigurationReason complexConfigurationReason)
    {
        this.complexConfigurationReasons.add(complexConfigurationReason);
    }

    public JdbcDatasource.Builder getJdbcDatasourceBuilder()
    {
        return jdbcDatasourceBuilder;
    }

    public void setJdbcDatasourceBuilder(JdbcDatasource.Builder jdbcDatasourceBuilder)
    {
        this.jdbcDatasourceBuilder = (jdbcDatasourceBuilder != null) ? jdbcDatasourceBuilder : JdbcDatasource.builder();
    }

    public ConnectionPoolInfo.Builder getConnectionPoolInfoBuilder()
    {
        return connectionPoolInfoBuilder;
    }

    public void setConnectionPoolInfoBuilder(ConnectionPoolInfo.Builder connectionPoolInfoBuilder)
    {
        if (connectionPoolInfoBuilder != null)
        {
            if (connectionPoolInfoBuilder.getPoolMaxSize() == null)
            {
                connectionPoolInfoBuilder.setPoolMaxSize(DEFAULT_POOL_MAX_SIZE);
            }
            this.connectionPoolInfoBuilder = connectionPoolInfoBuilder;
        }
        else
        {
            this.connectionPoolInfoBuilder = newConnectionPoolInfoBuilder();
        }
    }

    /**
     * Determines the database type from the JDBC driver's class name.
     * 
     * @param initialLoad indicates whether the database type is being reset
     *      in response to loading the database configuration file.  If it is,
     *      then any settings that match database-specific defaults are cleared
     *      to avoid impacting another database type if it gets changed.
     * @return the resulting database type
     * @throws IllegalArgumentException if the JDBC driver's class name is
     *      not recognized.
     */
    public DatabaseType initDatabaseType(final boolean initialLoad)
    {
        final DatabaseType newDatabaseType = DatabaseType.forJdbcDriverClassName(jdbcDatasourceBuilder.getDriverClassName());
        jdbcDatasourceBuilder.setDatabaseType(newDatabaseType);
        if (initialLoad)
        {
            removeDefaultAdvancedSettings(newDatabaseType);
        }
        return newDatabaseType;
    }

    private void removeDefaultAdvancedSettings(DatabaseType databaseType)
    {
        switch (databaseType)
        {
            case MY_SQL:
            {
                final String validationQuery = connectionPoolInfoBuilder.getValidationQuery();
                if (validationQuery != null && validationQuery.trim().equalsIgnoreCase(MYSQL_VALIDATION_QUERY))
                {
                    connectionPoolInfoBuilder.setValidationQuery(null);
                }
                final Integer validationQueryTimeout = connectionPoolInfoBuilder.getValidationQueryTimeout();
                if (validationQueryTimeout != null && validationQueryTimeout == MYSQL_VALIDATION_QUERY_TIMEOUT)
                {
                    connectionPoolInfoBuilder.setValidationQueryTimeout(null);
                }
                final Boolean testWhileIdle = connectionPoolInfoBuilder.getTestWhileIdle();
                if (testWhileIdle != null && testWhileIdle == Boolean.TRUE)
                {
                    connectionPoolInfoBuilder.setTestWhileIdle(null);
                }
                final Long minEvictableTimeMillis = connectionPoolInfoBuilder.getMinEvictableTimeMillis();
                if (minEvictableTimeMillis != null && minEvictableTimeMillis.equals(MYSQL_MIN_EVICTABLE_TIME_MILLIS))
                {
                    connectionPoolInfoBuilder.setMinEvictableTimeMillis(null);
                }
                final Long timeBetweenEvictionRunsMillis = connectionPoolInfoBuilder.getTimeBetweenEvictionRunsMillis();
                if (timeBetweenEvictionRunsMillis != null && timeBetweenEvictionRunsMillis.equals(MYSQL_TIME_BETWEEN_EVICTION_RUNS_MILLIS))
                {
                    connectionPoolInfoBuilder.setTimeBetweenEvictionRunsMillis(null);
                }
                break;
            }
            case HSQL:
            {
                final Long minEvictableTimeMillis = connectionPoolInfoBuilder.getMinEvictableTimeMillis();
                if (minEvictableTimeMillis != null && minEvictableTimeMillis.equals(HSQL_MIN_EVICTABLE_TIME_MILLIS))
                {
                    connectionPoolInfoBuilder.setMinEvictableTimeMillis(null);
                }
                final Long timeBetweenEvictionRunsMillis = connectionPoolInfoBuilder.getTimeBetweenEvictionRunsMillis();
                if (timeBetweenEvictionRunsMillis != null && timeBetweenEvictionRunsMillis.equals(HSQL_TIME_BETWEEN_EVICTION_RUNS_MILLIS))
                {
                    connectionPoolInfoBuilder.setTimeBetweenEvictionRunsMillis(null);
                }
            }
        }
        if (connectionPoolInfoBuilder.getPoolMaxSize() == null)
        {
            connectionPoolInfoBuilder.setPoolMaxSize(DEFAULT_POOL_MAX_SIZE);
        }
        final Long maxWait = connectionPoolInfoBuilder.getPoolMaxWait();
        if (maxWait != null && maxWait.equals(DEFAULT_POOL_MAX_WAIT))
        {
            connectionPoolInfoBuilder.setPoolMaxWait(null);
        }
        final Integer poolMaxIdle = connectionPoolInfoBuilder.getPoolMaxIdle();
        if (poolMaxIdle != null && poolMaxIdle.equals(connectionPoolInfoBuilder.getPoolMaxSize()))
        {
            connectionPoolInfoBuilder.setPoolMaxIdle(null);
        }
        final Integer poolMinSize = connectionPoolInfoBuilder.getPoolMinSize();
        if (poolMinSize != null && poolMinSize.equals(connectionPoolInfoBuilder.getPoolMaxSize()))
        {
            connectionPoolInfoBuilder.setPoolMinSize(null);
        }
        final Boolean removeAbandoned = connectionPoolInfoBuilder.getRemoveAbandoned();
        if (removeAbandoned != null && removeAbandoned == Boolean.TRUE)
        {
            connectionPoolInfoBuilder.setRemoveAbandoned(null);
        }
        final Integer removeAbandonedTimeout = connectionPoolInfoBuilder.getRemoveAbandonedTimeout();
        if (removeAbandonedTimeout != null && removeAbandonedTimeout.equals(DEFAULT_REMOVE_ABANDONED_TIMOUT))
        {
            connectionPoolInfoBuilder.setRemoveAbandonedTimeout(null);
        }
    }

    /**
     * Changes unset advanced connection pool settings to values that
     * are appropriate for the selected database type.  Implicitly calls
     * <code>{@link #initDatabaseType(boolean) initDatabaseType(false)}</code>
     * to ensure that the database type is set consistently before setting
     * the values.
     *
     * @throws IllegalArgumentException as for {@link #initDatabaseType(boolean)}
     */
    public void applyDefaultAdvancedSettings()
    {
        switch (initDatabaseType(false))
        {
            case MY_SQL:
            {
                if (connectionPoolInfoBuilder.getValidationQuery() == null)
                {
                    connectionPoolInfoBuilder.setValidationQuery(MYSQL_VALIDATION_QUERY);
                }
                if (connectionPoolInfoBuilder.getValidationQueryTimeout() == null)
                {
                    connectionPoolInfoBuilder.setValidationQueryTimeout(MYSQL_VALIDATION_QUERY_TIMEOUT);
                }
                if (connectionPoolInfoBuilder.getTestWhileIdle() == null)
                {
                    connectionPoolInfoBuilder.setTestWhileIdle(true);
                }
                if (connectionPoolInfoBuilder.getMinEvictableTimeMillis() == null)
                {
                    connectionPoolInfoBuilder.setMinEvictableTimeMillis(MYSQL_MIN_EVICTABLE_TIME_MILLIS);
                }
                if (connectionPoolInfoBuilder.getTimeBetweenEvictionRunsMillis() == null)
                {
                    connectionPoolInfoBuilder.setTimeBetweenEvictionRunsMillis(MYSQL_TIME_BETWEEN_EVICTION_RUNS_MILLIS);
                }
                break;
            }
            case HSQL:
            {
                if (connectionPoolInfoBuilder.getMinEvictableTimeMillis() == null)
                {
                    connectionPoolInfoBuilder.setMinEvictableTimeMillis(HSQL_MIN_EVICTABLE_TIME_MILLIS);
                }
                if (connectionPoolInfoBuilder.getTimeBetweenEvictionRunsMillis() == null)
                {
                    connectionPoolInfoBuilder.setTimeBetweenEvictionRunsMillis(HSQL_TIME_BETWEEN_EVICTION_RUNS_MILLIS);
                }
                break;
            }
        }
        if (connectionPoolInfoBuilder.getPoolMaxSize() == null)
        {
            connectionPoolInfoBuilder.setPoolMaxSize(DEFAULT_POOL_MAX_SIZE);
        }
        if (connectionPoolInfoBuilder.getPoolMaxWait() == null)
        {
            connectionPoolInfoBuilder.setPoolMaxWait(DEFAULT_POOL_MAX_WAIT);
        }
        if (connectionPoolInfoBuilder.getPoolMaxIdle() == null)
        {
            connectionPoolInfoBuilder.setPoolMaxIdle(connectionPoolInfoBuilder.getPoolMaxSize());
        }
        if (connectionPoolInfoBuilder.getPoolMinSize() == null)
        {
            connectionPoolInfoBuilder.setPoolMinSize(connectionPoolInfoBuilder.getPoolMaxSize());
        }
        if (connectionPoolInfoBuilder.getRemoveAbandoned() == null)
        {
            connectionPoolInfoBuilder.setRemoveAbandoned(true);
        }
        if (connectionPoolInfoBuilder.getRemoveAbandonedTimeout() == null)
        {
            connectionPoolInfoBuilder.setRemoveAbandonedTimeout(DEFAULT_REMOVE_ABANDONED_TIMOUT);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final Settings settings = (Settings)o;
        return equals(controlPort, settings.controlPort) &&
                equals(httpPort, settings.httpPort) &&
                equals(jiraHome, settings.jiraHome) &&
                equals(schemaName, settings.schemaName) &&
                equals(jdbcDatasourceBuilder, settings.jdbcDatasourceBuilder) &&
                equals(connectionPoolInfoBuilder, settings.connectionPoolInfoBuilder) &&
                equals(sslSettings, settings.sslSettings);
    }

    private static <T> boolean equals(T o1, T o2)
    {
        return (o1 != null) ? o1.equals(o2) : (o2 == null);
    }

    private static ConnectionPoolInfo.Builder newConnectionPoolInfoBuilder()
    {
        return ConnectionPoolInfo.builder().setPoolMaxSize(DEFAULT_POOL_MAX_SIZE);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
