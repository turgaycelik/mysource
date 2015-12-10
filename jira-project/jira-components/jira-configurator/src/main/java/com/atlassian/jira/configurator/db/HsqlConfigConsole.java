package com.atlassian.jira.configurator.db;

import com.atlassian.jira.config.database.DatabaseType;
import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.exception.ParseException;

public class HsqlConfigConsole  implements DatabaseConfigConsole
{
    private final String PREFIX = "jdbc:hsqldb:";
    private final String SUFFIX = "/database/jiradb";

    private String jiraHome;

    public String getDatabaseType()
    {
        return "HSQL";
    }

    public String getClassName()
    {
        return DatabaseType.HSQL.getJdbcDriverClassName();
    }

    public String getUsername()
    {
        return "sa";
    }

    public String getPassword()
    {
        return "";
    }

    @Override
    public ConfigField[] getFields()
    {
        return null;
    }

    @Override
    public void setSettings(Settings settings) throws ParseException
    {
        jiraHome = settings.getJiraHome();
    }

    @Override
    public String getInstanceName()
    {
        return "(unused)";
    }

    @Override
    public void saveSettings(Settings newSettings) throws ValidationException
    {
        newSettings.getJdbcDatasourceBuilder()
                .setDriverClassName(getClassName())
                .setJdbcUrl(getUrl())
                .setUsername(getUsername())
                .setPassword(getPassword());
    }

    @Override
    public void testConnection()
    {
        // Nothing to test
    }

    @Override
    public String getUrl()
    {
        return PREFIX + jiraHome + SUFFIX;
    }

}
