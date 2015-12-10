package com.atlassian.jira.configurator.db;

import com.atlassian.jira.config.database.DatabaseType;
import com.atlassian.jira.config.database.JdbcDatasource;
import com.atlassian.jira.config.database.jdbcurlparser.DatabaseInstance;
import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.exception.ParseException;

public class DatabaseConfigConsoleImpl extends AbstractConnectionConfig implements DatabaseConfigConsole
{
    private final ConfigField cfHostname = new ConfigField("Hostname");
    private final ConfigField cfPort = new ConfigField("Port");
    private final ConfigField cfInstance = new ConfigField("Instance");
    private final ConfigField cfUsername = new ConfigField("Username");
    private final ConfigField cfPassword = new ConfigField("Password", true);
    private ConfigField[] fields = {cfHostname, cfPort, cfInstance, cfUsername, cfPassword};

    private DatabaseType databaseType;

    public DatabaseConfigConsoleImpl(final DatabaseType databaseType)
    {
        this.databaseType = databaseType;
        cfInstance.setLabel(databaseType.getInstanceFieldName());
    }

    public String getDatabaseType()
    {
        return databaseType.getDisplayName();
    }

    public ConfigField[] getFields()
    {
        return fields;
    }
    
    public void setSettings(final Settings settings) throws ParseException
    {
        final JdbcDatasource.Builder datasourceBuilder = settings.getJdbcDatasourceBuilder();
        cfUsername.setValue(datasourceBuilder.getUsername());
        cfPassword.setValue(datasourceBuilder.getPassword());

        // parse the URL.
        DatabaseInstance databaseInstance =  databaseType.getJdbcUrlParser().parseUrl(datasourceBuilder.getJdbcUrl());

        cfHostname.setValue(databaseInstance.getHostname());
        cfPort.setValue(databaseInstance.getPort());
        cfInstance.setValue(databaseInstance.getInstance());
    }

    public String getInstanceName()
    {
        return cfHostname.getValue() + ':' + cfPort.getValue() + '/' + cfInstance.getValue();
    }

    public String getUsername()
    {
        return cfUsername.getValue();
    }

    public String getPassword()
    {
        return cfPassword.getValue();
    }

    public String getUrl() throws ValidationException
    {
        try
        {
            return  databaseType.getJdbcUrlParser().getUrl(cfHostname.getValue(), cfPort.getValue(), cfInstance.getValue());
        }
        catch (ParseException e)
        {
            throw new ValidationException(e.getMessage());
        }
    }

    public String getClassName()
    {
        return databaseType.getJdbcDriverClassName();
    }

}
