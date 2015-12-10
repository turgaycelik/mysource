package com.atlassian.jira.configurator.db;

import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.exception.ParseException;

/**
 * @since 4.1
 */
public interface DatabaseConfigConsole extends ConnectionConfig
{
    String getDatabaseType();
    
    ConfigField[] getFields();

    void setSettings(Settings settings) throws ParseException;

    String getInstanceName();
}
