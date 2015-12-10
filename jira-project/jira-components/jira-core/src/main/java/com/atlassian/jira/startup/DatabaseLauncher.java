package com.atlassian.jira.startup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.ComponentReference;
import com.atlassian.jira.config.database.DatabaseConfig;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.database.DatabaseUtil;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.ConnectionKeeper;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericHelper;
import org.ofbiz.core.entity.config.DatasourceInfo;
import org.ofbiz.core.entity.config.EntityConfigUtil;
import org.ofbiz.core.entity.model.ModelEntity;

/**
 * Configures the JIRA database by configuring transactions and setting up HSQL connection hacks.
 *
 * @since v4.4
 */
public class DatabaseLauncher implements JiraLauncher
{
    private static final Logger log = Logger.getLogger(DatabaseLauncher.class);

    private static final String HSQLDB = "hsql";
    private static final int CK_CONNECTIONS = 1;
    private static final int CK_SLEEPTIME = 300000;
    private static final String TRANSACTION_ISOLATION_PROPERTY = "jira.transaction.isolation";
    private static final String TRANSACTION_DISABLE_PROPERTY = "jira.transaction.disable";
    private final JiraProperties jiraSystemProperties;

    private volatile ConnectionKeeper connectionKeeper;
    private final ComponentReference<DatabaseConfigurationManager> configManagerRef =
            ComponentAccessor.getComponentReference(DatabaseConfigurationManager.class);

    public DatabaseLauncher(final JiraProperties jiraSystemProperties)
    {
        this.jiraSystemProperties = jiraSystemProperties;
    }


    @Override
    public void start()
    {
        DatabaseConfig databaseConfig = configManagerRef.get().getDatabaseConfiguration();
        if (databaseConfig == null)
        {
            log.fatal("No database config found");
            return;
        }
        // Add the datasource and delegator

        final DatasourceInfo datasourceInfo = databaseConfig.getDatasourceInfo();
        if (datasourceInfo == null)
        {
            log.fatal("No datasource info found");
            return;
        }

        setupHsqlHacks(datasourceInfo);
        initDatabaseTransactions(datasourceInfo);
        // JRADEV-23357 clean up table name case sensitivity problems
        cleanupDatabaseTableNames();
        new JiraStartupLogger().printStartingMessageDatabaseOK();
    }

    @Override
    public void stop()
    {
        shutdownHsqlHacks();

        final DatabaseConfig config = configManagerRef.get().getDatabaseConfiguration();
        if (config != null)
        {
            // shutting down even though database was not yet set up
            String name = config.getDatasourceName();
            final EntityConfigUtil entityConfigUtil = EntityConfigUtil.getInstance();
            // check if delegator was ever configured
            if (entityConfigUtil.getDelegatorInfo(name) != null)
            {
                entityConfigUtil.removeDelegator(name);
            }
            // check if datasource was ever configured
            if (entityConfigUtil.getDatasourceInfo(name) != null)
            {
                entityConfigUtil.removeDatasource(name);
            }
        }
    }

    /**
     * Sets up hacks to ensure HSQL connections stay alive
     *
     * @param datasourceInfo The datasource info
     */
    private void setupHsqlHacks(DatasourceInfo datasourceInfo)
    {
        if (datasourceInfo != null)
        {
            if (HSQLDB.equals(datasourceInfo.getFieldTypeName()))
            {
                final String message1 = "hsqldb is an in-memory database, and susceptible to corruption when abnormally terminated.";
                final String message2 = "DO NOT USE IN PRODUCTION, please switch to a regular database.";
                final String line = StringUtils.repeat("*", message1.length());
                final String newLine = jiraSystemProperties.getProperty("line.separator");
                log.warn(newLine + newLine + line + newLine + message1 + newLine + message2 + newLine + line + newLine);
                if (log.isDebugEnabled())
                {
                    log.debug("Will open " + CK_CONNECTIONS + " connections to keep the database alive.");
                    log.debug("Starting ConnectionKeeper with datasource name '" + datasourceInfo.getName() +
                            "', connections to open '" + CK_CONNECTIONS + "' and sleep time '" + CK_SLEEPTIME + "' milliseconds.");
                }
                connectionKeeper = new ConnectionKeeper(datasourceInfo.getName(), CK_CONNECTIONS, CK_SLEEPTIME);
                connectionKeeper.start();
            }
        }
        else
        {
            log.info("Cannot get datasource information from server. Probably using JBoss. Please ensure that you are not using " + HSQLDB + " also.");
        }
    }

    private void shutdownHsqlHacks()
    {
        if (connectionKeeper != null)
        {
            connectionKeeper.shutdown();
        }
    }

    private void initDatabaseTransactions(final DatasourceInfo datasourceInfo)
    {
        boolean startTransaction = true;
        Integer isolationLevel = null;

        // Test for null datasource as it is null under JBoss
        if (datasourceInfo != null)
        {
            // HSQLDB does not support any transaction isolation except for Connection.
            if (HSQLDB.equals(datasourceInfo.getFieldTypeName()))
            {
                log.info("Setting isolation level to '" + Connection.TRANSACTION_READ_UNCOMMITTED + "' as this is the only isolation level '" + HSQLDB + "' supports.");
                isolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED;
            }
        }
        else
        {
            log.info("Cannot get datasource information from server. Probably using JBoss. If using HSQLDB please set '" + TRANSACTION_ISOLATION_PROPERTY + "' to '1'. Other databases should not need this property.");
        }

        try
        {
            if (jiraSystemProperties.getBoolean(TRANSACTION_DISABLE_PROPERTY))
            {
                log.info("System property + '" + TRANSACTION_DISABLE_PROPERTY + "' set to true.");
                startTransaction = false;
            }

            final String isolationProperty = jiraSystemProperties.getProperty(TRANSACTION_ISOLATION_PROPERTY);
            if (isolationProperty != null)
            {
                try
                {
                    log.info("System property + '" + TRANSACTION_ISOLATION_PROPERTY + "' set to '" + isolationProperty + "'. Overriding default.");
                    isolationLevel = Integer.valueOf(isolationProperty);
                }
                catch (final NumberFormatException e)
                {
                    log.error("The '" + TRANSACTION_ISOLATION_PROPERTY + "' is set to a non-numeric value '" + isolationProperty + "'.");
                }
            }
        }
        catch (final SecurityException e)
        {
            log.warn(
                    "There was a security problem trying to read transaction configuration system properties. This usually occurs if you are " + "running JIRA with a security manager. As these system properties are not required to be set (unless you are trying to solve another problem) " + "JIRA should function properly.",
                    e);
        }

        log.info("Database transactions enabled: " + startTransaction);
        CoreTransactionUtil.setUseTransactions(startTransaction);

        if (isolationLevel != null)
        {
            log.info("Database transaction isolation level: " + isolationLevel);
            CoreTransactionUtil.setIsolationLevel(isolationLevel);
        }
        else
        {
            log.info("Using JIRA's default for database transaction isolation level: " + CoreTransactionUtil.getIsolationLevel());
        }
    }

    private void cleanupDatabaseTableNames()
    {
        // This is only a problem for people upgrading from an early 6.0.x release to 6.0.5 or higher.
        // We can eventually get rid of this method once upgrades are not supported from 6.0

        OfBizDelegator ofBizDelegator = ComponentAccessor.getOfBizDelegator();
        boolean needsTablesRecreated = false;
        // Test MovedIssueKey
        try
        {
            ofBizDelegator.findByAnd("MovedIssueKey", FieldMap.build("oldIssueKey", "bogus"));
            // Sweet - it worked
        }
        catch (DataAccessException ex)
        {
            log.warn("JRADEV-23357: unable to select from the 'MovedIssueKey' entity.");
            cleanupDatabaseTableName("MOVED_ISSUE_KEY");
            needsTablesRecreated = true;
        }
        // Test ProjectKey
        try
        {
            ofBizDelegator.findByAnd("ProjectKey", FieldMap.build("projectKey", "bogus"));
            // Sweet - it worked
        }
        catch (DataAccessException ex)
        {
            log.warn("JRADEV-23357: unable to select from the 'ProjectKey' entity.");
            cleanupDatabaseTableName("PROJECT_KEY");
            needsTablesRecreated = true;
        }
        if (needsTablesRecreated)
        {
            kickOfbizInTheGuts();
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value =
            "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE", justification = "Dynamic SQL does not come from user input so no SQL injection is possible.")
    private void cleanupDatabaseTableName(String tableName)
    {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try
        {
            // JRADEV-23357
            // So the table presumably exists but in upper-case and we need lower-case. Lets confirm that...
            con = DefaultOfBizConnectionFactory.getInstance().getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
            rs.next();
            int count = rs.getInt(1);
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            if (count > 0)
            {
                throw new IllegalStateException("Need to rename the " + tableName + " table, but there is existing data in it. Please contact Atlassian Support.");
            }
            // Yep, table exists and has no data - drop and recreate
            log.info("We need to change the case of table '" + tableName + "'... will drop table and then recreate.");
            stmt = con.createStatement();
            stmt.execute("DROP TABLE " + tableName);
            // In a sec we will force OfBiz to rerun its startup check and add back the missing tables.
        }
        catch (SQLException e)
        {
            throw new DataAccessException(e);
        }
        finally
        {
            DatabaseUtil.closeQuietly(rs);
            DatabaseUtil.closeQuietly(stmt);
            DatabaseUtil.closeQuietly(con);
        }
    }

    private void kickOfbizInTheGuts()
    {
        DelegatorInterface delegatorInterface = ComponentAccessor.getComponent(DelegatorInterface.class);
        Map<String,ModelEntity> modelEntities = delegatorInterface.getModelEntityMapByGroup("default");
        try
        {
            GenericHelper helper = delegatorInterface.getEntityHelper("ProjectKey");
            // This will loop over all entities and add any that are missing - just like we do on startup.
            helper.checkDataSource(modelEntities, null, true);
        }
        catch (GenericEntityException ex)
        {
            throw new DataAccessException(ex);
        }
    }
}
