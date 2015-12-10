package com.atlassian.jira.configurator.db;

import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.configurator.config.ValidationException;

import java.sql.SQLException;

public interface ConnectionConfig
{
    /**
     * Saves the settings in this ConnectionConfig into the given Settings object.
     * @param newSettings Settings object to copy the settings into.
     */
    void saveSettings(Settings newSettings) throws ValidationException;

    void testConnection() throws ClassNotFoundException, SQLException, ValidationException;

    String getUsername();
    String getPassword();
    String getUrl() throws ValidationException;
    String getClassName();

}
