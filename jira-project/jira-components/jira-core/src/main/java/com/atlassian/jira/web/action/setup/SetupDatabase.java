package com.atlassian.jira.web.action.setup;

import com.atlassian.config.ConfigurationException;
import com.atlassian.config.bootstrap.BootstrapException;
import com.atlassian.config.bootstrap.DefaultAtlassianBootstrapManager;
import com.atlassian.config.db.DatabaseDetails;
import com.atlassian.config.db.DatabaseList;
import com.atlassian.core.util.PairType;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.database.DatabaseConfig;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.config.database.DatabaseDriverRegisterer;
import com.atlassian.jira.config.database.Datasource;
import com.atlassian.jira.config.database.InvalidDatabaseDriverException;
import com.atlassian.jira.config.database.JdbcDatasource;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.database.DatabaseType;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.help.HelpUrl;
import com.atlassian.jira.help.HelpUrls;
import com.atlassian.jira.startup.DatabaseInitialImporter;
import com.atlassian.jira.startup.StartupCheck;
import com.atlassian.jira.template.TemplateSources;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.util.HelpUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.config.ConnectionPoolInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * <p>Action for configuring a database connection for JIRA and testing that it works.
 * Also allows to set the server language for JIRA.</p>
 *
 * <p>Step 1 of the setup wizard.</p>
 *
 * @since v4.4
 */
public class SetupDatabase extends AbstractSetupAction
{
    private static final int DEFAULT_POOL_SIZE = 20;
    private boolean testingConnection; // The hidden field populated when the button is clicked.

    private final BuildUtilsInfo buildUtilsInfo;
    private final DatabaseConfigurationManager databaseConfigurationManager;
    private final IndexLanguageToLocaleMapper languageToLocaleMapper;
    private final VelocityTemplatingEngine velocityEngine;
    private final DatabaseInitialImporter databaseInitialImporter;
    private final HelpUrls helpUrls;
    private final JiraProperties jiraProperties;

    private String language;
    private boolean changingLanguage;

    // Database information
    private String databaseOption = "INTERNAL"; // default to internal
    private String databaseType;
    private String schemaName;
    private static final Map<String, String> SCHEMA_NAMES = ImmutableMap.of("postgres72", "public", "mssql", "dbo");
    private String jdbcHostname;
    private String jdbcPort;
    private String jdbcDatabase;
    private String jdbcSid;
    private String jdbcUsername;
    private String jdbcPassword;
    private Integer poolSize = DEFAULT_POOL_SIZE;

    private String dbErrorMessage;

    // from atlassian config (cf. supportedDatabases.properties)
    private DatabaseList databaseList = new DatabaseList();
    private boolean isTestConnectionSuccessful;
    private boolean showNonEmptyDatabaseUpgradeDocumentationMessage = false;

    @VisibleForTesting
    static final Map<String, DatabaseType> databaseTypeMap = MapBuilder.<String, DatabaseType>newBuilder()
            .add(DatabaseType.ORACLE.getTypeName(), DatabaseType.ORACLE)
            .add(DatabaseType.POSTGRES.getTypeName(), DatabaseType.POSTGRES)
            .add(DatabaseType.MY_SQL.getTypeName(), DatabaseType.MY_SQL)
            .add(DatabaseType.SQL_SERVER.getTypeName(), DatabaseType.SQL_SERVER)
            .toMap();

    public SetupDatabase(final FileFactory fileFactory, final IndexLanguageToLocaleMapper languageToLocaleMapper,
            BuildUtilsInfo buildUtilsInfo, final DatabaseConfigurationManager databaseConfigurationManager,
            VelocityTemplatingEngine velocityEngine, final DatabaseInitialImporter databaseInitialImporter,
            final HelpUrls helpUrls, final JiraProperties jiraProperties)
    {
        super(fileFactory);
        this.languageToLocaleMapper = languageToLocaleMapper;
        this.buildUtilsInfo = buildUtilsInfo;
        this.databaseConfigurationManager = databaseConfigurationManager;
        this.velocityEngine = velocityEngine;
        this.databaseInitialImporter = databaseInitialImporter;
        this.helpUrls = helpUrls;
        this.jiraProperties = jiraProperties;
    }

    public String doDefault()
    {
        if (isDatabaseSetup())
        {
            if (!databaseInitialImporter.dataAlreadyLoaded())
            {
                // Load initial data
                databaseInitialImporter.importInitialData(getLoggedInUser());
            }
            return forceRedirect("SetupApplicationProperties!default.jspa");
        }
        setIndexingLanguageForDefaultServerLocale();
        return INPUT;
    }

    @Override
    protected void doValidation()
    {
        // Don't validate if we're already setup or just changing languages
        if (setupAlready() || isChangingLanguage())
        {
            return;
        }

        // if using external db
        if (isExternalDatabase())
        {
            // Make sure a database type is selected!
            if ("".equals(databaseType))
            {
                addError("databaseType", getText("setupdb.error.selectDatabaseType"));
            }

            if (StringUtils.isEmpty(jdbcHostname))
            {
                addError("jdbcHostname", getText("setupdb.error.requireJdbcHostname"));
            }

            if (StringUtils.isEmpty(jdbcPort))
            {
                addError("jdbcPort", getText("setupdb.error.requireJdbcPort"));
            }

            if (isOracleDatabaseType())
            {
                if (StringUtils.isEmpty(jdbcSid))
                {
                    addError("jdbcSid", getText("setupdb.error.requireSID"));
                }
            }
            else
            {
                if (StringUtils.isEmpty(jdbcDatabase))
                {
                    addError("jdbcDatabase", getText("setupdb.error.requireDatabase"));
                }
            }

            if (StringUtils.isEmpty(jdbcUsername))
            {
                addError("jdbcUsername", getText("setupdb.error.requireJdbcUsername"));
            }
        }

        testConnection();

        validateDatabaseIsEmpty();

        super.doValidation();
    }

    private boolean isOracleDatabaseType()
    {
        return "oracle10g".equals(databaseType);
    }

    private boolean isExternalDatabase()
    {
        return "EXTERNAL".equals(databaseOption);
    }

    private void testConnection()
    {
        // Only test the connection if all required fields are entered
        if (!hasAnyErrors())
        {
            try
            {
                final DatabaseConfig databaseConfiguration = createDatabaseConfiguration();
                final StartupCheck startupCheck = databaseConfiguration.testConnection(new DefaultAtlassianBootstrapManager());
                if (startupCheck != null) {
                    addErrorMessage(startupCheck.getFaultDescription());
                }
            }
            catch (BootstrapException e)
            {
                addErrorMessage(getText("setupdb.error.connectionFailed"));
                Throwable cause = e.getCause();
                addErrorMessage(cause.getLocalizedMessage());
                if (cause.getCause() != null && !cause.getLocalizedMessage().equals(cause.getCause().getLocalizedMessage()))
                {
                    addErrorMessage(cause.getCause().getLocalizedMessage());
                }
            }
            catch (InvalidDatabaseDriverException e)
            {
                addErrorMessage(getText("setupdb.error.invalidDriver", e.driverClassName()));
                // additional message for drivers we're not shipping
                if (isNotShippedDriver(getDatabaseTypeEnum()))
                {
                    dbErrorMessage = createNotPresentMessage(getDatabaseTypeEnum());
                }
            }
            isTestConnectionSuccessful = isTestingConnection() && !hasAnyErrors();
        }
    }

    private void validateDatabaseIsEmpty()
    {
        // Only test if database if empty if all previous checks have succeeded
        if (!hasAnyErrors())
        {
            boolean isDatabaseEmpty = false;
            final DatabaseConfig databaseConfiguration = createDatabaseConfiguration();
            try
            {
                isDatabaseEmpty = databaseConfiguration.isDatabaseEmpty(new DefaultAtlassianBootstrapManager());
            }
            catch (BootstrapException e)
            {
                // This really shouldn't happen since we have just successfully tested database connection
                addErrorMessage(getText("setupdb.error.connectionFailed"));
                Throwable cause = e.getCause();
                addErrorMessage(cause.getLocalizedMessage());
                if (cause.getCause() != null && !cause.getLocalizedMessage().equals(cause.getCause().getLocalizedMessage()))
                {
                    addErrorMessage(cause.getCause().getLocalizedMessage());
                }
            }

            if (!isDatabaseEmpty)
            {
                addErrorMessage(getText("setupdb.error.nonemptyDatabase"));
                showNonEmptyDatabaseUpgradeDocumentationMessage = true;
            }

            isTestConnectionSuccessful = isTestingConnection() && !hasAnyErrors();
        }
    }

    public boolean isShowNonEmptyDatabaseUpgradeDocumentationMessage()
    {
        return showNonEmptyDatabaseUpgradeDocumentationMessage;
    }

    public String getNonEmptyDatabaseUpgradeDocumentationMessage()
    {
        return getText("setupdb.error.nonemptyDatabase.upgrade.documentation", getHelpUtil().getHelpPath("dbconfig.upgrade.instructions").getUrl());
    }

    HelpUtil getHelpUtil()
    {
        return HelpUtil.getInstance();
    }

    @Override
    protected String doExecute() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        if (!isDatabaseSetup())
        {
            if (isChangingLanguage())
            {
                setLanguage();
                return INPUT;
            }
            else if (isTestingConnection())
            {
                return hasAnyErrors() ? ERROR : INPUT;
            }
            else
            {
                DatabaseConfig databaseConfiguration = createDatabaseConfiguration();
                databaseConfigurationManager.setDatabaseConfiguration(databaseConfiguration);
                // now do the post db setup work
                databaseConfigurationManager.activateDatabase();
            }
        }
        // Load initial data
        databaseInitialImporter.importInitialData(getLoggedInUser());

        setLanguage();

        return forceRedirect("SetupApplicationProperties!default.jspa");
    }

    private DatabaseConfig createDatabaseConfiguration()
    {
        if (isExternalDatabase())
        {
            final DatabaseDetails databaseDetails = getDatabaseDetails(databaseType);
            poolSize = databaseDetails.getPoolSize();
            final String instanceName = isOracleDatabaseType() ? jdbcSid : jdbcDatabase;

            final DatabaseType type = getDatabaseTypeEnum();

            ConnectionPoolInfo.Builder connectionPoolInfoBuilder = ConnectionPoolInfo.builder();
            connectionPoolInfoBuilder
                    .setPoolMaxSize(poolSize)
                    .setPoolMaxWait(30000L)
                    .setPoolMinSize(poolSize)
                    .setRemoveAbandoned(true)
                    .setRemoveAbandonedTimeout(300);
            if (type == DatabaseType.MY_SQL)
            {
                connectionPoolInfoBuilder.setValidationQuery("select 1");
                connectionPoolInfoBuilder.setValidationQueryTimeout(3);
                connectionPoolInfoBuilder.setTestWhileIdle(true);
                connectionPoolInfoBuilder.setMinEvictableTimeMillis(60000L);
                connectionPoolInfoBuilder.setTimeBetweenEvictionRunsMillis(300000L);
            }
            if (type == DatabaseType.HSQL)
            {
                connectionPoolInfoBuilder.setMinEvictableTimeMillis(4000L);
                connectionPoolInfoBuilder.setTimeBetweenEvictionRunsMillis(5000L);
            }

            JdbcDatasource.Builder builder = JdbcDatasource.builder()
                    .setDatabaseType(type)
                    .setHostname(jdbcHostname)
                    .setPort(jdbcPort)
                    .setInstance(instanceName)
                    .setUsername(jdbcUsername)
                    .setPassword(jdbcPassword)
                    .setConnectionPoolInfo(connectionPoolInfoBuilder.build());

            Datasource datasource = builder.build();
            return new DatabaseConfig(databaseType, schemaName, datasource);
        }
        // we are doing internal
        return databaseConfigurationManager.getInternalDatabaseConfiguration();
    }

    public boolean isDatabaseConnectionTestWorked()
    {
        return isTestConnectionSuccessful;
    }

    public boolean isDatabaseSetup()
    {
        return databaseConfigurationManager.isDatabaseSetup();
    }

    public Map<String, String> getInstalledLocales()
    {
        Set<Locale> installedLocales = ComponentAccessor.getComponentOfType(LocaleManager.class).getInstalledLocales();
        Map<String, String> localeMap = new LinkedHashMap<String, String>();

        for (Locale installedLocale : installedLocales)
        {
            localeMap.put(installedLocale.toString(), installedLocale.getDisplayName(installedLocale));
        }

        return localeMap;
    }

    public List<PairType> getExternalDatabases()
    {
        PairType defaultSelect = new PairType("", getText("setupdb.database.selectType"));

        List<PairType> selectList = new ArrayList<PairType>();
        selectList.add(defaultSelect);
        selectList.addAll(databaseList.getDatabases());

        return selectList;
    }

    public List<DbMessage> getDatabaseMessages()
    {
        final ImmutableList.Builder<DbMessage> result = ImmutableList.builder();
        for (Map.Entry<String,DatabaseType> databaseType : databaseTypeMap.entrySet())
        {
            final DatabaseDriverRegisterer registerer = DatabaseDriverRegisterer.forType(databaseType.getValue());
            // add special message if the driver is not shipped and not registered (i.e. currently not installed)
            if (isNotShippedDriver(databaseType.getValue()) && !registerer.isDriverRegistered())
            {
                result.add(new DbMessage(databaseType.getKey(), createNotPresentMessage(databaseType.getValue())));
            }
            else
            {
                result.add(new DbMessage(databaseType.getKey(), null));
            }
        }
        return result.build();
    }

    public DatabaseDetails getDatabaseDetails(String database)
    {
        try
        {
            return DatabaseDetails.getDefaults(database);
        }
        catch (ConfigurationException e)
        {
            log.debug(e);
            return null;
        }
    }

    public String getSchemaName(String database)
    {
        return SCHEMA_NAMES.get(database);
    }

    public String getDefaultServerLanguage()
    {
        String defaultLocale = getFreshApplicationProperties().getDefaultLocale().toString();
        if (getInstalledLocales().keySet().contains(defaultLocale))
        {
            return defaultLocale;
        }
        else
        {
            return "en_UK";
        }
    }

    /**
     * We always get a fresh copy of the application properties because during the short life of this Action,
     * the Pico container can be swapped out from underneath us.
     * @return ApplicationProperties
     */
    private ApplicationProperties getFreshApplicationProperties()
    {
        return ComponentAccessor.getApplicationProperties();
    }

    /**
     * Checks for drivers that we're not shipping with JIRA by default. We include special messages to help our
     * beloved customers install appropriate drivers if they need.
     *
     * @param databaseType database type
     * @return <code>true</code> if given <tt>databaseType</tt> is not being shipped with JIRA by default and requires
     * manual installation
     */
    private boolean isNotShippedDriver(DatabaseType databaseType)
    {
        // ATM MySQL only
        return databaseType == DatabaseType.MY_SQL;
    }

    private String createNotPresentMessage(DatabaseType dbType)
    {
        return getText("setupdb.database.drivernotpresent", dbType.getDisplayName(),
                getDocLink(dbType, getText("setupdb.database.drivernotpresent.linktext")));
    }

    private String getDocLink(DatabaseType type, String linkText)
    {
        final HelpUrl helpPath = DatabaseDriverRegisterer.getDbConfigLink(type, helpUrls);
        return velocityEngine.render(TemplateSources.file("/templates/jira/simplelink.vm"))
               .applying(ImmutableMap.<String,Object>of(
                       "targetUrl", helpPath.getUrl(),
                       "title", helpPath.getTitle(),
                       "alt", helpPath.getAlt(),
                       "newWindow", true,
                       "linkText", linkText
               )).asHtml();
    }

    private void setLanguage()
    {
        // if user selects a non-default language, we need to change the default locale
        if (StringUtils.isNotBlank(getLanguage()))
        {
            getFreshApplicationProperties().setString(APKeys.JIRA_I18N_DEFAULT_LOCALE, getLanguage());
        }
        // set the indexing language for the selected locale
        setIndexingLanguageForDefaultServerLocale();
    }

    private void setIndexingLanguageForDefaultServerLocale()
    {
        getFreshApplicationProperties().setString
                (
                        APKeys.JIRA_I18N_LANGUAGE_INPUT,
                        languageToLocaleMapper.getLanguageForLocale(getLocale().toString())
                );
    }

    private DatabaseType getDatabaseTypeEnum()
    {
        final DatabaseType type = databaseTypeMap.get(databaseType);
        if (type == null)
        {
            throw new IllegalStateException("Unknown database type '" + databaseType + "'");
        }
        return type;
    }

    public String getDatabaseType()
    {
        return databaseType;
    }

    public void setDatabaseType(String databaseType)
    {
        this.databaseType = databaseType;
    }

    public String getDatabaseOption()
    {
        return databaseOption;
    }

    public void setDatabaseOption(String databaseOption)
    {
        this.databaseOption = databaseOption;
    }

    @Override
    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public BuildUtilsInfo getBuildUtilsInfo()
    {
        return buildUtilsInfo;
    }

    public int modulo(int index, int modulus)
    {
        return index % modulus;
    }

    public boolean isTestingConnection()
    {
        return testingConnection;
    }

    public void setTestingConnection(boolean testingConnection)
    {
        this.testingConnection = testingConnection;
    }

    public String getJdbcHostname()
    {
        return jdbcHostname;
    }

    public void setJdbcHostname(String jdbcHostname)
    {
        this.jdbcHostname = jdbcHostname;
    }

    public String getJdbcPassword()
    {
        return jdbcPassword;
    }

    public void setJdbcPassword(String jdbcPassword)
    {
        this.jdbcPassword = jdbcPassword;
    }

    public String getJdbcPort()
    {
        return jdbcPort;
    }

    public void setJdbcPort(String jdbcPort)
    {
        this.jdbcPort = jdbcPort;
    }

    public String getJdbcDatabase()
    {
        return jdbcDatabase;
    }

    public void setJdbcDatabase(String jdbcDatabase)
    {
        this.jdbcDatabase = jdbcDatabase;
    }

    public String getJdbcSid()
    {
        return jdbcSid;
    }

    public void setJdbcSid(String jdbcSid)
    {
        this.jdbcSid = jdbcSid;
    }

    public String getJdbcUsername()
    {
        return jdbcUsername;
    }

    public void setJdbcUsername(String jdbcUsername)
    {
        this.jdbcUsername = jdbcUsername;
    }

    public String getSchemaName()
    {
        return schemaName;
    }

    public void setSchemaName(String schemaName)
    {
        this.schemaName = schemaName;
    }

    public boolean isChangingLanguage()
    {
        return changingLanguage;
    }

    public void setChangingLanguage(boolean changingLanguage)
    {
        this.changingLanguage = changingLanguage;
    }

    public boolean isHasDbErrorMessage()
    {
        return dbErrorMessage != null;
    }

    public String getDbErrorMessage()
    {
        return dbErrorMessage;
    }

    public String getProductName() {
        return jiraProperties.getProductName();
    }

    public static class DbMessage
    {
        private final String type;
        private final String message;

        public DbMessage(String type, String msg)
        {
            this.type = type;
            this.message = msg;
        }

        public String getType()
        {
            return type;
        }

        public boolean getHasMessage()
        {
            return message != null;
        }

        public String getMessage()
        {
            return message;
        }
    }
}
