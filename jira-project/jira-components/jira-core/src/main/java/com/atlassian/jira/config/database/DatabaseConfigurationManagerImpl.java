package com.atlassian.jira.config.database;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.util.concurrent.LazyReference;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.config.ConnectionPoolInfo;
import org.ofbiz.core.entity.config.DatasourceInfo;
import org.ofbiz.core.entity.config.EntityConfigUtil;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A threadsafe implementation which reads and writes configuration for the database using the given
 * {@link DatabaseConfigurationLoader}. Caches configuration.
 */
public class DatabaseConfigurationManagerImpl implements DatabaseConfigurationManager
{
    private static final Logger log = Logger.getLogger(DatabaseConfigurationManagerImpl.class);
    private static final int DEFAULT_POOL_SIZE = 20;
    private static final long DEFAULT_POOL_MAX_WAIT = 30000L;
    private static final String ACTIVATED = "post-database-activated";
    private static final String CONFIGURED_PRE_ACTIVATED = "post-database-configured-but-pre-database-activated";

    /**
     * Main lock for mutating database configuration.
     */
    @ClusterSafe("The database configuration is in the local home directory")
    private final Lock setupLock = new ReentrantLock();

    /**
     * Things to do after database is activated.
     */
    private final Queue<Runnable> postDbSetupQueue = new LinkedBlockingQueue<Runnable>();

    /**
     * Things to do after database is configured, but before it is activated.
     */
    private final Queue<Runnable> postDbConfiguredQueue = new LinkedBlockingQueue<Runnable>();

    /**
     * Cache of the actual configuration.
     */
    private final AtomicReference<DatabaseConfig> configCache = new AtomicReference<DatabaseConfig>();

    /**
     * A negative cache! If we know for sure that we are not setup, then this will be false. Otherwise it will be true.
     * Therefore, if we might be but we don't know then it will definitely be true.
     */
    private final AtomicBoolean possiblySetup = new AtomicBoolean(true);

    /**
     * We still need a flag to indicate definite set up
     */
    private final AtomicBoolean definitelySetup = new AtomicBoolean(false);

    private final DatabaseConfigurationLoader databaseConfigLoader;
    private final ComponentLocator componentLocator;

    /**
     * The DatabaseConfig that represents using JIRA with its internal database.
     */
    @ClusterSafe
    private final LazyReference<DatabaseConfig> internalConfig;

    /**
     * Stores whether or not the EntityConfigUtil has been set up with the delegator and datasourceinfo
     */
    private volatile boolean ofBizConfigured = false;
    private static final String DATASOURCE_NAME = "defaultDS";
    private static final String DELEGATOR_NAME = "default";

    public DatabaseConfigurationManagerImpl(final JiraHome jiraHome,
            final DatabaseConfigurationLoader databaseConfigLoader, final ComponentLocator componentLocator)
    {
        this.databaseConfigLoader = databaseConfigLoader;
        this.componentLocator = componentLocator;
        // only need jiraHome if we're actually going to use the internal database configuration.
        internalConfig = new LazyReference<DatabaseConfig>()
        {
            @Override
            protected DatabaseConfig create() throws Exception
            {
                return createInternalConfig(jiraHome);
            }
        };
    }

    private DatabaseConfig createInternalConfig(JiraHome jiraHome)
    {
        final String jdbcUrl = "jdbc:hsqldb:" + jiraHome.getHomePath() + "/database/jiradb";
        final String driver = "org.hsqldb.jdbcDriver";
        ConnectionPoolInfo.Builder connectionPoolInfoBuilder = ConnectionPoolInfo.builder();
        connectionPoolInfoBuilder
                .setPoolMaxSize(DEFAULT_POOL_SIZE)
                .setPoolMinSize(DEFAULT_POOL_SIZE)
                .setPoolMaxWait(DEFAULT_POOL_MAX_WAIT)
                .setRemoveAbandoned(true)
                .setRemoveAbandonedTimeout(300)
                .setMinEvictableTimeMillis(4000L)
                .setTimeBetweenEvictionRunsMillis(5000L);

        JdbcDatasource.Builder builder = JdbcDatasource.builder()
                .setJdbcUrl(jdbcUrl)
                .setDriverClassName(driver)
                .setDatabaseType(DatabaseType.HSQL)
                .setUsername("sa")
                .setPassword("")
                .setConnectionPoolInfo(connectionPoolInfoBuilder.build());

        Datasource datasource = builder.build();
        return new DatabaseConfig("hsql", "PUBLIC", datasource);
    }

    @Override
    public void setDatabaseConfiguration(DatabaseConfig databaseConfig)
    {
        setupLock.lock();
        try
        {
            databaseConfigLoader.saveDatabaseConfiguration(databaseConfig);
            configCache.set(databaseConfig);
            // it is not yet definitely set up but it is definitely possibly set up
            possiblySetup.set(true);
        }
        finally
        {
            setupLock.unlock();
        }
    }

    @Override
    public void createDbConfigFromEntityDefinition()
    {
        if (!databaseConfigLoader.configExists())
        {
            DatasourceInfo info = EntityConfigUtil.getInstance().getDatasourceInfo(DATASOURCE_NAME);
            if (info != null)
            {
                Datasource datasource = null;
                if (info.getJndiDatasource() != null)
                {
                    datasource = new JndiDatasource(info.getJndiDatasource());
                }
                else if (info.getJdbcDatasource() != null)
                {
                    datasource = new JdbcDatasource(info.getJdbcDatasource());
                }
                else
                {
                    log.warn("The datasource defined in entityengine.xml is not a valid JNDI or JDBC datasource.");
                }
                // perform migration if datasource is setup in entityengine.xml file
                // AND dbconfig.xml file is not in the home dir
                if (datasource != null)
                {
                    log.info("Migrating from entityengine.xml specification of datasource to new dbconfig.xml form");
                    DatabaseConfig config = new DatabaseConfig(DATASOURCE_NAME, DELEGATOR_NAME,
                            info.getFieldTypeName(), info.getSchemaName(), datasource);
                    databaseConfigLoader.saveDatabaseConfiguration(config);
                    setDatabaseConfiguration(config);
                }
            }
            else
            {
                log.warn("no " + DATASOURCE_NAME + " datasource");
            }
        }
    }

    @Override
    public DatabaseConfig getDatabaseConfiguration()
    {
        setupLock.lock();
        try
        {
            DatabaseConfig dbConfig = configCache.get();
            if (dbConfig == null)
            {
                try
                {
                    dbConfig = databaseConfigLoader.loadDatabaseConfiguration();
                    configCache.set(dbConfig);
                }
                catch (RuntimeException e)
                {
                    possiblySetup.set(false);
                    throw e;
                }
            }
            definitelySetup.set(true);
            return dbConfig;
        }
        finally
        {
            setupLock.unlock();
        }
    }

    @Override
    public void doNowOrWhenDatabaseActivated(Runnable runnable, String desc)
    {
        doNowOrEnqueue(ACTIVATED, postDbSetupQueue, runnable, desc);
    }

    @Override
    public void doNowOrWhenDatabaseConfigured(Runnable runnable, String desc)
    {
        doNowOrEnqueue(CONFIGURED_PRE_ACTIVATED, postDbConfiguredQueue, runnable, desc);
    }

    void configureOfbiz(DatabaseConfig databaseConfig)
    {
        log.debug("Configuring Ofbiz");
        // Add the datasource and delegator
        final DatasourceInfo datasourceInfo = databaseConfig.getDatasourceInfo();
        EntityConfigUtil.getInstance().addDatasourceInfo(datasourceInfo);
        final String delegatorName = databaseConfig.getDelegatorName();
        log.debug("delegator name is " + delegatorName);
        EntityConfigUtil.DelegatorInfo delegatorInfo = new EntityConfigUtil.DelegatorInfo(delegatorName,
                "main", "main", MapBuilder.singletonMap("default", databaseConfig.getDatasourceName()));
        EntityConfigUtil.getInstance().addDelegatorInfo(delegatorInfo);

        log.debug("Unlocking GenericDelegator");
        GenericDelegator.unlock();
        CoreFactory.getGenericDelegator().initialiseAndCheckDatabase();

        ofBizConfigured = true;
    }

    public boolean isDatabaseSetup()
    {
        setupLock.lock();
        try
        {
            if (!possiblySetup.get())
            {
                return false;
            }
            getDatabaseConfiguration();
            return (definitelySetup.get());
        }
        catch (RuntimeException e)
        {
            log.debug("database is not setup");
            return false;
        }
        finally
        {
            setupLock.unlock();
        }
    }

    @Override
    public void activateDatabase()
    {
        log.debug("activating database");
        setupLock.lock();
        try
        {
            final DatabaseConfig databaseConfiguration = getDatabaseConfiguration();

            // set it into entityengine's static configuration
            configureOfbiz(databaseConfiguration);
        }
        finally
        {
            setupLock.unlock();
        }

        for (Runnable runnable : postDbConfiguredQueue)
        {
            runnable.run();
        }

        setupLock.lock();
        try
        {
            final PropertiesManager propertiesManager = componentLocator.getComponent(PropertiesManager.class);
            // let ApplicationProperties know it can start using the database backing now.
            propertiesManager.onDatabaseConfigured();
            // we are now definitely setup. It was probably already set to true.
            definitelySetup.set(true);
        }
        finally
        {
            setupLock.unlock();
        }

        for (Runnable runnable : postDbSetupQueue)
        {
            runnable.run();
        }
    }

    @Override
    public DatabaseConfig getInternalDatabaseConfiguration()
    {
        return internalConfig.get();
    }

    void doNowOrEnqueue(String queueDesc, Queue<Runnable> queue, Runnable runnable, String desc)
    {
        Assertions.notNull("runnable", runnable);
        Assertions.notBlank("desc", desc);
        if (isDatabaseSetup())
        {
            if (!ofBizConfigured)
            {
                configureOfbiz(getDatabaseConfiguration());
            }
            log.info("Now running " + desc + "");
            runnable.run();
        }
        else
        {
            log.info("The database is not yet configured");
            log.info("Enqueuing " + desc + " on " + queueDesc + " queue");
            queue.add(runnable);
        }
    }
}
