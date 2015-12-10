package com.atlassian.jira.config.database;

import com.atlassian.config.bootstrap.AtlassianBootstrapManager;
import com.atlassian.config.bootstrap.BootstrapException;
import com.atlassian.config.db.DatabaseDetails;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.util.KeyValuePair;
import com.atlassian.jira.util.KeyValueParser;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.ofbiz.core.entity.config.ConnectionPoolInfo;
import org.ofbiz.core.entity.config.DatasourceInfo;
import org.ofbiz.core.entity.config.JdbcDatasourceInfo;

import java.sql.Connection;
import java.util.Properties;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * A JDBC datasource.  <b>Note</b>: The direct constructors for this
 * class are all <em>deprecated</em>.  Use the {@link Builder} instead.
 * Several getters are also <em>deprecated</em> because the same data
 * can be obtained indirectly through {@link #getConnectionPoolInfo()}.
 */
public final class JdbcDatasource implements Datasource
{
    private static final Logger log = Logger.getLogger(DatabaseConfigHandler.class);

    // XML element names for general JDBC settings
    static final String JDBC_DATASOURCE = "jdbc-datasource";

    private static final String URL = "url";
    private static final String DRIVER_CLASS = "driver-class";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CONNECTION_PROPERTIES = "connection-properties";

    // XML element names for connection pool settings
    private static final String POOL_SIZE = "pool-size";  // Deprecated; same meaning as pool-max-size
    private static final String POOL_MAX_SIZE = "pool-max-size";  // Replaces/overrides pool-size
    private static final String POOL_MIN_SIZE = "pool-min-size";
    private static final String POOL_MAX_WAIT = "pool-max-wait";
    private static final String POOL_SLEEP_TIME = "pool-sleep-time";
    private static final String POOL_LIFE_TIME = "pool-life-time";
    private static final String DEADLOCK_MAX_WAIT = "deadlock-max-wait";
    private static final String DEADLOCK_RETRY_WAIT = "deadlock-retry-wait";
    private static final String VALIDATION_QUERY = "validation-query";
    private static final String MIN_EVICTABLE_IDLE_TIME_MILLIS = "min-evictable-idle-time-millis";
    private static final String TIME_BETWEEN_EVICTION_RUNS_MILLIS = "time-between-eviction-runs-millis";

    // XML element names for advanced pool settings
    private static final String DEFAULT_CATALOG = "default-catalog";
    private static final String MAX_OPEN_PREPARED_STATEMENTS = "max-open-prepared-statements";
    private static final String POOL_INITIAL_SIZE = "pool-initial-size";
    private static final String POOL_MAX_IDLE = "pool-max-idle";
    private static final String POOL_NUM_TESTS_PER_EVICTION_RUN = "pool-num-tests-per-eviction-run";
    private static final String POOL_PREPARED_STATEMENTS = "pool-prepared-statements";
    private static final String POOL_REMOVE_ABANDONED = "pool-remove-abandoned";
    private static final String POOL_REMOVE_ABANDONED_TIMEOUT = "pool-remove-abandoned-timeout";
    private static final String POOL_TEST_ON_BORROW = "pool-test-on-borrow";
    private static final String POOL_TEST_ON_RETURN = "pool-test-on-return";
    private static final String POOL_TEST_WHILE_IDLE = "pool-test-while-idle";
    private static final String VALIDATION_QUERY_TIMEOUT = "validation-query-timeout";

    private final String jdbcUrl;
    private final String driverClassName;
    private final String username;
    private final String password;
    private final Properties connectionProperties;
    private final ConnectionPoolInfo poolInfo;

    private static boolean registerDriverOnConstruct = true;

    /**
     * This setting is soley so the config tool can avoid the fatal side-effect of registering the JDBC driver in this
     * class's constructor.
     *
     * @param registerDriverOnConstruct If true we do "Class.forName(driverClassName);" in the Constructor.
     */
    public static void setRegisterDriverOnConstruct(boolean registerDriverOnConstruct)
    {
        JdbcDatasource.registerDriverOnConstruct = registerDriverOnConstruct;
    }

    public static void registerDriver(String className)
    {
        DatabaseDriverRegisterer.forDriverClass(className).registerDriver();
    }


    /**
     * Factory method to create a new {@link Builder} for constructing a
     * <tt>JdbcDatasource</tt> using a chain of named setters rather than a
     * long list of parameters in a specific order.
     * @return a new {@link Builder}
     */
    public static Builder builder()
    {
        return new Builder();
    }

    @Deprecated
    public JdbcDatasource(String jdbcUrl, String driverClassName, String username, String password,
            int poolMaxSize, String validationQuery, Long minEvictableTimeMillis, Long timeBetweenEvictionRunsMillis)
    {
        this(builder(username, password, poolMaxSize, validationQuery, minEvictableTimeMillis, timeBetweenEvictionRunsMillis)
                .setJdbcUrl(jdbcUrl)
                .setDriverClassName(driverClassName));
    }

    @Deprecated
    public JdbcDatasource(String jdbcUrl, String driverClassName, String username, String password, Properties connectionProperties,
            int poolMaxSize, String validationQuery, Long minEvictableTimeMillis, Long timeBetweenEvictionRunsMillis)
    {
        this(builder(username, password, poolMaxSize, validationQuery, minEvictableTimeMillis, timeBetweenEvictionRunsMillis)
                .setJdbcUrl(jdbcUrl)
                .setDriverClassName(driverClassName)
                .setConnectionProperties(connectionProperties));
    }

    @Deprecated
    public JdbcDatasource(DatabaseType databaseType, String hostname, String port, String instance,
            String username, String password, Integer poolMaxSize, String validationQuery,
            Long minEvictableTimeMillis, Long timeBetweenEvictionRunsMillis)
    {
        this(builder(username, password, poolMaxSize, validationQuery, minEvictableTimeMillis, timeBetweenEvictionRunsMillis)
                .setDatabaseType(databaseType)
                .setHostname(hostname)
                .setPort(port)
                .setInstance(instance));
    }

    public JdbcDatasource(JdbcDatasourceInfo jdbc)
    {
        this(builder()
                .setJdbcUrl(jdbc.getUri())
                .setDriverClassName(jdbc.getDriverClassName())
                .setUsername(jdbc.getUsername())
                .setPassword(jdbc.getPassword())
                .setConnectionProperties(jdbc.getConnectionProperties())
                .setConnectionPoolInfo(jdbc.getConnectionPoolInfo()) );
    }

    private JdbcDatasource(Builder builder)
    {
        final DatabaseType databaseType = builder.getDatabaseType();
        if (builder.getJdbcUrl() == null)
        {
            if (databaseType == null)
            {
                throw new IllegalArgumentException("Must set either the JDBC URL or the database type");
            }
            try
            {
                this.jdbcUrl = databaseType.getJdbcUrlParser().getUrl(builder.getHostname(), builder.getPort(), builder.getInstance());
                this.driverClassName = databaseType.getJdbcDriverClassName();
            }
            catch (ParseException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
        else
        {
            this.jdbcUrl = builder.getJdbcUrl();
            this.driverClassName = builder.getDriverClassName();
        }

        Assertions.notBlank("JDBC URL", jdbcUrl);
        Assertions.notBlank("JDBC Driver Class Name", driverClassName);
        this.username = Assertions.notBlank("username", builder.getUsername());
        this.password = Assertions.notNull("password", builder.getPassword());

        final Properties properties = builder.getConnectionProperties();
        this.connectionProperties = (properties != null) ? (Properties)properties.clone() : null;
        this.poolInfo = Assertions.notNull("connectionPoolInfo", builder.getConnectionPoolInfo());
        Assertions.not("poolInfo.getMaxSize() <= 0", poolInfo.getMaxSize() <= 0);

        // The config tool does not run in Tomcat, so it does not have the drivers on it's default classpath.
        // Give it a chance to avoid the fatal side-effect of registering the driver in this class's constructor.
        if (registerDriverOnConstruct)
        {
            registerDriver();
        }
    }



    // helper for the old constructors
    private static Builder builder(
            String username, String password, Integer poolMaxSize, String validationQuery,
            Long minEvictableTimeMillis, Long timeBetweenEvictionRunsMillis)
    {
        final ConnectionPoolInfo connectionPoolInfo = ConnectionPoolInfo.builder()
                .setPoolMaxSize(poolMaxSize)
                .setValidationQuery(validationQuery)
                .setMinEvictableTimeMillis(minEvictableTimeMillis)
                .setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis)
                .build();
        return new Builder()
                .setUsername(username)
                .setPassword(password)
                .setConnectionPoolInfo(connectionPoolInfo);
    }


    public Builder toBuilder()
    {
        return new Builder()
                .setJdbcUrl(jdbcUrl)
                .setDriverClassName(driverClassName)
                .setUsername(username)
                .setPassword(password)
                .setConnectionPoolInfo(poolInfo);
    }



    private void registerDriver()
    {
        registerDriver(driverClassName);
    }

    @Override
    public Connection getConnection(AtlassianBootstrapManager bootstrapManager) throws BootstrapException
    {
        return bootstrapManager.getTestDatabaseConnection(createDbDetails());
    }

    @Override
    public DatasourceInfo getDatasource(String datasourceName, String databaseType, String schemaName)
    {
        JdbcDatasourceInfo jdbcInfo = new JdbcDatasourceInfo(jdbcUrl, driverClassName, username, password,
                null /* isolationLevel */, connectionProperties, poolInfo);
        return new DatasourceInfo(datasourceName, databaseType, schemaName, jdbcInfo);
    }

    @Override
    public String getDescriptorValue(String databaseType)
    {
        return databaseType + " " + jdbcUrl;
    }

    @Override
    public String getDescriptorLabel()
    {
        return "Database JDBC config";
    }

    /**
     * The JDBC URL
     *
     * @return The JDBC URL
     */
    public String getJdbcUrl()
    {
        return jdbcUrl;
    }

    /**
     * The class name for the driver
     *
     * @return The class name.  May or may not be valid.
     */
    public String getDriverClassName()
    {
        return driverClassName;
    }

    /**
     * The username for the database connection
     *
     * @return The username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * The password for the database connection
     *
     * @return The password
     */
    public String getPassword()
    {
        return password;
    }

    public Properties getConnectionProperties()
    {
        return (connectionProperties != null) ? (Properties)connectionProperties.clone() : null;
    }

    public ConnectionPoolInfo getConnectionPoolInfo()
    {
        return poolInfo;
    }

    @Deprecated
    public int getPoolSize()
    {
        return poolInfo.getMaxSize();
    }

    @Deprecated
    public String getValidationQuery()
    {
        return poolInfo.getValidationQuery();
    }

    @Deprecated
    public Long getMinEvictableTimeMillis()
    {
        return poolInfo.getMinEvictableTimeMillis();
    }

    @Deprecated
    public Long getTimeBetweenEvictionRunsMillis()
    {
        return poolInfo.getTimeBetweenEvictionRunsMillis();
    }

    @Override
    public String toString()
    {
        return ToStringBuilderExcludingPassword.toString(this);
    }

    @Override
    public boolean equals(Object o)
    {
        return o instanceof JdbcDatasource && toBuilder().equals(((JdbcDatasource)o).toBuilder());
    }

    @Override
    public int hashCode()
    {
        return toBuilder().hashCode();
    }


    DatabaseDetails createDbDetails()
    {
        final DatabaseDetails dbDetails = new DatabaseDetails();
        dbDetails.setDatabaseUrl(jdbcUrl);
        dbDetails.setDriverClassName(driverClassName);
        dbDetails.setUserName(username);
        dbDetails.setPassword(password);
        dbDetails.setPoolSize(poolInfo.getMaxSize());
        return dbDetails;
    }

    /**
     * Parses JDBC Datasource settings that have been serialized to XML.
     *
     * @param datasourceElement the <tt>jdbc-datasource</tt> element to pase
     * @return the parsed JDBC datasource
     */
    static JdbcDatasource parse(Element datasourceElement)
    {
        if (datasourceElement == null || !datasourceElement.getName().equals(JDBC_DATASOURCE))
        {
            throw new IllegalArgumentException("Expected " + JDBC_DATASOURCE + " element");
        }

        Properties connectionProperties = parseConnectionProperties(datasourceElement.elementText(CONNECTION_PROPERTIES));
        Integer poolMaxSize = parseInteger(datasourceElement, POOL_MAX_SIZE);
        if (poolMaxSize == null)
        {
            poolMaxSize = parseInteger(datasourceElement, POOL_SIZE);
        }

        // Basic connection pool properties
        final ConnectionPoolInfo poolInfo = ConnectionPoolInfo.builder()
                // Normal settings...
                .setPoolMaxSize(poolMaxSize)
                .setPoolMinSize(parseInteger(datasourceElement, POOL_MIN_SIZE))
                .setPoolMaxWait(parseLong(datasourceElement, POOL_MAX_WAIT))
                .setPoolSleepTime(parseLong(datasourceElement, POOL_SLEEP_TIME))
                .setPoolLifeTime(parseLong(datasourceElement, POOL_LIFE_TIME))
                .setDeadLockMaxWait(parseLong(datasourceElement, DEADLOCK_MAX_WAIT))
                .setDeadLockRetryWait(parseLong(datasourceElement, DEADLOCK_RETRY_WAIT))
                .setValidationQuery(datasourceElement.elementText(VALIDATION_QUERY))
                .setMinEvictableTimeMillis(parseLong(datasourceElement, MIN_EVICTABLE_IDLE_TIME_MILLIS))
                .setTimeBetweenEvictionRunsMillis(parseLong(datasourceElement, TIME_BETWEEN_EVICTION_RUNS_MILLIS))
                // Advanced settings...
                .setDefaultCatalog(datasourceElement.elementText(DEFAULT_CATALOG))
                .setMaxOpenPreparedStatements(parseInteger(datasourceElement, MAX_OPEN_PREPARED_STATEMENTS))
                .setNumTestsPerEvictionRun(parseInteger(datasourceElement, POOL_NUM_TESTS_PER_EVICTION_RUN))
                .setPoolInitialSize(parseInteger(datasourceElement, POOL_INITIAL_SIZE))
                .setPoolMaxIdle(parseInteger(datasourceElement, POOL_MAX_IDLE))
                .setPoolPreparedStatements(parseBoolean(datasourceElement, POOL_PREPARED_STATEMENTS))
                .setRemoveAbandoned(parseBoolean(datasourceElement, POOL_REMOVE_ABANDONED))
                .setRemoveAbandonedTimeout(parseInteger(datasourceElement, POOL_REMOVE_ABANDONED_TIMEOUT))
                .setTestOnBorrow(parseBoolean(datasourceElement, POOL_TEST_ON_BORROW))
                .setTestOnReturn(parseBoolean(datasourceElement, POOL_TEST_ON_RETURN))
                .setTestWhileIdle(parseBoolean(datasourceElement, POOL_TEST_WHILE_IDLE))
                .setValidationQueryTimeout(parseInteger(datasourceElement, VALIDATION_QUERY_TIMEOUT))
                .build();

        return new Builder()
                .setJdbcUrl(datasourceElement.elementText(URL))
                .setDriverClassName(datasourceElement.elementText(DRIVER_CLASS))
                .setUsername(datasourceElement.elementText(USERNAME))
                .setPassword(datasourceElement.elementText(PASSWORD))
                .setConnectionProperties(connectionProperties)
                .setConnectionPoolInfo(poolInfo)
                .build();
    }

    private static Boolean parseBoolean(Element element, String key)
    {
        String value = element.elementText(key);
        if (isBlank(value))
        {
            return null;
        }
        if ("true".equalsIgnoreCase(value))
        {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(value))
        {
            return Boolean.FALSE;
        }
        log.warn("Invalid value for '" + key + "': '" + value + '\'');
        return null;
    }

    private static Integer parseInteger(Element element, String key)
    {
        String value = element.elementText(key);
        if (isBlank(value))
        {
            return null;
        }
        try
        {
            return Integer.parseInt(value.trim());
        }
        catch (NumberFormatException nfe)
        {
            log.warn("Invalid value for '" + key + "': '" + value + '\'');
            return null;
        }
    }

    private static Long parseLong(Element element, String key)
    {
        String value = element.elementText(key);
        if (isBlank(value))
        {
            return null;
        }
        try
        {
            return Long.parseLong(value.trim());
        }
        catch (NumberFormatException nfe)
        {
            log.warn("Invalid value for '" + key + "': '" + value + '\'');
            return null;
        }
    }


    private static Properties parseConnectionProperties(String value)
    {
        if (value == null || value.length() == 0)
        {
            return null;
        }
        // <connection-properties>portNumber=5432;defaultAutoCommit=true</connection-properties>
        // Split on the semicolon
        Properties properties = new Properties();
        final String[] keyValues = value.split(";");
        for (String keyValueText : keyValues)
        {
            final KeyValuePair<String,String> keyValuePair = KeyValueParser.parse(keyValueText);
            properties.setProperty(keyValuePair.getKey(), keyValuePair.getValue());
        }
        return properties;
    }

    private void writeValue(Element element, String key, String value)
    {
        element.addElement(key).setText(value);
    }

    private void writeValue(Element element, String key, int value)
    {
        element.addElement(key).setText(String.valueOf(value));
    }

    private void writeValue(Element element, String key, long value)
    {
        element.addElement(key).setText(Long.toString(value));
    }

    private void writeOptionalValue(Element element, String key, long value, long defaultValue)
    {
        if (value != defaultValue)
        {
            writeValue(element, key, value);
        }
    }

    private void writeOptionalValue(Element element, String key, Object value)
    {
        if (value != null)
        {
            element.addElement(key).setText(value.toString());
        }
    }


    /**
     * Serializes this JDBC to an XML element.  This creates a child element
     * called <tt>jdbc-datasource</tt> which contains the settings.
     *
     * @param element the parent element within which to write the JDBC Datasource.
     */
    public void writeTo(Element element)
    {
        final Element jdbc = element.addElement(JDBC_DATASOURCE);
        writeValue(jdbc, URL, getJdbcUrl());
        writeValue(jdbc, DRIVER_CLASS, getDriverClassName());
        writeValue(jdbc, USERNAME, getUsername());
        writeValue(jdbc, PASSWORD, getPassword());

        final ConnectionPoolInfo poolInfo = getConnectionPoolInfo();
        writeValue(jdbc, POOL_MIN_SIZE, poolInfo.getMinSize());
        writeValue(jdbc, POOL_MAX_SIZE, poolInfo.getMaxSize());

        // These fields are omitted if they are set to the default values
        writeOptionalValue(jdbc, POOL_MAX_WAIT, poolInfo.getMaxWait(), ConnectionPoolInfo.DEFAULT_POOL_MAX_WAIT);
        writeOptionalValue(jdbc, POOL_SLEEP_TIME, poolInfo.getSleepTime(), ConnectionPoolInfo.DEFAULT_POOL_SLEEP_TIME);
        writeOptionalValue(jdbc, POOL_LIFE_TIME, poolInfo.getLifeTime(), ConnectionPoolInfo.DEFAULT_POOL_LIFE_TIME);
        writeOptionalValue(jdbc, DEADLOCK_MAX_WAIT, poolInfo.getDeadLockMaxWait(), ConnectionPoolInfo.DEFAULT_DEADLOCK_MAX_WAIT);
        writeOptionalValue(jdbc, DEADLOCK_RETRY_WAIT, poolInfo.getDeadLockRetryWait(), ConnectionPoolInfo.DEFAULT_DEADLOCK_RETRY_WAIT);

        //    <validation-query>SELECT 'X' FROM DUAL</validation-query>
        writeOptionalValue(jdbc, VALIDATION_QUERY, poolInfo.getValidationQuery());
        //    <min-evictable-idle-time-millis>4000</min-evictable-idle-time-millis>
        writeOptionalValue(jdbc, MIN_EVICTABLE_IDLE_TIME_MILLIS, poolInfo.getMinEvictableTimeMillis());
        //    <time-between-eviction-runs-millis>5000</time-between-eviction-runs-millis>
        writeOptionalValue(jdbc, TIME_BETWEEN_EVICTION_RUNS_MILLIS, poolInfo.getTimeBetweenEvictionRunsMillis());

        // Advanced settings
        writeOptionalValue(jdbc, DEFAULT_CATALOG, poolInfo.getDefaultCatalog());
        writeOptionalValue(jdbc, MAX_OPEN_PREPARED_STATEMENTS, poolInfo.getMaxOpenPreparedStatements());
        writeOptionalValue(jdbc, POOL_NUM_TESTS_PER_EVICTION_RUN, poolInfo.getNumTestsPerEvictionRun());
        writeOptionalValue(jdbc, POOL_INITIAL_SIZE, poolInfo.getInitialSize());
        writeOptionalValue(jdbc, POOL_MAX_IDLE, poolInfo.getMaxIdle());
        writeOptionalValue(jdbc, POOL_PREPARED_STATEMENTS, poolInfo.getPoolPreparedStatements());
        writeOptionalValue(jdbc, POOL_REMOVE_ABANDONED, poolInfo.getRemoveAbandoned());
        writeOptionalValue(jdbc, POOL_REMOVE_ABANDONED_TIMEOUT, poolInfo.getRemoveAbandonedTimeout());
        writeOptionalValue(jdbc, POOL_TEST_ON_BORROW, poolInfo.getTestOnBorrow());
        writeOptionalValue(jdbc, POOL_TEST_ON_RETURN, poolInfo.getTestOnReturn());
        writeOptionalValue(jdbc, POOL_TEST_WHILE_IDLE, poolInfo.getTestWhileIdle());
        writeOptionalValue(jdbc, VALIDATION_QUERY_TIMEOUT, poolInfo.getValidationQueryTimeout());
    }



    /**
     * This is a builder class for constructing a <tt>JdbcDatasource</tt> manually.
     * Use the {@link JdbcDatasource#builder()} factory to obtain an instance
     * of this class and call the various <tt>setXyzzy</tt> methods to
     * populate the fields.  Those that are left unset or are explicitly
     * set to <tt>null</tt> will use their default values.
     */
    public static class Builder
    {
        private DatabaseType databaseType;
        private String hostname;
        private String port;
        private String instance;
        private String jdbcUrl;
        private String driverClassName;
        private String username;
        private String password;
        private Properties connectionProperties;
        private ConnectionPoolInfo connectionPoolInfo;

        /**
         * Returns a new <tt>JdbcDatasource</tt> as specified by the current state
         * of this builder.
         *
         * @return the new <tt>JdbcDatasource</tt>
         * @throws IllegalArgumentException if the configuration is invalid, such as
         *      if the username is left unspecified or if neither the JDBC URL nor
         *      a database type is given.
         * @throws InvalidDatabaseDriverException if the database driver is invalid
         */
        public JdbcDatasource build()
        {
            return new JdbcDatasource(this);
        }

        public DatabaseType getDatabaseType()
        {
            return databaseType;
        }

        /** Sets the database type.  This value, the hostname, the port, and
         * the instance are ignored if the {@link #setJdbcUrl(String) JDBC URL}
         * is explicitly set.  One or the other means of setting the URL is
         * required.
         *
         * @param databaseType the database type for the JDBC URL
         * @return <tt>this</tt> builder
         * @see #setJdbcUrl(String)
         */
        public Builder setDatabaseType(DatabaseType databaseType)
        {
            this.databaseType = databaseType;
            return this;
        }

        public String getHostname()
        {
            return hostname;
        }

        /** Sets the hostname for the JDBC URL.
         *
         * @param hostname the hostname for the JDBC URL
         * @return <tt>this</tt> builder
         * @see #setDatabaseType(DatabaseType)
         * @see #setJdbcUrl(String)
         */
        public Builder setHostname(String hostname)
        {
            this.hostname = hostname;
            return this;
        }

        public String getPort()
        {
            return port;
        }

        /** Sets the port for the JDBC URL.
         *
         * @param port the port for the JDBC URL
         * @return <tt>this</tt> builder
         * @see #setDatabaseType(DatabaseType)
         * @see #setJdbcUrl(String)
         */
        public Builder setPort(String port)
        {
            this.port = port;
            return this;
        }

        public String getInstance()
        {
            return instance;
        }

        /** Sets the instance for the JDBC URL.
         *
         * @param instance the instance for the JDBC URL
         * @return <tt>this</tt> builder
         * @see #setDatabaseType(DatabaseType)
         * @see #setJdbcUrl(String)
         */
        public Builder setInstance(String instance)
        {
            this.instance = instance;
            return this;
        }

        public String getJdbcUrl()
        {
            return jdbcUrl;
        }

        /** Sets an explicit value for the JDBC URL.  If this value is not
         * set, then the database type, hostname, port, and instance must be
         * set, instead.  If this is set, then those other values are ignored,
         * and the {@link #setDriverClassName(String) driver class name} will
         * be required.
         *
         * @param jdbcUrl the complete JDBC URL to use
         * @return <tt>this</tt> builder
         * @see #setDatabaseType(DatabaseType)
         * @see #setDriverClassName(String)
         */
        public Builder setJdbcUrl(String jdbcUrl)
        {
            this.jdbcUrl = jdbcUrl;
            return this;
        }

        public String getDriverClassName()
        {
            return driverClassName;
        }

        /**
         * The driver class name is required when the {@link #setJdbcUrl(String) JDBC URL}
         * is explicitly set.  Otherwise, it is ignored.
         *
         * @param driverClassName the class name of the JDBC driver to use
         * @return <tt>this</tt> builder
         */
        public Builder setDriverClassName(String driverClassName)
        {
            this.driverClassName = driverClassName;
            return this;
        }

        public String getUsername()
        {
            return username;
        }

        public Builder setUsername(String username)
        {
            this.username = username;
            return this;
        }

        public String getPassword()
        {
            return password;
        }

        public Builder setPassword(String password)
        {
            this.password = password;
            return this;
        }

        public Properties getConnectionProperties()
        {
            return connectionProperties;
        }

        public Builder setConnectionProperties(Properties connectionProperties)
        {
            this.connectionProperties = connectionProperties;
            return this;
        }

        public ConnectionPoolInfo getConnectionPoolInfo()
        {
            return connectionPoolInfo;
        }

        public Builder setConnectionPoolInfo(ConnectionPoolInfo connectionPoolInfo)
        {
            this.connectionPoolInfo = connectionPoolInfo;
            return this;
        }


        @Override
        public boolean equals(Object o)
        {
            if (! (o instanceof JdbcDatasource.Builder))
            {
                return false;
            }
            final JdbcDatasource.Builder other = (JdbcDatasource.Builder)o;
            return equals(databaseType, other.databaseType) &&
                    equals(hostname, other.hostname) &&
                    equals(port, other.port) &&
                    equals(instance, other.instance) &&
                    equals(jdbcUrl, other.jdbcUrl) &&
                    equals(driverClassName, other.driverClassName) &&
                    equals(username, other.username) &&
                    equals(password, other.password) &&
                    equals(connectionProperties, other.connectionProperties) &&
                    equals(connectionPoolInfo, other.connectionPoolInfo);
        }

        @Override
        public int hashCode()
        {
            int h = 0x78695A4B;
            h = hash(h, databaseType);
            h = hash(h, hostname);
            h = hash(h, port);
            h = hash(h, instance);
            h = hash(h, jdbcUrl);
            h = hash(h, driverClassName);
            h = hash(h, username);
            h = hash(h, password);
            h = hash(h, connectionProperties);
            h = hash(h, connectionPoolInfo);
            return h;
        }

        private static int hash(final int h, final Object o)
        {
            return h * 17 + ((o != null) ? o.hashCode() : 0);
        }

        private static <T> boolean equals(T o1, T o2)
        {
            return (o1 != null) ? o1.equals(o2) : (o2 == null);
        }

        @Override
        public String toString()
        {
            return ToStringBuilderExcludingPassword.reflectionToString(this);
        }
    }
}
